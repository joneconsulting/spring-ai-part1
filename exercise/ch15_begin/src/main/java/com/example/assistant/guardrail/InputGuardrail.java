package com.example.assistant.guardrail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 입력 가드레일 (Section 14에서 가져온 요소) — 교안 15장 2절
 *
 * 통합 프로젝트에서 가드레일은 "최상단"에 둔다.
 * A/B 어느 경로로 가든 방어가 가장 먼저 동작해야 한다.
 */
@Component
public class InputGuardrail {

    private final List<String> patterns;

    public InputGuardrail(@Value("${assistant.guardrail.injection-patterns:}") String csv) {
        this.patterns = Arrays.stream(csv.split(","))
                .map(String::trim).map(String::toLowerCase)
                .filter(s -> !s.isBlank()).toList();
    }

    /** 차단해야 하면 true */
    public boolean shouldBlock(String text) {
        if (text == null) return false;
        String lower = text.toLowerCase();

        return patterns.stream().anyMatch(lower::contains);
    }
}
