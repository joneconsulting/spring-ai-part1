# Section 15 — 미니 프로젝트: 사내 문서 AI Assistant (실행 예제)

16시간의 조각들을 **하나의 서비스**로 통합합니다.
경로 A(Spring AI 직접 구현)와 경로 B(Dify)를 한 애플리케이션에 담고, **가드레일을 최상단**에 두며,
**같은 질문을 A/B로 던져 비교**합니다.

## 준비

```bash
docker compose up -d
export OPENAI_API_KEY=sk-...
# 경로 B(Dify) 연동은 선택 — 없으면 mock
export DIFY_API_KEY=app-xxxxxxxx     # (선택)
./gradlew bootRun
```

---

## 최종 아키텍처

```
┌──────────────────────────────────────────────┐
│  Spring Boot — 가드레일 · 라우팅 · 관측성     │
└──────────────────────────────────────────────┘
        │                        │
   경로 A (Spring AI)       경로 B (Dify)
   pgvector RAG + 근거      Knowledge 챗봇 (mock/폴백)
```

**핵심 설계**: 같은 질문을 A/B 어느 경로로도 보낼 수 있습니다.
이 구조 자체가 **비교 실험의 도구**이자 **장애 시 이중화**입니다.

