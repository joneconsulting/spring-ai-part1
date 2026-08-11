package com.example.rag.eval;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** 평가 세트 모델 (evaluation-set.json 매핑) */
@JsonIgnoreProperties(ignoreUnknown = true)
public record EvaluationSet(String description, String document, List<EvalQuestion> questions) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record EvalQuestion(
            String id,
            String type,            // general | boundary | trap
            String question,
            String expectedSource,  // 기대 근거 (trap 은 null)
            boolean answerable      // false 면 "확인할 수 없습니다"가 정답
    ) {}
}
