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

## 실행 순서

### 1) 적재
```bash
curl -X POST localhost:8080/api/rag/ingest
# {"documents":1,"chunks":7,"chunkSize":500}
```

### 2) 검색만 확인 (LLM 호출 없음) ⭐
```bash
curl "localhost:8080/api/rag/search?q=연차는 며칠인가요"
```
**교안 7장 10절의 습관** — LLM을 붙이기 전에 검색 품질부터 확인합니다.
`score`와 `text`를 보고 근거가 적합한지 육안 검증하세요.

### 3) RAG 질의응답
```bash
curl "localhost:8080/api/rag/ask?q=연차는 며칠이고 언제까지 써야 하나요"
```

### 4) RAG 미적용과 비교 ⭐
```bash
curl "localhost:8080/api/rag/ask-norag?q=연차는 며칠이고 언제까지 써야 하나요"
```

| 엔드포인트 | 기대 결과 |
|---|---|
| `/ask` | **"제12조에 따르면 15일…"** — 우리 문서의 조항을 인용 |
| `/ask-norag` | 일반론 또는 사실과 다른 답변 |

**이 대비가 RAG의 가치를 몸으로 이해하는 순간입니다.**

### 5) 문서에 없는 내용 물어보기 (환각 방어 확인)
```bash
curl "localhost:8080/api/rag/ask?q=회사 주차장은 몇 대까지 수용하나요"
# -> "제공된 문서에서 확인할 수 없습니다"
```

## 실습 과제

1. `application.yml`의 `rag.chunk-size`를 200 / 1000으로 바꿔 재적재 후 검색 결과 비교
2. `rag.top-k`를 1과 8로 바꿔 답변 차이 관찰
3. 본인 팀 위키 문서를 `src/main/resources/docs/`에 넣고 `IngestionService`에서 경로 변경

> ⚠️ 재적재 시 기존 데이터가 남아 중복됩니다. 실습 중 초기화하려면:
> ```bash
> docker exec -it rag-pgvector psql -U rag -d ragdb -c "TRUNCATE vector_store;"
> ```

## 트러블슈팅

| 증상 | 원인 | 해결 |
|---|---|---|
| dimension mismatch 오류 | yml `dimensions` ≠ 모델 출력 차원 | `text-embedding-3-small`=1536 확인, 테이블 재생성 |
| 검색 결과 0건 | 적재가 안 됐거나 빈 텍스트 저장 | `/api/rag/ingest` 응답의 chunks 수 확인 |
| 연결 실패 | pgvector 미기동 | `docker compose ps` 로 상태 확인 |
| 답변이 "확인할 수 없습니다"만 반복 | topK가 작음 | `rag.top-k` 상향 후 재시도 |

## 주요 파일

```
src/main/java/com/example/rag/
├── Section07Application.java
├── ingest/IngestionService.java   # Reader → Splitter → VectorStore.add()
└── api/RagController.java         # ingest / search / ask / ask-norag
src/main/resources/
├── application.yml
└── docs/company-policy.txt        # 강의용 가상 사내규정 (직접 작성, 저작권 이슈 없음)
```

---
### 주의
- **Spring AI 1.0.0 GA 기준** 코드입니다. 버전이 다르면 `QuestionAnswerAdvisor`, `SearchRequest`,
  `TokenTextSplitter`의 시그니처가 다를 수 있습니다 (교안의 `[Week 0 검증 대상]` 표시 참조).
- 적재 시 문서 크기에 비례해 임베딩 API 비용이 발생합니다.
