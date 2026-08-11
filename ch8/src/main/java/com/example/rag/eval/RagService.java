package com.example.rag.eval;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 평가 대상 RAG 서비스.
 *
 * Advisor 대신 직접 검색하는 이유: 평가에는 "그때 사용된 근거 문서"가 반드시 필요하기 때문.
 * (교안 8장 7절 - EvaluationRequest 에 docs 를 넘겨야 한다)
 */
@Service
public class RagService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final int topK;
    private final double threshold;
    private final int activeChunkSize;

    public RagService(ChatClient.Builder builder,
                      VectorStore vectorStore,
                      @Value("${rag.top-k:4}") int topK,
                      @Value("${rag.similarity-threshold:0.0}") double threshold,
                      @Value("${rag.active-chunk-size:500}") int activeChunkSize) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.topK = topK;
        this.threshold = threshold;
        this.activeChunkSize = activeChunkSize;
    }

    /** 검색 + 생성. 사용된 근거를 함께 반환한다. */
    public RagResult ask(String question) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(question)
                        .topK(topK)
                        .similarityThreshold(threshold)
                        .filterExpression("chunkSize == '" + activeChunkSize + "'")
                        .build());

        if (docs.isEmpty()) {
            return new RagResult(question, "제공된 문서에서 확인할 수 없습니다.", List.of(), 0);
        }

        String context = docs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));

        String answer = chatClient.prompt()
                .system("""
                        너는 사내 규정 안내 도우미다.
                        아래 [근거]에 있는 내용만 사용해 한국어로 간결하게 답하라.
                        근거에 없는 내용은 절대 추측하지 말고
                        정확히 "제공된 문서에서 확인할 수 없습니다."라고만 답하라.""")
                .user(u -> u.text("[근거]\n{ctx}\n\n[질문]\n{q}")
                        .param("ctx", context)
                        .param("q", question))
                .call()
                .content();

        int contextTokensApprox = context.length() / 2;   // 한국어 근사치 (실측 대용)
        return new RagResult(question, answer, docs, contextTokensApprox);
    }

    public record RagResult(String question, String answer, List<Document> evidences, int contextTokensApprox) {}
}
