package com.example.dify.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Dify 프록시 클라이언트 (교안 13장 2·4·5절)
 *
 * 왜 Spring 이 중간에 있는가 (교안 13장 1절):
 *   - API 키를 서버에만 보관 (프론트 노출 방지)
 *   - 인증·비용통제·로깅·폴백을 Spring 이 담당
 *
 * 이 클래스가 담당하는 것:
 *   - blocking 호출 (RestClient) — conversation_id 왕복
 *   - streaming 호출 (WebClient/Flux) — SSE 프록시
 *   - Dify 장애 시 Spring AI 로 폴백
 */
@Component
public class DifyProxyClient {

    private static final Logger log = LoggerFactory.getLogger(DifyProxyClient.class);

    private final String apiKey;
    private final boolean mockWhenNoKey;
    private final int timeoutSeconds;
    private final RestClient restClient;
    private final WebClient webClient;
    private final ChatClient fallbackClient;   // 폴백용 Spring AI

    public DifyProxyClient(@Value("${dify.base-url}") String baseUrl,
                           @Value("${dify.api-key:}") String apiKey,
                           @Value("${dify.mock-when-no-key:true}") boolean mockWhenNoKey,
                           @Value("${dify.timeout-seconds:60}") int timeoutSeconds,
                           ChatClient.Builder builder) {
        this.apiKey = apiKey;
        this.mockWhenNoKey = mockWhenNoKey;
        this.timeoutSeconds = timeoutSeconds;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.fallbackClient = builder
                .defaultSystem("너는 사내 규정 안내 도우미다. 한국어로 간결하게 답하라. (폴백 엔진)")
                .build();
    }

    public boolean isMockMode() {
        return (apiKey == null || apiKey.isBlank()) && mockWhenNoKey;
    }

    /** blocking 호출 — 교안 13장 3절 (conversation_id 왕복) */
    @SuppressWarnings("unchecked")
    public DifyReply chat(String query, String user, String conversationId) {
        if (isMockMode()) {
            return mockReply(query, conversationId);
        }
        try {
            Map<String, Object> body = requestBody(query, user, conversationId, "blocking");
            Map<String, Object> res = restClient.post()
                    .uri("/chat-messages")
                    .body(body)
                    .retrieve()
                    .body(Map.class);

            String answer = res == null ? "" : String.valueOf(res.getOrDefault("answer", ""));
            String convId = res == null ? "" : String.valueOf(res.getOrDefault("conversation_id", ""));
            return new DifyReply(answer, convId, false, false);

        } catch (Exception e) {
            // 교안 13장 5절 — Dify 장애 시 Spring AI 로 폴백
            log.warn("[Dify] 호출 실패, Spring AI 로 폴백합니다: {}", e.getMessage());
            String answer = fallbackClient.prompt().user(query).call().content();
            return new DifyReply(answer, conversationId == null ? "" : conversationId, false, true);
        }
    }

    /** streaming 호출 — 교안 13장 4절 (SSE 프록시) */
    public Flux<String> chatStream(String query, String user, String conversationId) {
        if (isMockMode()) {
            return Flux.just("[MOCK] ", "스트리밍 ", "응답 ", "예시입니다. ",
                    "DIFY_API_KEY 를 설정하면 실제 SSE 를 중계합니다.");
        }
        Map<String, Object> body = requestBody(query, user, conversationId, "streaming");

        return webClient.post()
                .uri("/chat-messages")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(String.class)          // Dify SSE 수신
                .map(this::extractAnswerChunk)     // 필요한 조각만 추출 (교안 13장 4절 함정 1)
                .doOnNext(chunk -> log.debug("[Dify SSE] {}", chunk))   // 로깅 (마스킹 지점)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .onErrorResume(e -> {              // 폴백
                    log.warn("[Dify SSE] 실패, 폴백 응답: {}", e.getMessage());
                    return Flux.just("[폴백] 일시적으로 응답을 가져올 수 없습니다.");
                });
    }

    // Dify SSE 이벤트에는 message 외 타입도 섞여 온다 (교안 13장 4절 함정 1)
    // 실제 구현에서는 JSON 파싱 후 event=="message" 인 것의 answer 필드만 추출한다.
    private String extractAnswerChunk(String raw) {
        // 데모용 단순 추출 — GA 에서는 ObjectMapper 로 파싱
        return raw;
    }

    private Map<String, Object> requestBody(String query, String user, String conversationId, String mode) {
        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("inputs", Map.of());
        body.put("response_mode", mode);
        body.put("user", user);
        body.put("conversation_id", conversationId == null ? "" : conversationId);
        return body;
    }

    private DifyReply mockReply(String query, String conversationId) {
        String convId = (conversationId == null || conversationId.isBlank())
                ? "mock-conv-" + Math.abs(query.hashCode() % 100000)
                : conversationId;
        return new DifyReply(
                "[MOCK] Dify 응답입니다. 질문: \"" + query + "\" (convId=" + convId + ")",
                convId, true, false);
    }

    /** Dify 응답 (answer + 대화상태 + mock/폴백 여부) */
    public record DifyReply(String answer, String conversationId, boolean mocked, boolean fellBack) {}
}
