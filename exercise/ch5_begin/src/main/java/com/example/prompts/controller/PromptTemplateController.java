package com.example.prompts.controller;

import com.example.prompts.service.ChatService;
import com.example.prompts.service.TravelService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** PromptTemplate 변수 바인딩 · Prompt 외부 파일 관리 */
@RestController
@RequestMapping("/api/s05/template")
public class PromptTemplateController {

    private final TravelService travelService;
    private final ChatService chatService;

    public PromptTemplateController(TravelService travelService, ChatService chatService) {
        this.travelService = travelService;
        this.chatService = chatService;
    }

    /** 대비용 — 문자열 연결(+) 방식 */
    @GetMapping("/travel/concat")
    public Map<String, String> travelConcat(
            @RequestParam(defaultValue = "여행 전문가") String role,
            @RequestParam(defaultValue = "번호 목록 3개") String format,
            @RequestParam(defaultValue = "가을에 가기 좋은 국내 여행지를 추천해 주세요") String question) {
        return null;
    }

    /** 슬라이드 코드 그대로 — 여행 전문가 · 번호 목록 3개 */
    @GetMapping("/travel")
    public Map<String, String> travel(
            @RequestParam(defaultValue = "가을에 가기 좋은 국내 여행지를 추천해 주세요") String question) {
        return null;
    }

    /** 템플릿 재사용 — 역할·형식만 바꿔 다른 도메인에 적용 */
    @GetMapping("/travel/custom")
    public Map<String, String> travelCustom(
            @RequestParam(defaultValue = "요리 연구가") String role,
            @RequestParam(defaultValue = "표 형식") String format,
            @RequestParam(defaultValue = "1인 가구를 위한 간단한 저녁 메뉴를 알려주세요") String question) {
        return null;
    }

    /** 현재 로딩된 system.st 원문 확인 — "프롬프트도 리뷰 대상" */
    @GetMapping("/prompt-file")
    public Map<String, String> promptFile() {
        return null;
    }

    /** 슬라이드 코드 그대로 — Resource를 system()에 직접 전달 ({service} 미치환) */
    @GetMapping("/chat/raw")
    public Map<String, String> chatRaw(
            @RequestParam(defaultValue = "환불은 며칠 이내에 신청해야 하나요?") String question) {
        return null;
    }

    /** 실전형 — {service} 바인딩 후 전달 */
    @GetMapping("/chat")
    public Map<String, String> chat(
            @RequestParam(defaultValue = "OO쇼핑 고객센터") String service,
            @RequestParam(defaultValue = "환불은 며칠 이내에 신청해야 하나요?") String question) {
        return null;
    }
}
