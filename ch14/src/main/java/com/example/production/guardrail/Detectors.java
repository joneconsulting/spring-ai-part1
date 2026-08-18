package com.example.production.guardrail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 가드레일 탐지기 모음 (교안 14장 4~5절)
 *
 *   - InjectionDetector : 입력 필터 (알려진 인젝션 패턴)
 *   - PiiDetector       : 출력 검증 (개인정보 패턴 유출)
 *   - SystemPromptLeakDetector : 출력 검증 (시스템 프롬프트 노출)
 *
 * SOC 관점(교안 14장 4절): 어느 한 겹도 완벽하지 않다는 전제로 겹겹이 쌓는다.
 * 정규식은 "알려진 공격의 비용을 올리는" 1차 필터일 뿐이다.
 */
public class Detectors {

    /** 입력 필터 — 인젝션 패턴 (교안 14장 4절 ①) */
    @Component
    public static class InjectionDetector {
        private final List<String> patterns;

        public InjectionDetector(@Value("${guardrail.injection-patterns:}") String csv) {
            this.patterns = Arrays.stream(csv.split(","))
                    .map(String::trim).map(String::toLowerCase)
                    .filter(s -> !s.isBlank()).toList();
        }

        public boolean isSuspicious(String text) {
            if (text == null) return false;
            String lower = text.toLowerCase();
            return patterns.stream().anyMatch(lower::contains);
        }
    }

    /** 출력 검증 — 개인정보 패턴 (교안 14장 5절 ③) */
    @Component
    public static class PiiDetector {
        // 주민번호(6-7), 전화번호, 카드번호(4-4-4-4), 이메일
        private static final Pattern RRN = Pattern.compile("\\d{6}[-\\s]?[1-4]\\d{6}");
        private static final Pattern PHONE = Pattern.compile("01[016789][-\\s]?\\d{3,4}[-\\s]?\\d{4}");
        private static final Pattern CARD = Pattern.compile("\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}[-\\s]?\\d{4}");
        private static final Pattern EMAIL = Pattern.compile("[\\w.+-]+@[\\w-]+\\.[\\w.-]+");

        public boolean contains(String text) {
            if (text == null) return false;
            return RRN.matcher(text).find()
                    || PHONE.matcher(text).find()
                    || CARD.matcher(text).find()
                    || EMAIL.matcher(text).find();
        }

        /** 마스킹 (로깅 시 사용 — 교안 14장 2절) */
        public String mask(String text) {
            if (text == null) return null;
            text = RRN.matcher(text).replaceAll("******-*******");
            text = PHONE.matcher(text).replaceAll("***-****-****");
            text = CARD.matcher(text).replaceAll("****-****-****-****");
            text = EMAIL.matcher(text).replaceAll("***@***");
            return text;
        }
    }

    /** 출력 검증 — 시스템 프롬프트 노출 (교안 14장 5절 ③) */
    @Component
    public static class SystemPromptLeakDetector {
        private final List<String> markers;

        public SystemPromptLeakDetector(@Value("${guardrail.system-prompt-markers:}") String csv) {
            this.markers = Arrays.stream(csv.split(","))
                    .map(String::trim).filter(s -> !s.isBlank()).toList();
        }

        public boolean leaks(String text) {
            if (text == null) return false;
            return markers.stream().anyMatch(text::contains);
        }
    }
}
