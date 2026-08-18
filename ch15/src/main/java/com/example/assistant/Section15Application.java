package com.example.assistant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Section 15. 미니 프로젝트 — 사내 문서 AI Assistant
 *
 * 통합 요소:
 *   - 경로 A: Spring AI 직접 구현 (pgvector RAG + 가드레일)
 *   - 경로 B: Dify 연동 (mock/폴백)
 *   - A/B 비교 실험
 *   - 가드레일 (최상단)
 */
@SpringBootApplication
public class Section15Application {
    public static void main(String[] args) {
        SpringApplication.run(Section15Application.class, args);
    }
}
