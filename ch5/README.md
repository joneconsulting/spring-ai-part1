# spring-ai-prompts — Section 5 실습 예제

**Prompt Engineering** (Spring 개발자를 위한 실전 LLM 서비스 개발)

`S05_Prompt_Engineering.pptx`의 슬라이드 예제를 **코드·프롬프트 문구까지 그대로** 실행할 수 있게 구성했습니다.
클래스명(`TravelService`, `ChatService`)과 프롬프트 파일(`system.st`)도 슬라이드와 동일합니다.

## 실행

```bash
export OPENAI_API_KEY=sk-...        # IntelliJ는 Run·Test 설정 두 곳 모두
./mvnw spring-boot:run
```

`http/section5-prompt-engineering.http` 를 슬라이드 순서대로 실행하면 됩니다.

## 슬라이드 ↔ 코드 매핑

| 슬라이드 | 코드 | 엔드포인트 |
|---|---|---|
| S5 프롬프트가 품질을 결정 | `PromptQualityService#summaryBeforeAfter` | `/api/s05/quality/summary` |
| S6 역할 구조 | `PromptQualityService#roleSeparated` / `#roleSeparationAntiPattern` | `/api/s05/role/*` |
| S7 System Prompt 4요소 | `PromptQualityService#customerCenterBot` + `prompts/system-customer-center.st` | `/api/s05/system-prompt/*` |
| S8 PromptTemplate | **`TravelService`** (슬라이드 코드 그대로) | `/api/s05/template/travel*` |
| S9 외부 파일 관리 | **`ChatService`** + **`prompts/system.st`** (슬라이드 코드 그대로) | `/api/s05/template/chat*`, `/prompt-file` |
| S10·S11 Zero/Few-shot | `SentimentClassifyService` + `prompts/few-shot-sentiment.st` | `/api/s05/technique/*shot*` |
| S12 Chain-of-Thought | `ReasoningService` | `/api/s05/technique/cot/compare` |
| S13 기법 선택 치트시트 | `TechniqueController#cheatsheet` | `/api/s05/technique/cheatsheet` |
| S14·S15 인젝션·3중 방어 | `PromptInjectionDemoService` + `prompts/system-hardened-refund-bot.st` | `/api/s05/injection/defense` |
| S16 한국어 전략 | `KoreanPromptService` | `/api/s05/korean/*` |
| S17 실습 V1~V4 | `PromptVersionLabService` | `/api/s05/lab/prompt-versions` |

## resources/prompts — 재사용 PromptTemplate 모음 (섹션 결과물)

| 파일 | 출처 | 용도 |
|---|---|---|
| `system.st` | S9 슬라이드 원문 | 정책 근거 상담원 |
| `system-customer-center.st` | S7 슬라이드 예시 | 4요소(역할·형식·제약·톤) |
| `summary-v3.st` | S5·S17 | 경제 뉴스 에디터 요약 |
| `few-shot-sentiment.st` | S11 슬라이드 원문 | 감성 분류 Few-shot |
| `system-hardened-refund-bot.st` | S15 2차 방어층 | 인젝션 방어 규칙 |

## 강의 진행 노트

**S5** — Before/After를 라이브로 실행해 차이를 보여주면 도입부가 강해집니다. 응답에 프롬프트 원문이 함께 담겨 있어 "무엇을 추가했더니 이렇게 달라졌는지"가 화면에 그대로 보입니다.

**S8 → S9** — `travel/concat`(문자열 +)을 먼저 실행해 가독성·이스케이프 문제를 지적한 뒤 `travel`(템플릿)로 넘어가세요. S9의 `chat/raw`는 **슬라이드 코드 그대로**라서 `{service}`가 치환되지 않은 채 전달됩니다. 이 관찰을 발판으로 `chat`(바인딩 버전)을 보여주면 S8+S9 결합이 자연스럽게 완성됩니다.

**S10·S11** — 반드시 `zero-shot` → `few-shot` 순서로. Zero-shot의 "긍정적인 리뷰로 보입니다. 왜냐하면…" 장황한 응답을 먼저 보여줘야 한 단어 수렴의 임팩트가 삽니다.

**S14·S15** — 응답의 `inputBlocked` / `outputBlocked` 필드로 **어느 겹에서 걸렸는지**가 드러납니다. 1차 필터를 우회하는 변형 입력을 넣어 2·3차가 받아내는 장면까지 보여주면 "한 겹으로는 부족하다"가 증명됩니다. `defense/benign`으로 오탐 점검도 함께 하세요.

**S17** — `lengths` 필드에 회차별 응답 글자 수가 담겨 있어 **분량 준수 편차**를 즉시 비교할 수 있습니다. temperature는 `application.yml`에 0.3으로 고정되어 있습니다(슬라이드 권장값).

## ⚠️ 호출 횟수

| 엔드포인트 | 호출 |
|---|---|
| `/lab/prompt-versions?runs=3` | **12회** |
| `/quality/summary`, `/*/compare`, `/korean/*` | 각 2회 |
| `/injection/defense` | 1~2회 (1차에서 차단되면 1회) |
| 그 외 | 1회 |

## 검증 필요 항목 `[Week 0]`

- `pom.xml` — spring-ai 버전 (1.0.0 기준)
- `application.yml` — 모델명
- `PromptTemplate(Resource)` 및 `render(Map)` 시그니처 — GA 기준 재확인
- S14 인젝션 데모는 **모델에 따라 방어될 수 있음** — 슬라이드 노트대로 "모델 의존 방어는 보장이 아니다"의 교훈으로 활용

## 다음 섹션

Section 6부터 RAG 구간(6~9)이 시작됩니다. 이 섹션의 결과물(재사용 PromptTemplate 모음, V1→V4 비교표, 보관해 둔 인젝션 공격문)이 이후 섹션의 재료가 됩니다.
