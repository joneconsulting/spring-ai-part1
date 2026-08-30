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
        /* 구현 */

        String question = "연차 휴가는 며칠이고 언제까지 사용해야 하나요?";

        // ---------- 2. 검색 ----------
        System.out.println("[2/3] 유사도 검색 (LLM 호출 전 - 근거 확인)");
        System.out.println("      질문: " + question);
        /* 구현 */
        System.out.println();

        // ---------- 3. 생성 ----------
        System.out.println("[3/3] 근거를 주입해 답변 생성");
        /* 구현 */

        String withRag = "";

        String withoutRag = "";

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
