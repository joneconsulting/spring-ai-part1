package com.example.dify.api;

import com.example.dify.client.DifyClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Section 12 실습 API — Spring AI vs Dify 비교 (교안 12장 4절)
 *
 *  GET /api/dify/ask?q=...       Dify 챗봇에 질문
 *  GET /api/springai/ask?q=...   Spring AI(직접 구현)에 질문
 *  GET /api/compare?q=...        같은 질문을 두 방식에 던져 나란히 비교 ★
 *
 * "우리가 4.5시간 들여 만든 것을 10분 만에?" 를 체험하고,
 * 판단 프레임(교안 12장 6절)으로 정리하는 것이 목적.
 */
@RestController
public class CompareController {

    private final ChatClient springAiClient;
    private final DifyClient difyClient;

    public CompareController(ChatClient.Builder builder, DifyClient difyClient) {
        this.springAiClient = builder
                .defaultSystem("너는 사내 규정 안내 도우미다. 한국어로 간결하게 답하라.")
                .build();
        this.difyClient = difyClient;
    }

    @GetMapping("/api/dify/ask")
    public Map<String, Object> difyAsk(@RequestParam String q,
                                       @RequestParam(defaultValue = "demo-user") String user) {
        DifyClient.DifyReply reply = difyClient.chat(q, user, "");
        return Map.of("engine", "Dify", "question", q,
                "answer", reply.answer(),
                "conversationId", reply.conversationId(),
                "mocked", reply.mocked());
    }

    @GetMapping("/api/springai/ask")
    public Map<String, Object> springAiAsk(@RequestParam String q) {
        String answer = springAiClient.prompt().user(q).call().content();
        return Map.of("engine", "Spring AI", "question", q, "answer", answer);
    }

    /** 같은 질문을 두 방식에 던져 나란히 비교 */
    @GetMapping("/api/compare")
    public Map<String, Object> compare(@RequestParam String q,
                                       @RequestParam(defaultValue = "demo-user") String user) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", q);

        long t1 = System.currentTimeMillis();
        String springAnswer = springAiClient.prompt().user(q).call().content();
        long springMs = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        DifyClient.DifyReply difyReply = difyClient.chat(q, user, "");
        long difyMs = System.currentTimeMillis() - t2;

        result.put("springAI", Map.of("answer", springAnswer, "latencyMs", springMs));
        result.put("dify", Map.of("answer", difyReply.answer(), "latencyMs", difyMs,
                "mocked", difyReply.mocked()));
        result.put("hint", "판단은 '누가 이겼나'가 아니라 '어떤 상황에 무엇이 유리한가' - 교안 12장 6절");
        return result;
    }
}
