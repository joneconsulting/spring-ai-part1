package com.example.springai.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

/**
 * Section 4. ChatModel 심화 — 대화 기억 서비스 계층 (슬라이드 S12·S14)
 *
 * ChatConfig가 만든 "memoryChatClient"(대화 기억 Advisor가 붙은 빈)를 주입받습니다.
 * 같은 conversationId로 보내면 대화가 이어지고, 다른 id면 새 대화가 됩니다.
 *
 * MovieService와 마찬가지로 AI 호출 로직은 Service에, HTTP는 Controller에 둡니다.
 */
@Service
public class MemoryChatService {

    private final ChatClient chatClient;

    // 기본 ChatClient가 아니라 기억이 붙은 memoryChatClient를 이름으로 지정해 주입
    public MemoryChatService(@Qualifier("memoryChatClient") ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * [S12] 동기 대화 — conversationId로 사용자별 대화를 분리합니다.
     * advisors 파라미터로 CONVERSATION_ID를 넘기면 해당 대화의 이력이 자동 주입됩니다.
     */
    public String chat(String conversationId, String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    /**
     * [S14] 스트리밍 대화 — 대화 기억 + 스트리밍이 함께 동작합니다.
     * Advisor는 stream()에서도 동일하게 적용됩니다 (확장점 패턴의 힘).
     */
    public Flux<String> chatStream(String conversationId, String message) {
        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content();
    }
}