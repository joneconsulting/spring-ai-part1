package com.example.springai.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {

    /**
     * 대화 이력 저장소.
     * MessageWindowChatMemory는 최근 N개 메시지만 유지하는 슬라이딩 윈도우 방식입니다.
     * 기본 저장소는 In-Memory라 애플리케이션 재시작 시 대화가 소실됩니다.
     * (운영에서는 JDBC 등 영속 저장소를 고려 — 슬라이드 S13)
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)     // 최근 10개 메시지만 유지 (윈도우 = 토큰 비용, 슬라이드 S13)
                .build();
    }

    /**
     * 대화 기억이 붙은 ChatClient.
     * defaultAdvisors로 MessageChatMemoryAdvisor를 등록하면,
     * 이 ChatClient로 나가는 모든 요청에 대화 이력이 자동으로 주입됩니다.
     */
    @Bean
    public ChatClient memoryChatClient(ChatClient.Builder builder, ChatMemory chatMemory) {
        return builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
