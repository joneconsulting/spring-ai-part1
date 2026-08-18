package com.example.assistant.routing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * 경로 B — Dify 연동 엔진 (Section 12~13 통합)
 *
 * DIFY_API_KEY 가 없으면 mock, 호출 실패 시 Spring AI 로 폴백한다.
 */
@Component
public class DifyEngine {

    private static final Logger log = LoggerFactory.getLogger(DifyEngine.class);

    private final String apiKey;
    private final boolean mockWhenNoKey;
    private final RestClient restClient;
    private final ChatClient fallbackClient;

    public DifyEngine(@Value("${assistant.dify.base-url:https://api.dify.ai/v1}") String baseUrl,
                      @Value("${assistant.dify.api-key:}") String apiKey,
                      @Value("${assistant.dify.mock-when-no-key:true}") boolean mockWhenNoKey,
                      ChatClient.Builder builder) {
        this.apiKey = apiKey;
        this.mockWhenNoKey = mockWhenNoKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
        this.fallbackClient = builder
                .defaultSystem("너는 사내 규정 안내 도우미다. 한국어로 간결하게 답하라. (폴백)")
                .build();
    }

    private boolean isMock() {
        return (apiKey == null || apiKey.isBlank()) && mockWhenNoKey;
    }

    @SuppressWarnings("unchecked")
    public Answer ask(String question, String user) {
        if (isMock()) {
            return new Answer("[MOCK] Dify 경로 응답입니다. 질문: \"" + question + "\"", true, false);
        }
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("query", question);
            body.put("inputs", Map.of());
            body.put("response_mode", "blocking");
            body.put("user", user);
            body.put("conversation_id", "");

            Map<String, Object> res = restClient.post()
                    .uri("/chat-messages").body(body).retrieve().body(Map.class);
            String answer = res == null ? "" : String.valueOf(res.getOrDefault("answer", ""));
            return new Answer(answer, false, false);

        } catch (Exception e) {
            log.warn("[경로B] Dify 실패, Spring AI 폴백: {}", e.getMessage());
            String answer = fallbackClient.prompt().user(question).call().content();
            return new Answer(answer, false, true);
        }
    }

    public record Answer(String text, boolean mocked, boolean fellBack) {}
}
