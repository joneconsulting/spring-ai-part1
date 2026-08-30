package com.example.expert.service;

import com.example.expert.domain.Evidence;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 근거 검증기 (교안 9장 7절 STEP 3)
 *
 * LLM이 evidences 를 지어낼 수 있으므로,
 * 반환된 인용문이 실제 검색된 문서에 존재하는지 후검증한다.
 */
@Component
public class EvidenceVerifier {

    /** 검증 결과: 통과 여부 + 경고 목록 */
    public record Result(boolean verified, List<String> warnings) {}

    public Result verify(List<Evidence> evidences, List<Document> retrieved) {
        List<String> warnings = new ArrayList<>();

        if (evidences == null || evidences.isEmpty()) {
            return null;
        }

        String corpus = null;

        boolean allFound = true;
        for (Evidence e : evidences) {

        }

        return null;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "");
    }

    private static String truncate(String s) {
        return s.length() <= 40 ? s : s.substring(0, 40) + "...";
    }
}
