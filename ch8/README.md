# Section 08 — RAG 품질 개선: 튜닝과 평가 (실행 예제)

**측정할 수 없으면 개선할 수 없다.** 튜닝 실험과 정량 평가를 실제로 돌려봅니다.

## 준비

```bash
docker compose up -d
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

## 실행 순서

### 1) 청크 크기별 적재 (200 / 500 / 1000)
```bash
curl -X POST localhost:8080/api/tuning/ingest-all
```
같은 문서를 **3가지 청크 크기로 각각 적재**합니다.
`chunkSize` 메타데이터로 논리적 컬렉션을 분리하므로 공정한 비교가 가능합니다.

```json
{
  "chunkSize_200":  {"chunks":18,"avgChars":260},
  "chunkSize_500":  {"chunks":7, "avgChars":650},
  "chunkSize_1000": {"chunks":4, "avgChars":1180}
}
```

### 2) 청크 크기별 검색 결과 비교 ⭐
```bash
curl "localhost:8080/api/tuning/compare?q=연차는 며칠인가요&topK=2"
```
같은 질문에 대해 청크 크기별로 **어떤 근거가 검색되는지** 나란히 보여줍니다.
- 200: 조각이 정밀하지만 문맥이 끊길 수 있음
- 1000: 문맥은 온전하나 무관한 내용이 섞임

### 3) 검색 파라미터 실험
```bash
# topK 변화
curl "localhost:8080/api/tuning/search?q=휴가&topK=1&chunkSize=500"
curl "localhost:8080/api/tuning/search?q=휴가&topK=8&chunkSize=500"

# threshold 변화 — 너무 높이면 hitCount가 0이 된다
curl "localhost:8080/api/tuning/search?q=휴가&threshold=0.3&chunkSize=500"
curl "localhost:8080/api/tuning/search?q=휴가&threshold=0.8&chunkSize=500"

# 메타데이터 필터 — 가장 저렴하고 확실한 정밀도 개선
curl "localhost:8080/api/tuning/search?q=휴가&category=인사규정&chunkSize=500"
```

> **관찰**: `threshold`를 올리면 노이즈가 줄지만 `hitCount`가 0이 되어
> "확인할 수 없습니다"만 반복하게 됩니다. **유사도 절대값은 모델마다 다르므로 자기 데이터로 캘리브레이션**하세요.

### 4) 평가 세트 실행 → Before/After 리포트 ⭐
```bash
curl -X POST localhost:8080/api/eval/run
```

```json
{
  "overallPassRate": "12/15 (80%)",
  "passRateByType": { "general":"8/9 (89%)", "boundary":"2/3 (67%)", "trap":"2/3 (67%)" },
  "avgContextTokensApprox": 1350,
  "failedIds": ["B02","T01","G07"],
  "hint": "회귀 확인: 이전 실행의 failedIds 와 비교하라..."
}
```

**Before/After 실습**:
1. 위 명령으로 Before 리포트 저장
2. `application.yml`에서 **한 가지만** 변경 (`active-chunk-size` 또는 `top-k` 또는 `similarity-threshold`)
3. 재기동 후 다시 실행 → After 리포트
4. **`failedIds`를 비교해 회귀 문항을 확인** ← 가장 중요

### 5) 품질 테스트 (회귀 감지기)
```bash
./gradlew test -Peval      # LLM 호출 비용 발생
./gradlew test             # 평가 테스트 제외 (기본)
```

## 평가 세트

`src/main/resources/eval/evaluation-set.json` — **15문항**

| 유형 | 개수 | 목적 |
|---|---|---|
| `general` | 9 | 대표 질의 |
| `boundary` | 3 | 의역·구어체·복합 조건 |
| **`trap`** | 3 | **문서에 없는 내용 — "확인할 수 없습니다"가 정답 (환각 방어력 측정)** |

문항을 추가하려면 JSON에 항목을 넣기만 하면 됩니다.

## 실습 과제

1. `active-chunk-size`를 200 / 1000으로 바꿔 통과율 변화 측정
2. `similarity-threshold`를 0.5로 올려 trap 통과율은 오르고 general 통과율은 떨어지는지 확인
3. 평가 세트에 본인 도메인 질문 5개 추가

## 주요 파일
```
src/main/java/com/example/rag/
├── tuning/TuningService.java       # 청크 실험 · 파라미터 실험
├── eval/RagService.java            # 평가 대상 RAG (근거 반환)
├── eval/EvaluationService.java     # LLM-as-a-Judge 평가 + 리포트
├── eval/EvaluationSet.java         # 평가 세트 모델
└── eval/EvalController.java
src/test/java/com/example/rag/RagQualityTest.java   # 품질 회귀 감지기
src/main/resources/eval/evaluation-set.json          # 15문항 평가 세트
```

---
### 주의
- **Spring AI 1.0.0 GA 기준**. `RelevancyEvaluator`·`EvaluationRequest` 시그니처는 버전 확인 필요.
- 평가 1회 실행 시 **15문항 × (RAG 호출 + 판정 호출)** 의 API 비용이 발생합니다.
- `avgContextTokensApprox`는 한국어 근사치(문자수/2)이며 정확한 과금 토큰이 아닙니다.
