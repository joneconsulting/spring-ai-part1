package com.example.dify.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Dify Chat API 호출 클라이언트 (교안 12장)
 *
 * Section 12는 Dify를 "소개"하는 섹션이므로, 여기서는 blocking 모드 기본 호출만 다룬다.
 * (conversation_id 관리·스트리밍 프록시는 Section 13에서 본격적으로 구현)
 *
 * ★ mock 폴백: DIFY_API_KEY 가 없으면 실제 호출 대신 mock 응답을 반환한다.
 *   Dify를 아직 띄우지 않은 수강생도 "Spring이 Dify를 부르는 흐름"을 확인할 수 있다.
 */
@Component
public class DifyClient {

    private static final Logger log = LoggerFactory.getLogger(DifyClient.class);

    private final String baseUrl;
    private final String apiKey;
    private final boolean mockWhenNoKey;
    private final RestClient restClient;

    public DifyClient(@Value("${dify.base-url}") String baseUrl,
                      @Value("${dify.api-key:}") String apiKey,
                      @Value("${dify.mock-when-no-key:true}") boolean mockWhenNoKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.mockWhenNoKey = mockWhenNoKey;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public boolean isMockMode() {
        return (apiKey == null || apiKey.isBlank()) && mockWhenNoKey;
    }

    /**
     * Dify chat-messages 호출 (blocking).
     * @param query 질문
     * @param user  사용자 식별자 (우리 시스템의 userId 를 매핑)
     * @param conversationId 대화 연속성 키 (빈 값이면 새 대화)
     */
    @SuppressWarnings("unchecked")
    public DifyReply chat(String query, String user, String conversationId) {
        if (isMockMode()) {
            log.warn("[Dify] mock 모드 - DIFY_API_KEY 미설정. 실제 Dify 를 호출하지 않습니다.");
            return mockReply(query, conversationId);
        }

        Map<String, Object> body = new HashMap<>();
        body.put("query", query);
        body.put("inputs", Map.of());
        body.put("response_mode", "blocking");
        body.put("user", user);
        body.put("conversation_id", conversationId == null ? "" : conversationId);

        log.info("[Dify] chat-messages 호출 - user={}, convId='{}'", user, conversationId);

        Map<String, Object> res = restClient.post()
                .uri("/chat-messages")
                .body(body)
                .retrieve()
                .body(Map.class);

        String answer = res == null ? "" : String.valueOf(res.getOrDefault("answer", ""));
        String convId = res == null ? "" : String.valueOf(res.getOrDefault("conversation_id", ""));
        return new DifyReply(answer, convId, false);
    }

    private DifyReply mockReply(String query, String conversationId) {
        String convId = (conversationId == null || conversationId.isBlank())
                ? "mock-conv-" + Math.abs(query.hashCode() % 100000)
                : conversationId;
        String answer = "[MOCK] Dify 응답 예시입니다. 질문: \"" + query + "\". "
                + "실제 응답을 받으려면 DIFY_API_KEY 를 설정하세요.";
        return new DifyReply(answer, convId, true);
    }

    /** Dify 응답 (answer + 대화 상태 + mock 여부) */
    public record DifyReply(String answer, String conversationId, boolean mocked) {}
}
