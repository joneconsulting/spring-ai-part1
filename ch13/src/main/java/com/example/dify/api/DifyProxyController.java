package com.example.dify.api;

import com.example.dify.client.DifyChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * Section 13 실습 API — 프로덕션 패턴 Dify 프록시
 *
 *  POST /api/dify/chat            blocking 대화 (conversation_id 유지)
 *  GET  /api/dify/chat/stream     SSE 스트리밍 프록시 ★
 *  POST /api/dify/reset           새 대화 시작
 *
 * 교안 5장: 프론트가 Dify 를 직접 부르면 안 된다.
 *              Spring 이 인증·비용통제·로깅·폴백을 담당한다.
 */
@RestController
@RequestMapping("/api/dify")
public class DifyProxyController {

    private final DifyChatService chatService;

    public DifyProxyController(DifyChatService chatService) {
        this.chatService = chatService;
    }

    /** blocking 대화 — 검증 포인트: 두 번째 질문에서 "아까 그거"가 통하는가 */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody ChatRequest req) {
        DifyChatService.ChatResult result = chatService.chat(req.userId(), req.message());
        return Map.of(
                "answer", result.answer(),
                "conversationId", result.conversationId(),
                "mocked", result.mocked(),
                "fellBack", result.fellBack());
    }

    /** SSE 스트리밍 프록시 (교안 10장 STEP 3) */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String userId,
                                   @RequestParam String message) {
        return chatService.chatStream(userId, message);
    }

    /** 새 대화 시작 — 저장된 conversation_id 삭제 */
    @PostMapping("/reset")
    public Map<String, Object> reset(@RequestParam String userId) {
        chatService.resetConversation(userId);
        return Map.of("userId", userId, "status", "conversation reset");
    }

    public record ChatRequest(String userId, String message) {}
}
