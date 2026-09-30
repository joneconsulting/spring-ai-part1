package com.example.springai.controllers;

import com.example.springai.services.MemoryChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/chat")
public class MemoryChatController {

    private final MemoryChatService memoryChatService;

    public MemoryChatController(MemoryChatService memoryChatService) {
        this.memoryChatService = memoryChatService;
    }

    /*
     * POST /api/chat/kenneth       body: "제 이름 Kenneth입니다"
     * POST /api/chat/kenneth       body: "제 이름이 뭐라고 했죠?" -> 기억?
     */
    @PostMapping("/{conversationId}")
    public String chat(@PathVariable String conversationId,
                       @RequestBody String message) {
        return memoryChatService.chat(conversationId, message);
    }

    @GetMapping(value = "/{conversationId}/stream",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@PathVariable String conversationId,
                               @RequestParam String message) {
        return memoryChatService.chatStream(conversationId, message);
    }
}
