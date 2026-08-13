package com.example.expert.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** 도메인 문서(인스턴스 스펙·요금) 적재 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    /** 이 길이(문자 수)를 넘는 블록만 TokenTextSplitter 로 추가 분할한다. */
    private static final int MAX_BLOCK_CHARS = 800;

    private final VectorStore vectorStore;
    private final Resource specDoc;

    public IngestionService(VectorStore vectorStore,
                            @Value("classpath:docs/cloud-instances.txt") Resource specDoc) {
        this.vectorStore = vectorStore;
        this.specDoc = specDoc;
    }

    public Map<String, Object> ingest() {
        String rawText = readAsString(specDoc);

        // ---------- 1) 문서의 논리적 구조(빈 줄)를 기준으로 1차 분할 ----------
        // "[gp-medium]" 같은 인스턴스 블록, "■ 요금 정책" 섹션 등이
        // 원본 문서에서 빈 줄로 구분되어 있으므로, 이 경계를 그대로 청크 경계로 사용한다.
        // -> 토큰 수 기준 고정 분할과 달리, 한 인스턴스의 스펙/요금/용도가 잘리지 않고 한 청크에 온전히 담긴다.
        String[] blocks = rawText.split("\\r?\\n\\s*\\r?\\n+");

        List<Document> chunks = new ArrayList<>();
        TokenTextSplitter fallbackSplitter = TokenTextSplitter.builder()
                .withChunkSize(350)
                .withMinChunkSizeChars(100)
                .withMinChunkLengthToEmbed(5)
                .withMaxNumChunks(10000)
                .withKeepSeparator(true)
                .build();

        for (String block : blocks) {
            String trimmed = block.strip();
            if (trimmed.isEmpty()) continue;

            Document blockDoc = new Document(trimmed, metadata());

            if (trimmed.length() <= MAX_BLOCK_CHARS) {
                // 블록 하나 = 청크 하나. 인스턴스 정보가 쪼개지지 않는다.
                chunks.add(blockDoc);
            } else {
                // ---------- 2) 예외적으로 블록이 너무 크면 그 블록 안에서만 추가 분할 ----------
                // (문서가 커져서 한 섹션이 지나치게 길어지는 경우를 대비한 안전장치)
                chunks.addAll(fallbackSplitter.apply(List.of(blockDoc)));
            }
        }

        vectorStore.add(chunks);

        log.info("적재 완료: 원본 1건 -> 블록 {}건 -> 청크 {}건", blocks.length, chunks.size());
        return Map.of("documents", 1, "chunks", chunks.size());
    }

    private Map<String, Object> metadata() {
        return Map.of(
                "source", "cloud-instances.txt",
                "category", "인프라스펙",
                "updated", "2025-11-01"
        );
    }

    private String readAsString(Resource resource) {
        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new IllegalStateException("문서를 읽는 중 오류가 발생했습니다: " + resource, e);
        }
    }
}
