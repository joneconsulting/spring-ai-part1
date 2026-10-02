# ch4 × TypeSafe Jev 코드 변경 검토

> 작성일: 2026-09-30 · 대상: `ch4` (Section 04. 구조화된 출력 + 대화 기억)
> 상태: **검토용 제안서** — 이 문서의 코드는 아직 프로젝트에 반영되지 않았습니다.
> Jev 자체 소개와 HTTP API 형식은 [`../ch3/README.md`](../ch3/README.md) 2장을 참고하세요.

---

## 1. 결론 요약

ch4는 "LLM 응답을 Java 타입으로 받기"(`entity()`)가 주제라서 **ch3보다 Jev와 궁합이 좋습니다.**
다만 Jev는 **세상 지식이 없고 글을 쓰지 못하므로** 기존 영화 조회 기능(`findMovie` 등)을 대체할 수는 없습니다.
Jev는 기존 코드를 바꾸기보다 **"비교용 예제"를 추가**하는 방식이 맞습니다.

| # | 변경 위치 | 내용 | 우선순위 |
|---|---|---|---|
| ① | `MovieService` / `MovieServiceImpl` / `MovieController` | 영화 리뷰 분석: **LLM `entity()` 버전 vs Jev 버전** 나란히 비교 | ★★★ 권장 |
| ② | `MemoryChatService` | 사용자가 "대화 초기화"를 원하는지 Jev가 판단 → `ChatMemory.clear()` | ★★ 선택 |
| ③ | 신규 `jev` 패키지, `application.yml`, `SpringAiApplication` | Jev 호출 공통 코드 (①② 공통 전제) | 필수 |
| – | `findMovie`, `findMoviesByDirector`, `raw-json-*`, `format` | **변경하지 않음** | – |

---

## 2. 왜 ch4와 잘 맞는가 — 강의 흐름상 위치

ch4의 기존 흐름(S8~S10)은 이렇습니다.

```
S8  entity()로 record 매핑            → 편하다
S9  List<Movie> 매핑                   → 제네릭도 된다
S10 함정: 문자열 JSON은 ```json 펜스로 깨질 수 있다
    대응①: "without markdown tags" 프롬프트
    대응②: entity() 사용 (스키마 지시문 자동 주입)
```

여기에 한 단계를 덧붙일 수 있습니다.

```
    대응③ (신규): 형식이 "부탁"이 아니라 "구조적으로 보장"되는 모델 → Jev
```

- `entity()`는 LLM에게 **"이 JSON 스키마대로 써 주세요"라고 부탁**하는 방식입니다. 대부분 잘 되지만, 모델이 스키마를 어기면 파싱 예외가 납니다. `enum` 필드에 목록에 없는 값이 올 수도 있습니다.
- Jev는 처음부터 **보기 중에서만 고르기 때문에** 형식 오류가 날 수 없습니다. 틀린 보기를 고를 수는 있지만, 없는 값을 만들어내지는 못합니다.

→ "구조화된 출력을 얻는 두 가지 철학"을 같은 입력으로 비교해 보여줄 수 있습니다.

---

## 3. 변경 ③ (공통 전제) — Jev 호출 코드

### `application.yml` (추가)

```yaml
jev:
  enabled: ${JEV_ENABLED:false}        # 기본 off → 키 없는 수강생도 기존 실습 그대로 진행
  api-key: ${TYPESAFE_API_KEY:}
  base-url: https://api.typesafe.ai
  model: jev-1.13.0                    # 녹화 시점 버전 고정
```

### `SpringAiApplication.java` (수정)

```java
@SpringBootApplication
@ConfigurationPropertiesScan          // ← 추가
public class SpringAiApplication { ... }
```

### `jev/JevProperties.java` (신규)

```java
package com.example.springai.jev;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jev")
public record JevProperties(boolean enabled, String apiKey, String baseUrl, String model) {
}
```

### `jev/JevClient.java` (신규)

ch3 초안은 "질문 분류" 전용이었지만, ch4에서는 여러 질문을 한 번에 보낼 수 있도록 **범용 메서드**로 만듭니다.

```java
package com.example.springai.jev;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class JevClient {
    private final RestClient restClient;
    private final JevProperties props;

    public JevClient(JevProperties props) {
        this.props = props;
        this.restClient = RestClient.builder()
                .baseUrl(props.baseUrl())
                .defaultHeader("Authorization", "Bearer " + props.apiKey())
                .build();
    }

    public boolean enabled() {
        return props.enabled();
    }

    /** state 하나에 여러 질문을 보내고, 질문 key별 결정을 돌려받습니다. */
    public Map<String, Decision> decide(Object state, Map<String, Object> questions) {
        JevResponse response = restClient.post()
                .uri("/v1/systemone")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("model", props.model(), "state", state, "questions", questions))
                .retrieve()
                .body(JevResponse.class);
        return response.answers();
    }

    public record JevResponse(String model, Map<String, Decision> answers) {
    }

    /** 질문 타입에 따라 채워지는 필드가 다릅니다 (choice / score / noul). */
    public record Decision(String type, String choice, Double score, Double noul,
                           Double confidence, Map<String, Double> probabilities) {
    }
}
```

---

## 4. 변경 ① (권장) — 영화 리뷰 분석: `entity()` vs Jev

### 왜 "리뷰 분석"인가

기존 `Movie(title, year, director)`는 **사실 조회**라서 세상 지식이 필요합니다. Jev는 이 일을 할 수 없습니다.
반면 **"주어진 리뷰 글을 판정하는 일"** 은 Jev가 가장 잘하는 일입니다.
답이 입력 안에 있고, 결과가 정해진 보기(장르, 스포일러 여부, 평점)이기 때문입니다.

### 신규 모델

**`model/Genre.java`**

```java
package com.example.springai.model;

public enum Genre {
    DRAMA("인물과 관계 중심의 드라마"),
    THRILLER("긴장감과 반전 중심의 스릴러"),
    COMEDY("웃음을 주는 코미디"),
    ACTION("액션과 볼거리 중심"),
    SF("과학·미래 설정의 SF"),
    ROMANCE("사랑 이야기 중심의 로맨스");

    private final String description;
    Genre(String description) { this.description = description; }
    public String description() { return description; }
}
```

**`model/ReviewAnalysis.java`**

```java
package com.example.springai.model;

public record ReviewAnalysis(
        Genre genre,              // 리뷰로 추정한 장르
        double sentiment,         // 0(매우 부정) ~ 4(매우 긍정)
        boolean spoiler,          // 스포일러 포함 여부
        Double confidence         // Jev만 제공 (LLM 버전은 null)
) {
}
```

### `MovieService` (메서드 2개 추가)

```java
ReviewAnalysis analyzeReviewWithLlm(String review);

ReviewAnalysis analyzeReviewWithJev(String review);
```

### `MovieServiceImpl` (추가)

```java
/**
 * [S10-대응②와 비교] LLM + entity() — 스키마를 "부탁"하는 방식.
 * 대부분 잘 되지만 enum에 없는 값이 오면 파싱 예외가 날 수 있습니다.
 */
public ReviewAnalysis analyzeReviewWithLlm(String review) {
    return chatClient.prompt()
            .user(u -> u.text("""
                    다음 영화 리뷰를 분석해줘.
                    sentiment는 0(매우 부정)~4(매우 긍정), confidence는 null로 둬.
                    리뷰: {review}
                    """).param("review", review))
            .call()
            .entity(ReviewAnalysis.class);
}

/**
 * [S10-대응③] Jev — 보기 중에서만 고르므로 형식 오류가 구조적으로 불가능.
 * criteria를 enum에서 만들기 때문에 응답은 항상 Genre.valueOf()로 변환됩니다.
 */
public ReviewAnalysis analyzeReviewWithJev(String review) {
    Map<String, String> genreCriteria = new LinkedHashMap<>();
    for (Genre g : Genre.values()) {
        genreCriteria.put(g.name(), g.description());
    }

    Map<String, JevClient.Decision> answers = jevClient.decide(review, Map.of(
            "genre", Map.of("type", "choice",
                    "instructions", "리뷰가 다루는 영화의 장르는?",
                    "criteria", genreCriteria),
            "sentiment", Map.of("type", "score",
                    "instructions", "리뷰 작성자의 영화 평가는?",
                    "criteria", List.of("매우 부정", "부정", "보통", "긍정", "매우 긍정")),
            "spoiler", Map.of("type", "noul",
                    "instructions", "리뷰가 결말이나 반전을 드러내는가?")));

    JevClient.Decision genre = answers.get("genre");
    return new ReviewAnalysis(
            Genre.valueOf(genre.choice()),
            answers.get("sentiment").score(),
            answers.get("spoiler").noul() >= 0.5,
            genre.confidence());
}
```

→ 생성자에 `JevClient` 주입 추가 필요.

### `MovieController` (엔드포인트 2개 추가)

```java
/** [S10-비교] 같은 리뷰를 LLM entity()로 분석. 예) POST /api/movies/review/llm */
@PostMapping("/review/llm")
public ReviewAnalysis reviewLlm(@RequestBody String review) {
    return movieService.analyzeReviewWithLlm(review);
}

/** [S10-비교] 같은 리뷰를 Jev로 분석. jev.enabled=false면 503 */
@PostMapping("/review/jev")
public ReviewAnalysis reviewJev(@RequestBody String review) {
    if (!jevClient.enabled()) {
        throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "jev.enabled=false");
    }
    return movieService.analyzeReviewWithJev(review);
}
```

### 강의 시연 포인트

| 관찰 항목 | `/review/llm` | `/review/jev` |
|---|---|---|
| 응답 시간 | 수 초 | 70~500ms |
| `genre`에 목록 밖 값 | 가능 (예: `"CRIME"` → 파싱 예외) | 불가능 |
| 확신도 | 없음 (`null`) | `confidence` 제공 |
| 반복 호출 시 일관성 | `temperature: 1.2`라 흔들림이 큼 | 확률 기반이라 안정적 |
| 비용 | 입력+출력 과금 | 입력만 과금 |

> 참고: 현재 `application.yml`의 `temperature: 1.2`는 높은 편이라, LLM 버전의 흔들림이 더 잘 드러납니다. 비교 시연에는 오히려 유리합니다.

---

## 5. 변경 ② (선택) — 대화 초기화 의도 감지

### 아이디어

`MemoryChatService`는 `conversationId`별로 최근 10개 메시지를 기억합니다(S13·S14).
사용자가 "처음부터 다시 하자", "지금까지 얘기 잊어줘"라고 해도, 지금은 LLM이 **"잊었다"고 말만 할 뿐 실제 메모리는 그대로 남습니다.**
Jev가 이 의도를 판단하면 **실제로 `ChatMemory.clear()`를 호출**할 수 있습니다.

→ "LLM의 말"과 "프로그램의 동작"의 차이를 보여주는 좋은 예시이며, S13(윈도우·메모리 관리) 설명과 이어집니다.

### `MemoryChatService` (수정)

```java
private final ChatClient chatClient;
private final ChatMemory chatMemory;     // ← 추가
private final JevClient jevClient;       // ← 추가

public MemoryChatService(@Qualifier("memoryChatClient") ChatClient chatClient,
                         ChatMemory chatMemory, JevClient jevClient) { ... }

public String chat(String conversationId, String message) {
    if (wantsReset(message)) {
        chatMemory.clear(conversationId);
        return "대화 기록을 초기화했습니다. 새로 시작해 볼까요?";
    }
    return chatClient.prompt() ... // 기존 코드 그대로
}

/** Jev noul: 사용자가 대화 초기화를 요청하는가? (0.8 이상일 때만 실행) */
private boolean wantsReset(String message) {
    if (!jevClient.enabled()) {
        return false;
    }
    try {
        JevClient.Decision d = jevClient.decide(message, Map.of(
                "reset", Map.of("type", "noul",
                        "instructions", "사용자가 지금까지의 대화를 잊고 새로 시작하자고 요청하는가?")))
                .get("reset");
        return d.noul() >= 0.8;
    } catch (RestClientException e) {
        return false;   // Jev 장애가 대화를 막지 않도록
    }
}
```

`chatStream`에도 같은 검사를 넣을 경우 `Flux.just("대화 기록을 초기화했습니다...")`를 반환합니다.

### 주의

- **모든 메시지마다** Jev 호출이 1회 추가됩니다(수백 ms). 체감 지연이 문제라면 ① 한 가지만 반영하는 것을 권장합니다.
- 임계값 0.8은 오작동(일반 대화를 초기화 요청으로 오인)을 줄이기 위한 보수적인 값입니다. 녹화 전에 예문 몇 개로 확인이 필요합니다.

---

## 6. 변경하지 않는 부분과 이유

| 메서드 | 이유 |
|---|---|
| `findMovie`, `findMoviesByDirector` | 영화 제목·연도·감독을 **알아내는** 일은 세상 지식이 필요 → Jev 불가 |
| `askRawJsonTrap`, `askRawJsonFixed` | LLM의 함정을 **보여주려고 일부러 만든** 예제이므로 유지 |
| `describeFormat` | `entity()` 내부 동작 설명용. Jev와 무관 |
| `ChatConfig` | 메모리 설정. ②를 넣어도 수정 불필요 (`ChatMemory` 빈을 주입만 받음) |

---

## 7. 반영 전 확인할 점

1. **기존 테스트 컴파일 오류 (Jev와 무관한 선행 이슈)**
   `src/test/.../OpenAIServiceImplTest.java`가 ch3의 `OpenAIServiceImpl`을 참조하지만, ch4에는 이 클래스가 없습니다. 그래서 `./mvnw test`가 컴파일 단계에서 실패합니다. Jev 반영과 별개로 삭제하거나 `MovieServiceImpl` 테스트로 바꿔야 합니다.
2. **키 발급 장벽**: Jev는 얼리 액세스 대기열 방식입니다. `jev.enabled=false`를 기본으로 두어 키가 없어도 기존 실습이 그대로 동작해야 합니다.
3. **exercise 동기화**: `exercise/ch4_begin` 베이스 코드에 `Genre`, `ReviewAnalysis`, `jev` 패키지를 미리 넣을지, 수강생이 직접 작성할지 결정해야 합니다.
4. **ch3와 중복**: ch3에도 Jev를 넣는다면 `JevClient`를 두 번 설명하게 됩니다. **Jev는 ch4에서 처음 소개**하는 편이 커리큘럼상 자연스럽습니다(ch3 README의 "ch4로 미루기" 안과 같은 결론).
5. **미검증 초안**: 이 문서의 코드는 컴파일·실행하지 않았습니다. 특히 Spring Boot 4 / Jackson 3에서 `Decision` record의 `null` 필드 역직렬화는 실제 호출로 확인이 필요합니다.
6. **pom 버전 차이**: ch3는 Spring Boot `4.1.0`, ch4는 `4.0.6`입니다. Jev 반영과는 무관하지만, 챕터 간에 맞출지 확인이 필요합니다.

---

## 8. 결정 체크리스트

- [ ] Jev 첫 소개 챕터: ch3 / **ch4 (권장)**
- [ ] 반영 범위: **① 리뷰 비교만 (권장)** / ① + ② 대화 초기화
- [ ] `exercise/ch4_begin`에 Jev 코드를 미리 넣을지
- [ ] `OpenAIServiceImplTest` 처리 방법 (삭제 / 교체)
- [ ] 녹화 시점의 Jev 모델 버전 고정값

---

## 출처

- [TypeSafe Docs — HTTP API reference](https://docs.typesafe.ai/api.md)
- [TypeSafe Docs — Primitives (choice / score / noul)](https://docs.typesafe.ai/primitives.md)
- [Introducing System One Models & Jev — TypeSafe AI Blog](https://typesafe.ai/blog/introducing-system-one-models-and-jev)
- [How to Use Jev: A practical guide — DEV Community](https://dev.to/valyuai/how-to-use-jev-a-practical-guide-to-typesafes-system-one-model-g5e)
