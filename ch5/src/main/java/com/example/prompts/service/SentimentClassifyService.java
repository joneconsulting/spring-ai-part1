package com.example.prompts.service;

import com.example.prompts.dto.BeforeAfterResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * S10. Zero-shot vs Few-shot — 예시를 줄 것인가.
 * S11. Few-shot 실전 — 감성 분류. "예시가 곧 사양(Spec)"
 *
 * 시연 순서: Zero-shot으로 먼저 시켜 "긍정적인 리뷰로 보입니다. 왜냐하면..." 같은 장황한 응답을
 *          보여준 뒤, Few-shot으로 한 단어 출력에 수렴시킨다.
 */
@Service
public class SentimentClassifyService {

    private final PromptRunner runner;

    @Value("classpath:/prompts/few-shot-sentiment.st")
    private Resource fewShotResource;

    public SentimentClassifyService(PromptRunner runner) {
        this.runner = runner;
    }

    /** Zero-shot — 지시만으로 수행 */
    public String zeroShot(String review) {
        return runner.user("다음 리뷰의 감성을 분류하세요.\n\n리뷰: " + review);
    }

    /** Few-shot — 예시 2개로 출력 형식을 고정 (슬라이드 프롬프트 그대로) */
    public String fewShot(String review) {
        String prompt = new PromptTemplate(fewShotResource).render(Map.of("review", review));
        return runner.user(prompt);
    }

    /** 두 방식을 나란히 — 출력이 예시의 형식에 수렴하는 것을 확인 */
    public BeforeAfterResponse compare(String review) {
        String zeroPrompt = "다음 리뷰의 감성을 분류하세요.\n\n리뷰: " + review;
        String fewPrompt = new PromptTemplate(fewShotResource).render(Map.of("review", review));

        var z = runner.userWithTokens(zeroPrompt);
        var f = runner.userWithTokens(fewPrompt);

        return new BeforeAfterResponse(
                "S10·S11 — Zero-shot vs Few-shot (감성 분류)",
                "Zero-shot — 지시만", zeroPrompt, z.answer(), z.totalTokens(),
                "Few-shot — 예시 2개", fewPrompt, f.answer(), f.totalTokens());
    }
}
