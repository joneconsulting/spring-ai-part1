package com.example.springai.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
@Slf4j
public class ChatController {
    private final ChatClient openAiChatClient;
    private final ChatClient ollamaChatClient;

    public ChatController(@Qualifier("ollamaChatModel") ChatModel ollamaChatModel,
                          @Qualifier("openAiChatModel") ChatModel openAiChatModel) {
        this.openAiChatClient = ChatClient.builder(openAiChatModel).build();
        this.ollamaChatClient = ChatClient.builder(ollamaChatModel).build();
    }

    @GetMapping("/openai/ask")
    public String openAiAsk(@RequestParam String question) {
        log.info("[OpenAI model] q={}", question);
        return openAiChatClient.prompt().user(question).call().content();
    }

    @GetMapping("/ollama/ask")
    public String ollamaAsk(@RequestParam String question) {
        log.info("[Ollama Gemma2 model] q={}", question);
        return ollamaChatClient.prompt().user(question).call().content();
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam String question) {
        return openAiChatClient.prompt()
                .user(question)
                .stream()
                .content();
    }

}

