# Section 09 — RAG 응용: 추천/판단 시스템 (실행 예제)

Q&A를 넘어 **근거를 바탕으로 추천하고 판단하는** AI Expert를 만듭니다.
**구조화 응답(Sec 4) × RAG(Sec 6~8)** 의 결합이 핵심입니다.

## 준비

```bash
docker compose up -d
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

## 실행 순서

### 1) 도메인 문서 적재
```bash
curl -X POST localhost:8080/api/expert/ingest
# {"documents":1,"chunks":5}
```
* Unknown type vector 오류 발생 시, 
  * docker exec -it rag-pgvector psql -U rag -d ragdb
  * SELECT extname, extversion FROM pg_extension WHERE extname = 'vector'; 확인 후 없으면,
  * CREATE EXTENSION IF NOT EXISTS vector; 실행 (재실행)
* Spring AI가 사용하는 테이블 확인 
```sql
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'vector_store';
```

### 2) 추천 요청 — 정상 케이스
```bash
curl -X POST localhost:8080/api/expert/recommend \
  -H 'Content-Type: application/json' \
  -d '{"vcpu":4,"memGb":8,"budget":150,"workload":"웹 애플리케이션 서버"}'
```

```json
{
  "decision": "gp-medium",
  "score": 90,
  "reason": "요구사항인 vCPU 4개와 메모리 8GiB를 정확히 충족하며, 월 90 USD로 예산 150 USD 이내입니다. ...",
  "evidences": [
    { "source": "cloud-instances.txt", "quote": "vCPU: 4, 메모리: 8 GiB" },
    { "source": "cloud-instances.txt", "quote": "월 예상 비용: 90 USD (온디맨드 기준)" }
  ],
  "evidenceVerified": true,
  "warnings": [],
  "routing": "AUTO_APPROVE"
}
```

### 3) 판단 보류 케이스 ⭐
```bash
curl -X POST localhost:8080/api/expert/recommend \
  -H 'Content-Type: application/json' \
  -d '{"vcpu":256,"memGb":4096,"budget":10,"workload":"양자컴퓨팅 시뮬레이션"}'
```
근거 문서에 충족하는 인스턴스가 없으므로 **`decision: "판단 보류"`, `routing: "HOLD"`** 가 나와야 합니다.

> **교안 9장 6절의 원칙**: *근거가 약하면 억지 판단 대신 판단 보류*

### 4) 예산 부족 케이스 (검토 필요)
```bash
curl -X POST localhost:8080/api/expert/recommend \
  -H 'Content-Type: application/json' \
  -d '{"vcpu":8,"memGb":64,"budget":100,"workload":"대용량 데이터베이스"}'
```
스펙은 `r-large`(300 USD)가 맞지만 예산이 부족 → **score 중간대 → `REVIEW_REQUIRED`**

## 핵심 구현 포인트

### ① 구조화 응답 × RAG
```java
return chatClient.prompt()
    .system("아래 [근거]만을 사용해 추천하라 ...")
    .user(u -> u.text("[요구사항]\n{req}\n\n[근거]\n{ctx}") ...)
    .call()
    .entity(Recommendation.class);   // ← 객체로 바로 매핑
```

### ② 근거 검증기 (EvidenceVerifier) ⭐
LLM이 `evidences`를 **지어낼 수 있으므로**, 반환된 인용문이 실제 검색된 문서에 존재하는지 후검증합니다.
검증 실패 시 **score를 49 이하로 강제 하향**하여 자동 승인을 막습니다.

### ③ HITL 라우팅 (교안 9장 6절)
| score | routing | 처리 |
|---|---|---|
| 80 이상 | `AUTO_APPROVE` | 자동 진행 (로그 기록) |
| 50~79 | `REVIEW_REQUIRED` | 담당자 검토 큐로 |
| 50 미만 | `HOLD` | **판단 보류** — 사유와 함께 반환 |

임계값은 `application.yml`의 `expert.auto-approve-score` / `review-required-score`로 조정합니다.

## 실습 과제

1. **근거 검증 무력화 실험**: 시스템 프롬프트의 `"그대로 인용하라"`를 `"요약해서 적어라"`로 바꾼 뒤
   `evidenceVerified`가 `false`로 바뀌고 routing이 강등되는지 확인
2. **temperature 영향**: `0.1` → `0.9`로 올려 같은 요청을 5회 반복 → 판정이 흔들리는 정도 관찰
3. **다른 도메인 적용**: `docs/`에 본인 도메인 문서를 넣고 `Requirement` 필드만 바꿔 재사용
   (교안 9장 3절 — 도메인만 갈아 끼우면 되는 **패턴**)

## 주요 파일
```
src/main/java/com/example/expert/
├── domain/Requirement.java        # 입력
├── domain/Recommendation.java     # LLM 판정 (decision/score/reason/evidences)
├── domain/Evidence.java           # 출처 + 인용문
├── domain/ExpertResponse.java     # 최종 응답 (검증 결과 + 라우팅 포함)
├── service/AiExpertService.java   # 검색 → 구조화 판정 → 검증 → 라우팅
├── service/EvidenceVerifier.java  # 인용문 후검증 (환각 탐지)
├── service/IngestionService.java
└── api/ExpertController.java
src/main/resources/docs/cloud-instances.txt   # 강의용 가상 스펙·요금 자료
```

---
### 주의
- **Spring AI 1.0.0 GA 기준**. `.entity()` 구조화 응답 API는 버전 확인 필요.
- 실습 문서는 **가상의 자료**입니다. 실제 클라우드 사업자의 스펙·요금이 아닙니다.
- LLM 판단은 **의사결정 지원**이지 최종 결재가 아닙니다 (교안 9장 6절).
