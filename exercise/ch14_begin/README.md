# Section 14 — 프로덕션 관점: 가드레일 · 관측성 (실행 예제)

**"동작한다"와 "운영할 수 있다"는 다릅니다.** 3중 가드레일 Advisor, PII 마스킹,
Micrometer 관측성을 실제로 구현합니다.

## 준비

```bash
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

