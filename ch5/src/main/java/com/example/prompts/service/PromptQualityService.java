package com.example.prompts.service;

import com.example.prompts.dto.BeforeAfterResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * S5 프롬프트가 품질을 결정 / S6 역할 구조 / S7 좋은 System Prompt의 4요소.
 */
@Service
public class PromptQualityService {

    /** 슬라이드에서 사용하는 예시 기사 (수강생이 다른 기사로 바꿔 실험) */
    public static final String SAMPLE_ARTICLE = """
            한국은행이 기준금리를 연 3.00%로 동결했다. 3회 연속 동결이다.
            금융통화위원회는 물가 상승률이 목표치에 근접했으나 가계부채 증가세와
            원/달러 환율 변동성을 고려해 신중한 기조를 유지하기로 했다.
            시장에서는 하반기 인하 가능성을 점치고 있으나, 총재는 기자회견에서
            "인하 시점을 예단하기 이르다"고 말했다. 이날 코스피는 0.4% 상승 마감했다.
            """;

    private final PromptRunner runner;

    /** S7 4요소 시스템 프롬프트 — 외부 파일로 관리 */
    @Value("classpath:/prompts/system-customer-center.st")
    private Resource customerCenterResource;

    public PromptQualityService(PromptRunner runner) {
        this.runner = runner;
    }

    /**
     * S5 — 같은 모델, 같은 비용. 문장만 바꿔도 결과 품질이 달라진다.
     * Before: "이 기사 요약해줘"  →  After: 역할·형식·제약 명시
     */
    public BeforeAfterResponse summaryBeforeAfter(String article) {
        String before = "이 기사 요약해줘\n\n" + article;

        String after = """
                당신은 경제 뉴스 에디터입니다.
                아래 기사를 핵심 사실 중심으로
                3개의 불릿, 각 40자 이내로 요약하세요.
                의견이나 전망은 포함하지 마세요.

                기사:
                %s
                """.formatted(article);

        var b = runner.userWithTokens(before);
        var a = runner.userWithTokens(after);

        return new BeforeAfterResponse(
                "S5 — 프롬프트가 품질을 결정",
                "Before — 막연한 지시", before, b.answer(), b.totalTokens(),
                "After — 역할·형식·제약 명시", after, a.answer(), a.totalTokens());
    }

    /**
     * S6 — 역할 구조. 사용자 입력은 user 채널로만 전달한다.
     * 슬라이드 코드 그대로: system("당신은 Spring 전문가입니다. 코드 예시와 함께 한국어로 답하세요.")
     */
    public String roleSeparated(String question) {
        return runner.client().prompt()
                .system("당신은 Spring 전문가입니다. 코드 예시와 함께 한국어로 답하세요.")
                .user(question)   // 사용자 입력은 user 채널로만
                .call().content();
    }

    /**
     * S6 안티패턴 비교 — 사용자 입력을 system에 섞어 넣으면 어떻게 되는가.
     * S14 인젝션 데모에서 이 분리가 왜 보안의 기초인지 회수된다.
     */
    public BeforeAfterResponse roleSeparationAntiPattern(String question) {
        // ❌ 안티패턴: 사용자 입력을 시스템 메시지에 이어 붙임
        String mixed = "당신은 Spring 전문가입니다. 코드 예시와 함께 한국어로 답하세요.\n" + question;
        var bad = runner.userWithTokens(mixed);

        // ✅ 정상: 채널 분리
        var good = runner.systemAndUserWithTokens(
                "당신은 Spring 전문가입니다. 코드 예시와 함께 한국어로 답하세요.", question);

        return new BeforeAfterResponse(
                "S6 — 역할 구조(System · User · Assistant)",
                "❌ 안티패턴 — 한 덩어리로 전달", mixed, bad.answer(), bad.totalTokens(),
                "✅ 채널 분리 — system / user", "[system] Spring 전문가 규칙\n[user] " + question,
                good.answer(), good.totalTokens());
    }

    /**
     * S7 — 좋은 System Prompt의 4요소(역할·형식·제약·톤).
     * 고객센터 봇 예시를 외부 파일(system-customer-center.st)에서 읽어 {company}만 바인딩한다.
     */
    public String customerCenterBot(String company, String question) {
        PromptTemplate template = new PromptTemplate(customerCenterResource);
        String systemText = template.render(Map.of("company", company));
        return runner.systemAndUser(systemText, question);
    }
}
