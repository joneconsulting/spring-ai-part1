# Section 12 — Dify 소개: Spring AI와의 하이브리드 (실행 예제)

Dify 자체는 **UI로 조작**하는 플랫폼입니다. 이 예제는 그 결과물(게시된 Dify 앱)을
**Spring이 호출**하고, **Spring AI 직접 구현과 나란히 비교**하는 데 집중합니다.

## 준비

```bash
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

### Dify 실습
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
