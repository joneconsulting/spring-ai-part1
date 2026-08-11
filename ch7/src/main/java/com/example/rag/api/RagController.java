package com.example.rag.api;

import com.example.rag.ingest.IngestionService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Section 7 실습 API
 *
 *  POST /api/rag/ingest       샘플 문서 적재
 *  GET  /api/rag/search       유사도 검색만 (LLM 호출 없음) - 적재 품질 검증용
 *  GET  /api/rag/ask          RAG 질의응답 (QuestionAnswerAdvisor)
 *  GET  /api/rag/ask-norag    RAG 미적용 답변 - 비교 실습용
 */
@RestController
@RequestMapping("/api/rag")
public class RagController {

    private final IngestionService ingestionService;
    private final VectorStore vectorStore;
    private final ChatClient ragChatClient;      // QuestionAnswerAdvisor 적용
    private final ChatClient plainChatClient;    // 비교용 (RAG 없음)
    private final int defaultTopK;

    public RagController(IngestionService ingestionService,
                         VectorStore vectorStore,
                         ChatClient.Builder builder,
                         @Value("${rag.top-k:4}") int defaultTopK) {
        this.ingestionService = ingestionService;
        this.vectorStore = vectorStore;
        this.defaultTopK = defaultTopK;

        // 교안 15장 - Advisor 하나로 검색 -> 주입 -> 생성이 완성된다
        this.ragChatClient = builder.clone()
                .defaultSystem("""
                        너는 사내 규정 안내 도우미다.
                        제공된 근거 문서의 내용만 사용해 한국어로 간결하게 답하라.
                        근거에 없는 내용은 추측하지 말고 "제공된 문서에서 확인할 수 없습니다"라고 답하라.
                        답변 끝에 근거가 된 조항 번호를 표기하라.""")
                .defaultAdvisors(
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .searchRequest(SearchRequest.builder().topK(defaultTopK).build())
                                .build())
                .build();

        this.plainChatClient = builder.clone().build();
    }

    @PostMapping("/ingest")
    public IngestionService.IngestResult ingest() {
        return ingestionService.ingestSample();
    }

    @PostMapping("/ingestPdf")
    public IngestionService.IngestResult ingestPdf() {
        return ingestionService.ingestPdf();
    }

    /**
     * LLM 없이 검색만 확인한다.
     * 교안 10장 10절 - "RAG가 이상하면 검색부터 본다"는 습관을 위한 엔드포인트.
     */
    @GetMapping("/search")
    public List<Map<String, Object>> search(@RequestParam String q,
                                            @RequestParam(required = false) Integer topK,
                                            @RequestParam(required = false) String source) {
//        List<Document> results = vectorStore.similaritySearch(
//                SearchRequest.builder()
//                        .query(q)
//                        .topK(topK != null ? topK : defaultTopK)
//                        .build());

        // 위 코드를 아래와 같이 변환
        SearchRequest.Builder requestBuilder = SearchRequest.builder()
                .query(q)
                .topK(topK != null ? topK : defaultTopK);

        // source가 전달된 경우 metadata 필터 적용
        if (source != null && !source.isBlank()) {
            requestBuilder.filterExpression(
                    "source == '" + source + "'"
            );
        }

        List<Document> results = vectorStore.similaritySearch(
                requestBuilder.build()
        );

        // 결과 반환
        return results.stream().map(d -> Map.<String, Object>of(
                "source", d.getMetadata().getOrDefault("source", "-"),
                "score", d.getScore() != null ? d.getScore() : 0.0,
                "text", preview(d.getText())
        )).toList();
    }

    /** RAG 질의응답 */
    @GetMapping("/ask")
    public Map<String, Object> ask(@RequestParam String q) {
        String answer = ragChatClient.prompt().user(q).call().content();
        return Map.of("mode", "RAG", "question", q, "answer", answer);
    }

    /** 비교용 - RAG 미적용 */
    @GetMapping("/ask-norag")
    public Map<String, Object> askWithoutRag(@RequestParam String q) {
        String answer = plainChatClient.prompt().user(q).call().content();
        return Map.of("mode", "NO-RAG", "question", q, "answer", answer);
    }

    private static String preview(String text) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= 200 ? flat : flat.substring(0, 200) + " ...";
    }
}
