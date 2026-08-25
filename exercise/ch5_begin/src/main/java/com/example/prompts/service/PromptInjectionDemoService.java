package com.example.prompts.service;

import com.example.prompts.dto.InjectionDefenseResult;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Prompt Injection — Prompt는 공격 표면(Attack Surface).
 * 3중 방어 구조 — Defense in Depth.
 *
 *   1차 입력 검증        — 요청이 모델에 닿기 전
 *   2차 시스템 프롬프트 방어 — 모델 스스로 버티게
 *   3차 출력 검증        — 새어 나가기 전 마지막 문
 *
 * 여기서는 세 겹의 '원리'를 최소 구현으로 보여준다.
 * Advisor 체인으로 만드는 프로덕션 가드레일은 Section 14에서 이어진다.
 */
@Service
public class PromptInjectionDemoService {

    /** 방어 없는 봇의 시스템 프롬프트 */
    private static final String UNDEFENDED_SYSTEM = null;

    /** 1차 — 의심 패턴 (슬라이드의 "지시 무시" 등) */
    private static final List<Pattern> SUSPICIOUS_INPUT = List.of(
            Pattern.compile("(이전|위의?|모든)\\s*지시.*(무시|잊)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("ignore\\s+(all\\s+)?(previous|above)\\s+instructions?", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(시스템|system)\\s*(프롬프트|prompt).*(출력|공개|알려|보여)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("너는\\s*이제.*(제한이?\\s*없|아무거나)", Pattern.CASE_INSENSITIVE),
            Pattern.compile("(역할|role)\\s*(을|를)?\\s*(변경|바꿔)", Pattern.CASE_INSENSITIVE)
    );

    /** 1차 — 입력 길이 제한 */
    private static final int MAX_INPUT_LENGTH = 1000;

    /** 3차 — 응답에 섞이면 안 되는 조각 */
    private static final List<String> FORBIDDEN_OUTPUT_FRAGMENTS = List.of();

    private static final String BLOCK_MESSAGE = null;

    private final PromptRunner runner;

    /** 2차 — 방어 규칙이 포함된 시스템 프롬프트 (외부 파일) */
    @Value("classpath:/prompts/system-hardened-refund-bot.st")
    private Resource hardenedResource;

    public PromptInjectionDemoService(PromptRunner runner) {
        this.runner = runner;
    }

    /**
     * 공격 문자열 하나를 세 겹의 방어에 차례로 통과시키고, 각 층의 판정을 그대로 반환한다.
     *
     * @param userInput 수강생이 직접 만들어 보는 공격문.
     */
    public InjectionDefenseResult run(String userInput) {

        // ── 방어 없는 봇 (비교군) ─────────────────────────────
        String undefended = runner.systemAndUser(UNDEFENDED_SYSTEM, userInput);

        // ── 1차. 입력 검증 ────────────────────────────────────
        List<String> matched = new ArrayList<>();
        for (Pattern p : SUSPICIOUS_INPUT) {
            if (p.matcher(userInput).find()) {
                matched.add(p.pattern());
            }
        }
        if (userInput.length() > MAX_INPUT_LENGTH) {
            matched.add("입력 길이 초과 (> " + MAX_INPUT_LENGTH + "자)");
        }
        boolean inputBlocked = !matched.isEmpty();

        if (inputBlocked) {
            // 모델에 닿기 전에 차단 — 토큰 비용도 발생하지 않는다
            return new InjectionDefenseResult(
                    userInput, undefended,
                    true, matched,
                    null,
                    false, List.of(),
                    BLOCK_MESSAGE);
        }

        // ── 2차. 시스템 프롬프트 방어 ─────────────────────────
        String hardenedSystem = new PromptTemplate(hardenedResource).getTemplate();
        String hardenedAnswer = runner.systemAndUser(hardenedSystem, userInput);

        // ── 3차. 출력 검증 ────────────────────────────────────
        List<String> violations = new ArrayList<>();
        for (String fragment : FORBIDDEN_OUTPUT_FRAGMENTS) {
            if (hardenedAnswer.contains(fragment)) {
                violations.add("시스템 프롬프트 조각 유출: \"" + fragment + "\"");
            }
        }
        boolean outputBlocked = !violations.isEmpty();

        return new InjectionDefenseResult(
                userInput, undefended,
                false, List.of(),
                hardenedAnswer,
                outputBlocked, violations,
                outputBlocked ? BLOCK_MESSAGE : hardenedAnswer);
    }
}
