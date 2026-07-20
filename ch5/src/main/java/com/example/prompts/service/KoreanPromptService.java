package com.example.prompts.service;

import com.example.prompts.dto.BeforeAfterResponse;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * S16. 한국어 Prompt 전략.
 *
 *  - 언어 조합 실험: 지시는 영어 + "한국어로 답변" 조합이 품질이 좋은 경우가 있다.
 *                  → 정답은 실험. 우리 과제로 A/B 해보고 결정한다.
 *  - 토큰 비용: 같은 의미라도 한국어가 영어보다 토큰을 더 소비.
 *              시스템 프롬프트는 매 요청 반복 전송 — 길이가 곧 고정비.
 *  - 기본기: 출력 언어를 운에 맡기지 말 것. "한국어로 답변" 항상 명시.
 */
@Service
public class KoreanPromptService {

    private final PromptRunner runner;

    public KoreanPromptService(PromptRunner runner) {
        this.runner = runner;
    }

    /**
     * 언어 조합 A/B — 같은 과제를 한국어 지시 vs 영어 지시(+한국어 답변)로 실행.
     * 토큰 수까지 함께 반환하므로 품질과 비용을 동시에 비교할 수 있다.
     */
    public BeforeAfterResponse languageAbTest(String task) {
        String korean = """
                당신은 시니어 백엔드 개발자입니다.
                아래 과제를 3개의 불릿으로, 각 50자 이내로 정리하세요.
                근거가 없는 내용은 쓰지 마세요.

                과제: %s
                """.formatted(task);

        String english = """
                You are a senior backend developer.
                Summarize the task below in exactly 3 bullets, each under 50 Korean characters.
                Do not include unsupported claims.
                Respond in Korean.

                Task: %s
                """.formatted(task);

        var ko = runner.userWithTokens(korean);
        var en = runner.userWithTokens(english);

        return new BeforeAfterResponse(
                "S16 — 언어 조합 A/B (정답은 실험)",
                "A. 지시도 한국어", korean, ko.answer(), ko.totalTokens(),
                "B. 지시는 영어 + \"한국어로 답변\"", english, en.answer(), en.totalTokens());
    }

    /**
     * 시스템 프롬프트 다이어트 — 같은 규칙을 장황하게 vs 압축해서.
     * "시스템 프롬프트는 매 요청 반복 전송 = 고정비"를 프롬프트 토큰 수로 확인한다.
     */
    public BeforeAfterResponse systemPromptDiet(String question) {
        String verbose = """
                당신은 저희 회사의 고객센터에서 근무하는 아주 친절하고 상냥한 상담원입니다.
                고객님께서 어떤 질문을 하시더라도 항상 최선을 다해 정중하게 답변해 주셔야 하며,
                반드시 존댓말을 사용하셔야 합니다. 그리고 만약 고객님의 질문에 대한 정확한 답변을
                알지 못하는 경우에는 절대로 추측해서 답변하지 마시고, 모른다고 솔직하게 말씀해 주세요.
                또한 답변은 너무 길지 않게 간결하게 작성해 주시기 바랍니다.
                """;

        String compact = """
                고객센터 상담원. 존댓말. 추측 금지(모르면 모른다고). 3문장 이내.
                """;

        var v = runner.systemAndUserWithTokens(verbose, question);
        var c = runner.systemAndUserWithTokens(compact, question);

        return new BeforeAfterResponse(
                "S16 — 시스템 프롬프트 다이어트 (길이 = 고정비)",
                "장황한 규칙", verbose, v.answer(), v.totalTokens(),
                "압축한 규칙 (동일 의미)", compact, c.answer(), c.totalTokens());
    }

    /** 기본기 — 출력 언어 명시 유무 비교 */
    public Map<String, String> outputLanguage(String question) {
        Map<String, String> result = new LinkedHashMap<>();
        result.put("언어 미지정", runner.user(question));
        result.put("\"한국어로 답변\" 명시", runner.user(question + "\n한국어로 답변하세요."));
        return result;
    }
}
