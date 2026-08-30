package com.example.rag.eval;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

/**
 * 평가 세트 기반 품질 측정 (교안 8장 6~9절)
 *
 * 측정 지표
 *   - Relevancy 통과율   : LLM-as-a-Judge 로 질문-답변 관련성 판정
 *   - "없다" 정답 처리율  : trap 문항에서 환각을 억제했는가
 *   - 평균 근거 토큰      : 컨텍스트 다이어트 효과 (비용 지표)
 */
@Service
public class EvaluationService {

    private static final Logger log = LoggerFactory.getLogger(EvaluationService.class);
    private static final String NOT_FOUND_MARKER = "확인할 수 없습니다";

    private final RagService ragService;
    private final RelevancyEvaluator relevancyEvaluator;
    private final EvaluationSet evaluationSet;

    public EvaluationService(RagService ragService,
                             ChatClient.Builder builder,
                             ObjectMapper objectMapper,
                             @Value("classpath:eval/evaluation-set.json") Resource evalSetResource)
            throws IOException {
        this.ragService = ragService;
        this.relevancyEvaluator = new RelevancyEvaluator(builder);
        this.evaluationSet = objectMapper.readValue(
                evalSetResource.getInputStream(), EvaluationSet.class);
    }

    public Report run() {
        List<ItemResult> items = new ArrayList<>();

        for (EvaluationSet.EvalQuestion q : evaluationSet.questions()) {
            RagService.RagResult r = ragService.ask(q.question());
            boolean saidNotFound = r.answer().contains(NOT_FOUND_MARKER);

            boolean pass;
            String note;

            if (!q.answerable()) {
                // 함정 문항: "확인할 수 없습니다"라고 답해야 통과

            } else if (saidNotFound) {
                // 답할 수 있어야 하는데 못 찾음 -> 검색 실패

            } else {
                // LLM-as-a-Judge 로 관련성 판정
                EvaluationRequest req = null; // 평가에 필요한 입력값을 담는 객체
                EvaluationResponse res = null; // 평가 결과를 담는 객체

            }

//            items.add(new ItemResult(q.id(), q.type(), q.question(), pass, note,
//                    r.contextTokensApprox(), preview(r.answer())));
//            log.info("[{}] {} - {}", q.id(), pass ? "PASS" : "FAIL", note);
        }

        return summarize(items);
    }

    private Report summarize(List<ItemResult> items) {
        long total = 0;
        long passed = 0;

        Map<String, String> byType = new LinkedHashMap<>();
        for (String type : List.of("general", "boundary", "trap")) {

        }

        double avgTokens = 0.0;
        List<String> failed = null;

        return new Report(
                "%d/%d (%.0f%%)".formatted(passed, total, pct(passed, total)),
                byType,
                Math.round(avgTokens),
                failed,
                items,
                "회귀 확인: 이전 실행의 failedIds 와 비교하라. 전체 통과율이 올라도 개별 문항이 깨질 수 있다."
        );
    }

    private static double pct(long n, long d) {
        return d == 0 ? 0 : n * 100.0 / d;
    }

    private static String preview(String s) {
        String flat = s.replaceAll("\\s+", " ").trim();
        return flat.length() <= 120 ? flat : flat.substring(0, 120) + " ...";
    }

    public record ItemResult(String id, String type, String question, boolean pass,
                             String note, int contextTokensApprox, String answerPreview) {}

    public record Report(String overallPassRate,
                         Map<String, String> passRateByType,
                         long avgContextTokensApprox,
                         List<String> failedIds,
                         List<ItemResult> items,
                         String hint) {}
}
