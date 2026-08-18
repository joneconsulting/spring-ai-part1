package com.example.production.api;

import com.example.production.guardrail.Detectors;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Section 14 실습 API
 *
 *  GET /api/chat?q=...        가드레일이 적용된 ChatClient
 *  GET /api/mask?text=...     PII 마스킹 데모 (로깅 안전화)
 *
 * 관측: /actuator/metrics/guardrail.blocked 에서 차단 건수를 확인 (교안 14장 1·5절)
 */
@RestController
public class ChatController {

    private final ChatClient guardedChatClient;
    private final Detectors.PiiDetector piiDetector;
    private final MeterRegistry metrics;

    public ChatController(ChatClient guardedChatClient,
                          Detectors.PiiDetector piiDetector,
                          MeterRegistry metrics) {
        this.guardedChatClient = guardedChatClient;
        this.piiDetector = piiDetector;
        this.metrics = metrics;
    }

    @GetMapping("/api/chat")
    public Map<String, Object> chat(@RequestParam String q) {
        // 요청 카운터 (관측성)
        metrics.counter("ai.requests", "endpoint", "chat").increment();

        String answer = guardedChatClient.prompt().user(q).call().content();
        return Map.of("question", q, "answer", answer);
    }

    /** PII 마스킹 데모 — 로그에 남기기 전에 민감정보를 가린다 (교안 14장 2절) */
    @GetMapping("/api/mask")
    public Map<String, Object> mask(@RequestParam String text) {
        return Map.of(
                "original_containsPii", piiDetector.contains(text),
                "masked", piiDetector.mask(text));
    }
}
