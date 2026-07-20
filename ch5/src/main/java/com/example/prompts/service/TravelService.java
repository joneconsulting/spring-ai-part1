package com.example.prompts.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * S8. PromptTemplate — 변수 바인딩.
 * "문자열 연결(+)이 아니라 템플릿으로"
 *
 * 왜 템플릿인가: 구조와 데이터의 분리 — JdbcTemplate의 ?, Thymeleaf의 ${}와 같은 이유.
 * {변수} 문법은 기본 StTemplateRenderer 기준.
 */
@Service
public class TravelService {

    private final ChatClient chatClient;

    public TravelService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /** ── 슬라이드 코드 그대로 ────────────────────────────────── */
    public String recommend(String question) {
        PromptTemplate template = new PromptTemplate("""
                당신은 {role}입니다.
                아래 질문에 {format} 형식으로 답하세요.

                질문: {question}
                """);

        String answer = chatClient
                .prompt(template.create(Map.of(
                        "role", "여행 전문가",
                        "format", "번호 목록 3개",
                        "question", question)))
                .call()
                .content();

        return answer;
    }

    /**
     * 대비용 — 문자열 연결(+) 방식.
     * 강의에서 먼저 이 코드를 보여주고 가독성·이스케이프·인젝션 위험을 지적한 뒤
     * 위 템플릿 버전으로 넘어간다.
     */
    public String recommendByConcat(String role, String format, String question) {
        String prompt = "당신은 " + role + "입니다.\n"
                + "아래 질문에 " + format + " 형식으로 답하세요.\n\n"
                + "질문: " + question;   // ← 사용자 입력이 프롬프트 구조에 그대로 섞인다

        return chatClient.prompt().user(prompt).call().content();
    }

    /** 역할·형식을 바꿔가며 템플릿의 재사용성을 확인 */
    public String recommendCustom(String role, String format, String question) {
        PromptTemplate template = new PromptTemplate("""
                당신은 {role}입니다.
                아래 질문에 {format} 형식으로 답하세요.

                질문: {question}
                """);

        return chatClient
                .prompt(template.create(Map.of(
                        "role", role,
                        "format", format,
                        "question", question)))
                .call()
                .content();
    }
}
