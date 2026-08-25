package com.example.prompts.controller;

import com.example.prompts.dto.BeforeAfterResponse;
import com.example.prompts.dto.PromptVersionResult;
import com.example.prompts.service.KoreanPromptService;
import com.example.prompts.service.PromptQualityService;
import com.example.prompts.service.PromptVersionLabService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 한국어 Prompt 전략 · 실습(V1~V4) */
@RestController
@RequestMapping("/api/s05")
public class KoreanAndLabController {

    private final KoreanPromptService koreanService;
    private final PromptVersionLabService labService;

    public KoreanAndLabController(KoreanPromptService koreanService,
                                  PromptVersionLabService labService) {
        this.koreanService = koreanService;
        this.labService = labService;
    }

    /** 언어 조합 A/B — "정답은 실험" */
    @GetMapping("/korean/ab-test")
    public BeforeAfterResponse abTest(
            @RequestParam(defaultValue = "MSA 환경에서 분산 트랜잭션을 다루는 방법") String task) {
        return null;
    }

    /** 시스템 프롬프트 다이어트 — 길이 = 고정비 */
    @GetMapping("/korean/system-diet")
    public BeforeAfterResponse systemDiet(
            @RequestParam(defaultValue = "배송이 지연되면 보상이 있나요?") String question) {
        return null;
    }

    /** 출력 언어 명시 유무 — "운에 맡기지 말 것" */
    @GetMapping("/korean/output-language")
    public Map<String, String> outputLanguage(
            @RequestParam(defaultValue = "Explain the CAP theorem briefly.") String question) {
        return null;
    }

    /**
     * 실습 — 같은 과제, 4개의 Prompt를 각 3회 실행.
     * 기본 4버전 × 3회 = 12회 호출. 토큰 사용량에 유의.
     */
    @GetMapping("/lab/prompt-versions")
    public PromptVersionResult promptVersions(
            @RequestParam(required = false) String article,
            @RequestParam(defaultValue = "3") int runs) {
        return null;
    }
}
