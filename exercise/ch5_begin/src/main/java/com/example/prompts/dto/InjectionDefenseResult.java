package com.example.prompts.dto;

import java.util.List;

/**
 * 인젝션 공격과 3중 방어(Defense in Depth) 결과.
 * 각 방어층이 어디서 걸러냈는지를 그대로 노출해, "한 겹으로는 부족하다"를 눈으로 확인
 */
public record InjectionDefenseResult(
        String userInput,

        // 방어 없는 봇
        String undefendedAnswer,

        // 1차 — 입력 검증 (요청이 모델에 닿기 전)
        boolean inputBlocked,
        List<String> matchedInputPatterns,

        // 2차 — 시스템 프롬프트 방어 (모델 스스로 버티게)
        String hardenedAnswer,

        // 3차 — 출력 검증 (새어 나가기 전 마지막 문)
        boolean outputBlocked,
        List<String> outputViolations,

        String finalAnswer
) {
}
