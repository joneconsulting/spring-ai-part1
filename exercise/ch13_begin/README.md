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

