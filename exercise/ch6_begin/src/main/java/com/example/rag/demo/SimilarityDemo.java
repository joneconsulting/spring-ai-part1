package com.example.rag.demo;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 데모 (1) 임베딩 유사도
 *
 * 교안 10장의 "고양이 / 강아지 / 동물 / 자동차" 예시를 실제 수치로 확인한다.
 * 슬라이드의 예시값(0.86 / 0.79 / 0.21)과 비교하며
 * "절대값이 아니라 순위와 격차가 의미 있다"는 감각을 잡는 것이 목적.
 */
@Component
public class SimilarityDemo {

    private final EmbeddingModel embeddingModel;

    public SimilarityDemo(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public void run() {
        System.out.println("=== 데모 (1) 임베딩 유사도 ===\n");

        String query = "고양이";
        Map<String, String> candidates = new LinkedHashMap<>();
        candidates.put("강아지", "강아지");
        candidates.put("동물", "동물");
        candidates.put("반려묘를 키우는 법", "반려묘를 키우는 법");
        candidates.put("자동차", "자동차");
        candidates.put("주식 투자 전략", "주식 투자 전략");

        // embedding

        System.out.println("""

                [관찰 포인트]
                  1) 단어가 완전히 달라도 의미가 가까우면 유사도가 높다  (고양이 vs 강아지)
                  2) 무관한 주제는 확연히 낮다                          (고양이 vs 주식)
                  3) 절대값은 모델마다 다르다 - 중요한 것은 '순위와 격차'
                     -> Section 8에서 similarityThreshold 를 자기 데이터로 캘리브레이션해야 하는 이유
                """);
    }

    /** 코사인 유사도 = (A dot B) / (|A| * |B|) */
    static double cosineSimilarity(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na += a[i] * a[i];
            nb += b[i] * b[i];
        }
        return dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    private static String bar(double sim) {
        int n = (int) Math.round(Math.max(0, sim) * 40);
        return "#".repeat(n);
    }
}
