package com.example.springai.controllers;

import com.example.springai.services.MemoryChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * Section 4. ChatModel 심화 — 대화 기억 Web 계층 (슬라이드 S14·S16)
 *
 * Controller는 HTTP만 담당하고, 대화 로직은 MemoryChatService에 위임합니다.
 *
 *  - S14 동기 대화     → POST /api/chat/{conversationId}
 *  - S14 스트리밍 대화  → GET  /api/chat/{conversationId}/stream
 */
@RestController
@RequestMapping("/api/chat")
public class MemoryChatController {

    private final MemoryChatService memoryChatService;

    public MemoryChatController(MemoryChatService memoryChatService) {
        this.memoryChatService = memoryChatService;
    }

    /**
     * [S14] 동기 대화. 같은 conversationId로 보내면 이전 대화를 기억합니다.
     * 예)
     *   POST /api/chat/kenneth   body: "제 이름은 Kenneth입니다"
     *   POST /api/chat/kenneth   body: "제 이름이 뭐라고 했죠?"   → 기억!
     */
    @PostMapping("/{conversationId}")
    public String chat(@PathVariable String conversationId,
                       @RequestBody String message) {
        return memoryChatService.chat(conversationId, message);
    }

    /**
     * [S16] 스트리밍 대화 — SSE. 기억을 유지하면서 첫 토큰부터 흘려보냅니다.
     * 브라우저에서 직접 열거나 curl -N 으로 확인하세요.
     */
    @GetMapping(value = "/{conversationId}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@PathVariable String conversationId,
                               @RequestParam String message) {
        return memoryChatService.chatStream(conversationId, message);
    }
}
