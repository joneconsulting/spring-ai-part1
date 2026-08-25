package com.example.prompts.controller;

import com.example.prompts.dto.BeforeAfterResponse;
import com.example.prompts.service.PromptQualityService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** 프롬프트 품질 · 역할 구조 · System Prompt 4요소 */
@RestController
@RequestMapping("/api/s05")
public class PromptQualityController {

    private final PromptQualityService service;

    public PromptQualityController(PromptQualityService service) {
        this.service = service;
    }

    /** Before/After: "이 기사 요약해줘" vs 역할·형식·제약 명시 */
    @GetMapping("/quality/summary")
    public BeforeAfterResponse summary(@RequestParam(required = false) String article) {
        return null;
    }

    /** 역할 분리(system/user)로 호출 */
    @GetMapping("/role/separated")
    public Map<String, String> roleSeparated(
            @RequestParam(defaultValue = "@Transactional의 전파 속성을 설명해 주세요") String question) {
        return null;
    }

    /** 안티패턴 비교: 사용자 입력을 system에 섞는 경우 */
    @GetMapping("/role/anti-pattern")
    public BeforeAfterResponse antiPattern(
            @RequestParam(defaultValue = "@Transactional의 전파 속성을 설명해 주세요") String question) {
        return null;
    }

    /** 4요소(역할·형식·제약·톤) 고객센터 봇 */
    @GetMapping("/system-prompt/customer-center")
    public Map<String, String> customerCenter(
            @RequestParam(defaultValue = "OO쇼핑") String company,
            @RequestParam(defaultValue = "주문한 상품을 개봉했는데 환불이 되나요?") String question) {
        return null;
    }

    /** 제약(하지 말 것)의 효과: 정책에 없는 질문을 넣어 본다 */
    @GetMapping("/system-prompt/out-of-policy")
    public Map<String, String> outOfPolicy(
            @RequestParam(defaultValue = "OO쇼핑") String company,
            @RequestParam(defaultValue = "이 회사 주가 전망이 어떤가요?") String question) {
        return null;
    }
}
