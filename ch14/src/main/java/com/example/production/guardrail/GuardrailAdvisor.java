package com.example.production.guardrail;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;

import java.util.List;

/**
 * 가드레일 Advisor — 3중 방어 (교안 14장 4~5절) ★
 *
 *   ① 입력 필터 (Pre)  : LLM 호출 전 인젝션 패턴 차단 (호출 자체를 막아 비용도 절감)
 *   ② 실제 LLM 호출
 *   ③ 출력 검증 (Post) : 개인정보·시스템 프롬프트 노출 차단
 *
 * Advisor 로 만들면 RAG·ChatMemory·Tool 등 어떤 조합에도 동일하게 적용된다.
 * (스프링 개발자에게는 인터셉터/AOP 와 같은 감각 — 횡단 관심사의 올바른 위치)
 */
public class GuardrailAdvisor implements CallAdvisor {

    private static final Logger log = LoggerFactory.getLogger(GuardrailAdvisor.class);

    private final Detectors.InjectionDetector injectionDetector;
    private final Detectors.PiiDetector piiDetector;
    private final Detectors.SystemPromptLeakDetector leakDetector;
    private final MeterRegistry metrics;

    public GuardrailAdvisor(Detectors.InjectionDetector injectionDetector,
                            Detectors.PiiDetector piiDetector,
                            Detectors.SystemPromptLeakDetector leakDetector,
                            MeterRegistry metrics) {
        this.injectionDetector = injectionDetector;
        this.piiDetector = piiDetector;
        this.leakDetector = leakDetector;
        this.metrics = metrics;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest req, CallAdvisorChain chain) {
        // ① 입력 필터 — 통과 못 하면 LLM 호출 자체를 하지 않는다 (비용 절감 효과도)
        String userText = req.prompt()
                .getUserMessage()
                .getText();

        if (userText != null && injectionDetector.isSuspicious(userText)) {
            metrics.counter("guardrail.blocked", "stage", "input").increment();

            log.warn("[Guardrail] 입력 차단 - 인젝션 패턴 감지");

            return refusal(req, "요청을 처리할 수 없습니다.");
        }

        // ② 실제 LLM 호출
        ChatClientResponse res = chain.nextCall(req);

        // ③ 출력 검증
        if (res.chatResponse() == null ||
                res.chatResponse().getResult() == null ||
                res.chatResponse().getResult().getOutput() == null) {

            return res;
        }

        String answer = res.chatResponse()
                .getResult()
                .getOutput()
                .getText();

        if (answer == null) {
            return res;
        }

        // PII 검사
        if (piiDetector.contains(answer)) {
            metrics.counter("guardrail.blocked", "stage", "output-pii").increment();

            log.warn("[Guardrail] 출력 차단 - 개인정보 패턴 감지");

            return refusal(req, "답변을 제공할 수 없습니다. (민감정보 포함)");
        }

        // System Prompt Leak 검사
        if (leakDetector.leaks(answer)) {
            metrics.counter("guardrail.blocked", "stage", "output-leak").increment();

            log.warn("[Guardrail] 출력 차단 - 시스템 프롬프트 노출");

            return refusal(req, "답변을 제공할 수 없습니다.");
        }

        return res;
    }

    @Override
    public String getName() {
        return "GuardrailAdvisor";
    }

    @Override
    public int getOrder() {
        return -1000;   // 최상단 — 방어가 가장 먼저 동작해야 한다 (교안 10장 2절)
    }

    /**
     * Guardrail 차단 응답 생성
     */
    private ChatClientResponse refusal(ChatClientRequest req, String message) {
        // 실제 GA 에서는 ChatClientResponse 를 message 로 구성해 반환한다.
        // 데모 스텁 환경에서는 표준 응답 객체를 반환한다.
        AssistantMessage assistantMessage =
                new AssistantMessage(message);

        Generation generation =
                new Generation(assistantMessage);

        ChatResponse chatResponse =
                new ChatResponse(List.of(generation));

        return ChatClientResponse.builder()
                .chatResponse(chatResponse)
                .context(req.context())
                .build();
    }
}
