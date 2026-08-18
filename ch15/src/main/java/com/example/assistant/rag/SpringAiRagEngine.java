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
        TextReader reader = new TextReader(policyDoc);
        reader.getCustomMetadata().putAll(Map.of(
                "source", "company-policy.txt", "category", "인사규정"));
        List<Document> docs = reader.get();
        List<Document> chunks = TokenTextSplitter.builder()
                .withChunkSize(500)         // 기본 텍스트 분할 토큰 크기
                .withMinChunkSizeChars(100)             // 최소 문자(Char) 단위 크기
                .withMinChunkLengthToEmbed(5)           // 임베딩할 최소 길이
                .withMaxNumChunks(10000)                // 최대 생성 가능 Chunk 수
                .withKeepSeparator(true)               // 구분자(Separator) 유지 여부
                .build()
                .apply(docs);

        vectorStore.add(chunks);
        log.info("[경로A] 적재 완료: 원본 {}건 -> 청크 {}건", docs.size(), chunks.size());
        return Map.of("documents", docs.size(), "chunks", chunks.size());
    }

    /** RAG 질의응답 (근거 포함) */
    public Answer ask(String question) {
        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(topK).build());

        if (docs.isEmpty()) {
            return new Answer("제공된 문서에서 확인할 수 없습니다.", List.of());
        }

        String context = docs.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));
        String answer = chatClient.prompt()
                .system("""
                        너는 사내 규정 안내 도우미다.
                        아래 [근거]에 있는 내용만 사용해 한국어로 간결하게 답하라.
                        근거에 없으면 "제공된 문서에서 확인할 수 없습니다"라고 답하라.
                        답변 끝에 근거 조항을 표기하라.""")
                .user(u -> u.text("[근거]\n{ctx}\n\n[질문]\n{q}")
                        .param("ctx", context).param("q", question))
                .call()
                .content();

        List<String> sources = docs.stream()
                .map(d -> String.valueOf(d.getMetadata().getOrDefault("source", "-")))
                .distinct().toList();
        return new Answer(answer, sources);
    }

    public record Answer(String text, List<String> sources) {}
}
