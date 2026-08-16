# Section 12 — Dify 소개: Spring AI와의 하이브리드 (실행 예제)

Dify 자체는 **UI로 조작**하는 플랫폼입니다. 이 예제는 그 결과물(게시된 Dify 앱)을
**Spring이 호출**하고, **Spring AI 직접 구현과 나란히 비교**하는 데 집중합니다.

## 준비

```bash
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

### Dify 실습 (교안 10장 4절 — UI에서 10분)
1. Dify Cloud 가입 또는 Self-hosted 기동
2. Knowledge 생성 → 문서 업로드 → 인덱싱
3. Chatbot 앱 생성 → Knowledge 연결 → 게시
4. **API 키 발급** → 아래처럼 환경변수로 주입
```bash
export DIFY_BASE_URL=https://api.dify.ai/v1     # 또는 self-hosted URL
export DIFY_API_KEY=app-xxxxxxxx
./gradlew bootRun
```

> ⚠️ **DIFY_API_KEY가 없어도 실습이 진행됩니다.**
> 키가 없으면 `DifyClient`가 **mock 응답**을 반환하여
> "Spring이 Dify를 호출하는 흐름"을 확인할 수 있습니다 (`"mocked": true`).

---

## 실행

### 1) Spring AI 직접 구현에 질문
```bash
curl "localhost:8080/api/springai/ask?q=연차는 며칠인가요"
```

### 2) Dify 챗봇에 질문
```bash
curl "localhost:8080/api/dify/ask?q=연차는 며칠인가요"
# 키 없으면: {"engine":"Dify","answer":"[MOCK] ...","mocked":true}
```

### 3) 나란히 비교 ⭐ (이 강의의 핵심 체험)
```bash
curl "localhost:8080/api/compare?q=연차는 며칠인가요"
```
```json
{
  "question": "연차는 며칠인가요",
  "springAI": { "answer": "...", "latencyMs": 1200 },
  "dify":     { "answer": "...", "latencyMs": 800, "mocked": false },
  "hint": "판단은 '누가 이겼나'가 아니라 '어떤 상황에 무엇이 유리한가'..."
}
```

---

## 비교 실험 설계 (교안 10장 4절)

| 비교 축 | 측정 방법 |
|---|---|
| 답변 품질 | 같은 질문 세트에 대한 응답 비교 (Section 8 평가 세트 재활용 가능) |
| 응답 속도 | `latencyMs` 필드 |
| 구축 시간 | Spring AI 4.5h vs Dify 10분 (이미 경험) |
| 변경 유연성 | "카테고리 필터 추가" 요구 반영 시간 |

> **공정한 비교**: 같은 문서·같은 모델·같은 질문으로 통제해야 인상평이 아닌 비교가 됩니다.

## 판단 프레임 (교안 12장 6절)

| Spring AI 직접 구현이 맞을 때 | Dify 같은 플랫폼이 맞을 때 |
|---|---|
| 업무 로직과 깊게 결합 | 아이디어 검증 — 속도 우선 |
| 커스텀 로직이 핵심 가치 | 현업이 프롬프트 직접 수정 |
| 데이터가 나갈 수 없음 | 표준 문서 Q&A로 충분 |

**현실의 정답은 대체로 하이브리드** → Section 13에서 실제 연동을 구현합니다.

## 주요 파일
```
src/main/java/com/example/dify/
├── client/DifyClient.java       # Dify chat-messages 호출 (+ mock 폴백)
└── api/CompareController.java   # springai / dify / compare
```

---
### 주의
- Dify API 스펙(엔드포인트·필드명)은 **Dify 버전별로 다를 수 있습니다** (교안 `[Week 0 검증 대상]`).
- 이 섹션은 `conversation_id` 관리·스트리밍을 다루지 않습니다 — **Section 13**의 주제입니다.
- **Spring AI 1.0.0 GA 기준** 코드입니다.
