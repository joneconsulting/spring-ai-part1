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

       // chunking

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
