package com.example.rag.demo;

import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 데모 (2) 청킹 비교
 *
 * 같은 문서를 청크 크기 200 / 500 / 1000 으로 분할해
 * "정밀 매칭 vs 문맥 보존" 트레이드오프를 눈으로 확인한다.  (교안 11장 7절)
 */
@Component
public class ChunkingDemo {

    private final Resource policyDoc;

    public ChunkingDemo(@Value("classpath:docs/company-policy.txt") Resource policyDoc) {
        this.policyDoc = policyDoc;
    }

    public void run() {
        System.out.println("=== 데모 (2) 청킹 비교 ===\n");

        List<Document> source = new TextReader(policyDoc).get();
        int totalChars = source.stream().mapToInt(d -> d.getText().length()).sum();
        System.out.printf("원본 문서: %d건, 총 %,d자%n%n", source.size(), totalChars);

        for (int chunkSize : new int[]{200, 500, 1000}) {
            TokenTextSplitter splitter = new TokenTextSplitter(
                    chunkSize,  // defaultChunkSize (토큰)
                    100,        // minChunkSizeChars
                    5,          // minChunkLengthToEmbed
                    10000,      // maxNumChunks
                    true        // keepSeparator
            );
            List<Document> chunks = splitter.apply(source);

            double avgLen = chunks.stream().mapToInt(d -> d.getText().length()).average().orElse(0);
            System.out.printf("[청크 크기 %4d 토큰] -> %2d개 청크, 평균 %.0f자%n",
                    chunkSize, chunks.size(), avgLen);
            System.out.println("  첫 청크 미리보기:");
            System.out.println("    " + preview(chunks.get(0).getText()));
            if (chunks.size() > 1) {
                System.out.println("  두번째 청크 미리보기:");
                System.out.println("    " + preview(chunks.get(1).getText()));
            }
            System.out.println();
        }

        System.out.println("""
                [관찰 포인트]
                  - 작은 청크 : 조각이 정밀하지만 조항 중간에서 끊길 수 있다 (문맥 단절)
                  - 큰 청크   : 문맥은 온전하지만 무관한 내용이 함께 딸려온다 (노이즈)
                  - 정답 크기는 없다. 문서 성격에 따라 다르며 Section 8에서 '평가'로 찾는다.
                """);
    }

    private static String preview(String text) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= 90 ? flat : flat.substring(0, 90) + " ...";
    }
}
