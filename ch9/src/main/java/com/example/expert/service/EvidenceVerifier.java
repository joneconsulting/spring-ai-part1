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
            warnings.add("근거(evidences)가 비어 있습니다 - 판단의 추적성이 없습니다");
            return new Result(false, warnings);
        }

        String corpus = normalize(
                retrieved.stream().map(Document::getText).reduce("", (a, b) -> a + "\n" + b));

        boolean allFound = true;
        for (Evidence e : evidences) {
            if (e.quote() == null || e.quote().isBlank()) {
                warnings.add("인용문이 비어 있는 근거가 있습니다: " + e.source());
                allFound = false;
                continue;
            }
            // 인용문의 핵심 토막이 원문에 실제로 존재하는지 확인
            String needle = normalize(e.quote());
            String probe = needle.length() > 20 ? needle.substring(0, 20) : needle;

            if (!corpus.contains(probe)) {
                warnings.add("검색된 문서에서 찾을 수 없는 인용문입니다 (환각 의심): \"%s\""
                        .formatted(truncate(e.quote())));
                allFound = false;
            }
        }
        return new Result(allFound, warnings);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.replaceAll("\\s+", "");
    }

    private static String truncate(String s) {
        return s.length() <= 40 ? s : s.substring(0, 40) + "...";
    }
}
