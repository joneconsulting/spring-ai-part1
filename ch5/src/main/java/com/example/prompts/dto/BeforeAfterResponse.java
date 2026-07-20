package com.example.prompts.dto;

/**
 * S5·S10·S12 — "Before vs After"를 한 번의 호출로 나란히 보여주기 위한 응답.
 * 프롬프트 원문까지 함께 반환하므로, 응답 화면만으로 "무엇을 추가했더니 어떻게 달라졌는지"가 드러난다.
 */
public record BeforeAfterResponse(
        String topic,
        String beforeLabel,
        String beforePrompt,
        String beforeAnswer,
        Long beforeTokens,
        String afterLabel,
        String afterPrompt,
        String afterAnswer,
        Long afterTokens
) {
}
