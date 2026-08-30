package com.example.expert.domain;

import java.util.List;

/**
 * 판정 결과 (교안 6장 2절)
 *
 * 필드별 소비자가 다르다:
 *   decision / score  -> 시스템 (자동화 연결)
 *   reason            -> 사람 (설명)
 *   evidences         -> 감사·검증 (추적성)
 */
public record Recommendation(
        String decision,            // 추천 인스턴스명 또는 "판단 보류"
        int score,                  // 적합도 0~100
        String reason,              // 사람이 읽는 판단 근거
        List<Evidence> evidences    // 출처 목록
) {}
