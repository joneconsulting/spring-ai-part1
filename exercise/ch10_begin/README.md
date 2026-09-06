# Section 10 — Tool Calling과 MCP (실행 예제)

LLM에게 **손과 발**을 달아줍니다. Part 1은 `@Tool` 로 자바 메서드를 노출하고,
Part 2는 MCP 클라이언트로 외부 도구를 연결합니다.

## 준비

```bash
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```
> DB가 필요 없습니다. Part 1(Tool Calling)만 실습한다면 Node.js도 필요 없습니다.

