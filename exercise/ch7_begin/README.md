# Section 07 — Spring AI로 RAG 파이프라인 구축 (실행 예제)

문서 적재부터 질의응답까지 **end-to-end RAG**를 pgvector 기반으로 구현합니다.

## 준비

```bash
docker compose up -d          # pgvector 기동
export OPENAI_API_KEY=sk-...
./gradlew bootRun
```

> 로컬 5432 포트가 이미 사용 중이면 `docker-compose.yml`의 **호스트 쪽 포트만** 변경하세요.
> 예) `"15432:5432"` → `application.yml`의 datasource URL도 함께 수정
