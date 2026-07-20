package com.example.prompts.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * S9. Prompt 외부 파일 관리 — "Prompt는 소스코드"의 물리적 실현.
 *
 * 분리하면 생기는 것
 *  ① 프롬프트 수정이 코드 리뷰의 대상이 됨
 *  ② Java 코드와 독립적으로 diff·이력 추적
 *  ③ 기획자·도메인 전문가와의 협업 지점 확보
 */
@Service
public class ChatService {

    /** ── 슬라이드 코드 그대로 ────────────────────────────────── */
    @Value("classpath:/prompts/system.st")
    private Resource systemResource;

    private final ChatClient chatClient;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /**
     * 슬라이드 코드 그대로 — Resource를 system()에 직접 전달.
     * 이 경우 파일 안의 {service} 자리는 치환되지 않고 그대로 전달된다는 점을 시연에서 짚어준다.
     */
    public String ask(String question) {
        return chatClient.prompt()
                .system(systemResource)
                .user(question)
                .call().content();
    }

    /**
     * 실전형 — 파일의 {service} 변수를 바인딩한 뒤 전달.
     * S8의 PromptTemplate과 S9의 외부 파일 관리를 결합한 형태로, 실무에서 쓰는 조합이다.
     */
    public String ask(String service, String question) {
        String systemText = new PromptTemplate(systemResource).render(Map.of("service", service));

        return chatClient.prompt()
                .system(systemText)
                .user(question)
                .call().content();
    }

    /** 현재 로딩된 프롬프트 파일 원문 확인 — "프롬프트도 리뷰 대상"임을 보여주는 용도 */
    public String currentSystemPrompt() {
        return new PromptTemplate(systemResource).getTemplate();
    }
}
