package com.example.rag.demo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 데모 (3) 미니 RAG
 *
 * DB 없이 SimpleVectorStore(인메모리)만으로 RAG 전체 흐름을 한 번에 관찰한다.
 *   적재: 로딩 -> 청킹 -> 임베딩 -> 저장
 *   질의: 질문 임베딩 -> 유사도 검색 -> 근거 주입 -> 생성
 *
 * Section 7에서는 이 구조를 pgvector 기반으로 확장한다.
 */
@Component
public class MiniRagDemo {

    private final ChatClient chatClient;
    private final EmbeddingModel embeddingModel;
    private final Resource policyDoc;

    public MiniRagDemo(ChatClient.Builder builder,
                       EmbeddingModel embeddingModel,
                       @Value("classpath:docs/company-policy.txt") Resource policyDoc) {
        this.chatClient = builder.build();
        this.embeddingModel = embeddingModel;
        this.policyDoc = policyDoc;
    }

    public void run() {
        System.out.println("=== 데모 (3) 미니 RAG (SimpleVectorStore) ===\n");

        // ---------- 1. 적재 ----------
        System.out.println("[1/3] 적재 중...");
        TextReader reader = new TextReader(policyDoc);
        reader.getCustomMetadata().put("source", "company-policy.txt");
        List<Document> docs = reader.get();

        List<Document> chunks = new TokenTextSplitter(400, 100, 5, 10000, true).apply(docs);
        VectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        vectorStore.add(chunks);
        System.out.printf("      원본 %d건 -> 청크 %d건 적재 완료%n%n", docs.size(), chunks.size());

        String question = "연차 휴가는 며칠이고 언제까지 사용해야 하나요?";

        // ---------- 2. 검색 ----------
        System.out.println("[2/3] 유사도 검색 (LLM 호출 전 - 근거 확인)");
        System.out.println("      질문: " + question);
        List<Document> found = vectorStore.similaritySearch(
                SearchRequest.builder().query(question).topK(3).build());

        for (int i = 0; i < found.size(); i++) {
            Document d = found.get(i);
            System.out.printf("      근거 %d) [%s] %s%n",
                    i + 1,
                    d.getMetadata().getOrDefault("source", "-"),
                    preview(d.getText()));
        }
        System.out.println();

        // ---------- 3. 생성 ----------
        System.out.println("[3/3] 근거를 주입해 답변 생성");
        String context = found.stream().map(Document::getText).collect(Collectors.joining("\n---\n"));

        String withRag = chatClient.prompt()
                .system("""
                        너는 사내 규정 안내 도우미다.
                        아래 [근거]에 있는 내용만 사용해 한국어로 간결하게 답하라.
                        근거에 없으면 "제공된 문서에서 확인할 수 없습니다"라고 답하라.""")
                .user(u -> u.text("[근거]\n{ctx}\n\n[질문]\n{q}")
                        .param("ctx", context)
                        .param("q", question))
                .call()
                .content();

        String withoutRag = chatClient.prompt().user(question).call().content();

        System.out.println("\n--- RAG 적용 답변 ---");
        System.out.println(withRag);
        System.out.println("\n--- RAG 미적용 답변 (비교용) ---");
        System.out.println(withoutRag);

        System.out.println("""

                [관찰 포인트]
                  - RAG 적용: 우리 문서의 구체적 조항을 인용한다
                  - RAG 미적용: 일반론에 그치거나 사실과 다를 수 있다 (환각)
                  - 이 대비가 RAG의 존재 이유다.
                """);
    }

    private static String preview(String text) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= 80 ? flat : flat.substring(0, 80) + " ...";
    }
}
