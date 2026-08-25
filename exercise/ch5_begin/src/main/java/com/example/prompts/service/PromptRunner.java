package com.example.prompts.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.stereotype.Component;

/**
 * 모든 실습이 공통으로 쓰는 호출 유틸.
 * 호출마다 토큰 사용량을 로그로 남긴다 — S16의 "시스템 프롬프트 길이 = 고정비"를 수치로 확인하기 위함.
 */
@Component
public class PromptRunner {

    private static final Logger log = LoggerFactory.getLogger(PromptRunner.class);

    private final ChatClient chatClient;

    public PromptRunner(ChatClient.Builder builder) {
        // defaultSystem을 지정하지 않는다 — 시스템 프롬프트의 유무·내용 차이가 이 섹션의 실습 대상이기 때문.
        this.chatClient = builder.build();
    }

    public ChatClient client() {
        return chatClient;
    }

    /** user 채널만 사용 */
    public String user(String userText) {
        return null;
    }

    /** system + user — S6의 역할 분리 */
    public String systemAndUser(String systemText, String userText) {
        return null;
    }

    /** 토큰 수까지 필요한 경우 */
    public Result userWithTokens(String userText) {
        return null;
    }

    public Result systemAndUserWithTokens(String systemText, String userText) {
        return null;
    }

    /*
     * ChatReponse는 텍스트 외에 토큰사용량, 정상종료 여부, 메타데이터 등의 정보도 표시
     */
    private String text(ChatResponse response) {

        return null;
    }

    private Long totalTokens(ChatResponse response) {
        var total = response.getMetadata().getUsage().getTotalTokens();
        return total == null ? null : total.longValue();
    }

    public record Result(String answer, Long totalTokens) {
    }
}
