package com.example.springai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AiRunner implements CommandLineRunner {
    private final ChatClient chatClient;

    public AiRunner(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Override
    public void run(String... args) throws Exception {
        String answer = chatClient.prompt()
                .user("Spring AI를 한 문장으로 정의해줘")
                .call()
                .content();

        System.out.println(answer);
    }
}
