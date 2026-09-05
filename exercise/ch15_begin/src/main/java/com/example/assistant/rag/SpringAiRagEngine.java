package com.example.assistant.rag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 경로 A — Spring AI 직접 구현 엔진 (Section 6~8 통합)
 *
 * pgvector RAG. 적재 → 검색 → 생성(근거 반환).
 */
@Component
public class SpringAiRagEngine {

    private static final Logger log = LoggerFactory.getLogger(SpringAiRagEngine.class);

    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final Resource policyDoc;
    private final int topK;

    public SpringAiRagEngine(ChatClient.Builder builder,
                             VectorStore vectorStore,
                             @Value("classpath:docs/company-policy.txt") Resource policyDoc,
                             @Value("${assistant.rag.top-k:4}") int topK) {
        this.chatClient = builder.build();
        this.vectorStore = vectorStore;
        this.policyDoc = policyDoc;
        this.topK = topK;
    }

    /** 문서 적재 */
    public Map<String, Object> ingest() {

//        log.info("[경로A] 적재 완료: 원본 {}건 -> 청크 {}건", docs.size(), chunks.size());
        return null;
    }

    /** RAG 질의응답 (근거 포함) */
    public Answer ask(String question) {
        List<Document> docs = null;

        if (docs.isEmpty()) {
            return new Answer("제공된 문서에서 확인할 수 없습니다.", List.of());
        }

        String context = null;
        String answer = null;

        List<String> sources = null;

        return new Answer(answer, sources);
    }

    public record Answer(String text, List<String> sources) {}
}
