package com.example.rag.tuning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 튜닝 실험 서비스 (교안 8장 2~3절)
 *
 * 핵심: 청크 크기별로 chunkSize 메타데이터를 부여해 "논리적 컬렉션"을 분리한다.
 *       -> 같은 테이블에 적재하되 filterExpression 으로 분리 검색하여 공정 비교가 가능하다.
 */
@Service
public class TuningService {

    private static final Logger log = LoggerFactory.getLogger(TuningService.class);

    private final VectorStore vectorStore;
    private final Resource policyDoc;
    private final List<Integer> chunkSizes;

    public TuningService(VectorStore vectorStore,
                         @Value("classpath:docs/company-policy.txt") Resource policyDoc,
                         @Value("${rag.chunk-sizes:200,500,1000}") String chunkSizesCsv) {
        this.vectorStore = vectorStore;
        this.policyDoc = policyDoc;
        this.chunkSizes = Arrays.stream(chunkSizesCsv.split(","))
                .map(String::trim).map(Integer::parseInt).toList();
    }

    /** 청크 크기별로 각각 적재한다 (chunkSize 메타데이터로 구분) */
    public Map<String, Object> ingestAll() {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int size : chunkSizes) {


//            List<Document> chunks = TokenTextSplitter.builder()
//                    .withChunkSize(size)         // 기본 텍스트 분할 토큰 크기
//                    .withMinChunkSizeChars(100)             // 최소 문자(Char) 단위 크기
//                    .withMinChunkLengthToEmbed(5)           // 임베딩할 최소 길이
//                    .withMaxNumChunks(10000)                // 최대 생성 가능 Chunk 수
//                    .withKeepSeparator(true)               // 구분자(Separator) 유지 여부
//                    .build()
//                    .apply(docs);

            // 적재


//            log.info("적재 완료 [chunkSize={}] -> {}건 (평균 {}자)", size, chunks.size(), Math.round(avgLen));
        }
        return result;
    }

    /** 청크 크기별 검색 결과를 나란히 비교한다 (교안 6장 2절 실습) */
    public Map<String, Object> compare(String question, int topK) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (int size : chunkSizes) {

        }

        result.put("_hint", "작은 청크는 정밀하지만 문맥이 끊기고, 큰 청크는 문맥이 온전하나 노이즈가 섞인다");
        return result;
    }

    /** 검색 파라미터 실험 (교안 8장 3절) */
    public Map<String, Object> search(String question, int topK, double threshold,
                                      Integer chunkSize, String category) {
        SearchRequest.Builder req = null;

        List<String> filters = new ArrayList<>();


        List<Document> found = vectorStore.similaritySearch(req.build());

        return Map.of(
                "question", question,
                "params", Map.of(
                        "topK", topK,
                        "threshold", threshold,
                        "chunkSize", chunkSize == null ? "-" : chunkSize,
                        "category", category == null ? "-" : category),
                "hitCount", found.size(),
                "hits", found.stream().map(d -> Map.<String, Object>of(
                        "score", round(d.getScore()),
                        "text", preview(d.getText()))).toList()
        );
    }

    private static double round(Double score) {
        return score == null ? 0.0 : Math.round(score * 10000) / 10000.0;
    }

    private static String preview(String text) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= 160 ? flat : flat.substring(0, 160) + " ...";
    }
}
