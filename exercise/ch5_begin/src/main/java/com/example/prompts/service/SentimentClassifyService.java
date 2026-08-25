package com.example.prompts.service;

import com.example.prompts.dto.BeforeAfterResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Zero-shot vs Few-shot — 예시를 줄 것인가.
 * Few-shot 실전 — 감성 분류. "예시가 곧 사양(Spec)"
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
        return null;
    }

    /** Few-shot — 예시 2개로 출력 형식을 고정 (슬라이드 프롬프트 그대로) */
    public String fewShot(String review) {
        return null;
    }

    /** 두 방식을 나란히 — 출력이 예시의 형식에 수렴하는 것을 확인 */
    public BeforeAfterResponse compare(String review) {
        String zeroPrompt = null;
        String fewPrompt = null;

        var z = runner.userWithTokens(zeroPrompt);
        var f = runner.userWithTokens(fewPrompt);

        return null;
    }
}
