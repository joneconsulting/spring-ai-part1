package com.example.production.guardrail;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 가드레일이 적용된 ChatClient 를 구성한다.
 *
 * GuardrailAdvisor 를 defaultAdvisors 로 등록하면
 * 이 ChatClient 를 통과하는 모든 요청에 3중 방어가 적용된다.
 */
@Configuration
public class GuardrailConfig {

    @Bean
    public GuardrailAdvisor guardrailAdvisor(Detectors.InjectionDetector injectionDetector,
                                             Detectors.PiiDetector piiDetector,
                                             Detectors.SystemPromptLeakDetector leakDetector,
                                             MeterRegistry meterRegistry) {
        return new GuardrailAdvisor(injectionDetector, piiDetector, leakDetector, meterRegistry);
    }

    @Bean
    public ChatClient guardedChatClient(ChatClient.Builder builder,
                                        GuardrailAdvisor guardrailAdvisor) {
        return builder
                .defaultSystem("너는 사내 규정 안내 도우미다. 한국어로 간결하게 답하라.")
                .defaultAdvisors(guardrailAdvisor)   // 최상단에 가드레일
                .build();
    }
}
