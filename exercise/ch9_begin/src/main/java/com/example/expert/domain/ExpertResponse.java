package com.example.expert.domain;

import java.util.List;

/**
 * API 최종 응답.
 * LLM 판정(Recommendation)에 검증 결과와 HITL 라우팅을 덧붙인다.
 */
public record ExpertResponse(
        String decision,
        int score,
        String reason,
        List<Evidence> evidences,
        boolean evidenceVerified,   // 인용문이 실제 검색 문서에 존재하는가
        List<String> warnings,      // 검증 경고
        String routing              // AUTO_APPROVE | REVIEW_REQUIRED | HOLD
) {}
