package com.example.rag.eval;

import com.example.rag.tuning.TuningService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Section 8 실습 API
 *
 *  POST /api/tuning/ingest-all   청크 크기별(200/500/1000) 적재
 *  GET  /api/tuning/compare      청크 크기별 검색 결과 비교
 *  GET  /api/tuning/search       검색 파라미터 실험 (topK / threshold / filter)
 *  GET  /api/rag/ask             현재 설정으로 질의응답 (근거 포함)
 *  POST /api/eval/run            평가 세트 전체 실행 -> 리포트
 */
@RestController
public class EvalController {

    private final TuningService tuningService;
    private final RagService ragService;
    private final EvaluationService evaluationService;

    public EvalController(TuningService tuningService,
                          RagService ragService,
                          EvaluationService evaluationService) {
        this.tuningService = tuningService;
        this.ragService = ragService;
        this.evaluationService = evaluationService;
    }

    @PostMapping("/api/tuning/ingest-all")
    public Map<String, Object> ingestAll() {
        return tuningService.ingestAll();
    }

    @GetMapping("/api/tuning/compare")
    public Map<String, Object> compare(@RequestParam String q,
                                       @RequestParam(defaultValue = "2") int topK) {
        return tuningService.compare(q, topK);
    }

    @GetMapping("/api/tuning/search")
    public Map<String, Object> search(@RequestParam String q,
                                      @RequestParam(defaultValue = "4") int topK,
                                      @RequestParam(defaultValue = "0.0") double threshold,
                                      @RequestParam(required = false) Integer chunkSize,
                                      @RequestParam(required = false) String category) {
        return tuningService.search(q, topK, threshold, chunkSize, category);
    }

    @GetMapping("/api/rag/ask")
    public Map<String, Object> ask(@RequestParam String q) {
        RagService.RagResult r = ragService.ask(q);
        return Map.of(
                "question", r.question(),
                "answer", r.answer(),
                "evidenceCount", r.evidences().size(),
                "contextTokensApprox", r.contextTokensApprox());
    }

    @PostMapping("/api/eval/run")
    public EvaluationService.Report runEvaluation() {
        return evaluationService.run();
    }
}
