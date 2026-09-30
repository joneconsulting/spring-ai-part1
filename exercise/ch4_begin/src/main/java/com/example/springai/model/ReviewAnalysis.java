package com.example.springai.model;

public record ReviewAnalysis(
        Genre genre,              // 리뷰로 추정한 장르
        double sentiment,         // 0(매우 부정) ~ 4(매우 긍정)
        boolean spoiler,          // 스포일러 포함 여부
        Double confidence         // Jev만 제공 (LLM 버전은 null)
) {
}
