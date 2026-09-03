# Section 13 — Spring Boot × Dify 연동 실전 (실행 예제)

Dify를 **백엔드 AI 엔진**으로 두고 Spring이 프록시하는 **프로덕션 패턴**을 구현합니다.
`conversation_id` 관리, SSE 스트리밍 프록시, 장애 시 폴백까지 다룹니다.

## 준비

```bash
export OPENAI_API_KEY=sk-...
# Dify 연동 (선택 — 없으면 mock/폴백으로 동작)
export DIFY_BASE_URL=https://api.dify.ai/v1
export DIFY_API_KEY=app-xxxxxxxx
./gradlew bootRun
```

> ⚠️ **DIFY_API_KEY가 없어도 실습이 진행됩니다.**
> - blocking 호출: mock 응답 (`"mocked": true`)
> - Dify 호출 실패 시: **Spring AI로 자동 폴백** (`"fellBack": true`)

---

## 실행

### 1) blocking 대화 + conversation_id 검증 ⭐
```bash
# 첫 질문
curl -X POST localhost:8080/api/dify/chat \
  -H 'Content-Type: application/json' \
  -d '{"userId":"user-1","message":"연차는 며칠인가요?"}'

# 두 번째 질문 — "아까 말한 그거"가 통하는가? (conversation_id 재사용)
curl -X POST localhost:8080/api/dify/chat \
  -H 'Content-Type: application/json' \
  -d '{"userId":"user-1","message":"그럼 언제까지 써야 하나요?"}'
```
**응답의 `conversationId`가 두 요청에서 동일하게 유지**되면 성공입니다 (교안 10장 STEP 2).

### 2) 새 대화 시작
```bash
curl -X POST "localhost:8080/api/dify/reset?userId=user-1"
# 이후 질문은 새 conversation_id 로 시작
```

### 3) SSE 스트리밍 프록시 ⭐ (교안 10장 STEP 3)
```bash
curl -N "localhost:8080/api/dify/chat/stream?userId=user-1&message=연차 규정 설명해줘"

(오류 발생 시)
curl -N -G "http://localhost:8080/api/dify/chat/stream" \
  --data-urlencode "userId=user-1" \
  --data-urlencode "message=연차 규정 설명해줘"
```
> `-N` 옵션으로 버퍼링 없이 토큰이 조각으로 도착하는 것을 확인합니다.
> mock 모드에서는 예시 조각들이 스트리밍됩니다.

### 4) 폴백 동작 검증 (교안 10장 STEP 4)
```bash
# 일부러 잘못된 키로 기동
DIFY_API_KEY=wrong-key ./gradlew bootRun

curl -X POST localhost:8080/api/dify/chat \
  -H 'Content-Type: application/json' \
  -d '{"userId":"user-1","message":"연차는 며칠인가요?"}'
# → Dify 호출 실패 → Spring AI 로 폴백 → "fellBack": true
```

---

## 아키텍처 (교안 10장)

```
브라우저 ──→ Spring Boot ──→ Dify API
                 │
                 └─(장애 시)→ Spring AI (폴백)
```

**Spring 계층이 담당하는 것**: 인증 · 비용통제 · 로깅 · 업무로직 · **폴백** · 응답 가공
→ AI 시대에도 백엔드 개발자의 자리입니다.

## 핵심 구현

| 파일 | 역할 |
|---|---|
| `ConversationSessionRepository` | userId ↔ conversation_id 매핑 (실무는 DB/Redis) |
| `DifyProxyClient` | blocking(RestClient) + streaming(WebClient/Flux) + 폴백 |
| `DifyChatService` | conversation_id 조회·저장 캡슐화 |
| `DifyProxyController` | `/chat` · `/chat/stream` · `/reset` |

## 실습 과제

1. **세션 저장소를 DB로**: `ConversationSessionRepository`를 JPA 또는 Redis로 교체
   (교안 10장 — 인스턴스 스케일아웃 대비)
2. **SSE 이벤트 파싱**: `DifyProxyClient.extractAnswerChunk()`를 실제 Dify SSE 포맷에 맞게 구현
   (교안 10장 함정 1 — message 외 타입 필터링장
3. **쿼터 추가**: 사용자별 일일 호출 한도를 Spring 계층에 추가 (비용 통제)

## 주요 파일
```
src/main/java/com/example/dify/
├── session/ConversationSessionRepository.java   # conversation_id 매핑 ★
├── client/DifyProxyClient.java                  # blocking+streaming+폴백
├── client/DifyChatService.java                  # 대화 상태 캡슐화
└── api/DifyProxyController.java                  # chat / chat/stream / reset
```

---
### 주의
- Dify API 스펙(SSE 이벤트 구조, 필드명)은 **버전별로 다릅니다** (교안 `[Week 0 검증 대상]`).
- `extractAnswerChunk()`는 데모용 단순 구현입니다. 실제로는 JSON 파싱이 필요합니다.
- **Spring AI 1.0.0 GA · Spring Boot 3.4 기준** 코드입니다.
