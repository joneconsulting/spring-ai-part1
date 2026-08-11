package com.example.rag.ingest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 적재 파이프라인:  Reader -> Splitter -> VectorStore.add()
 * 교안 10장 5, 8, 9, 10절에 해당한다.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final VectorStore vectorStore;
    private final Resource policyDoc;
    private final Resource contractDoc;
    private final int chunkSize;

    public IngestionService(VectorStore vectorStore,
                            @Value("classpath:docs/company-policy.txt") Resource policyDoc,
                            @Value("classpath:docs/sample-contract.pdf") Resource contractDoc,
                            @Value("${rag.chunk-size:500}") int chunkSize) {
        this.vectorStore = vectorStore;
        this.policyDoc = policyDoc;
        this.contractDoc = contractDoc;
        this.chunkSize = chunkSize;
    }

    /** 기본 샘플 문서 적재 */
    public IngestResult ingestSample() {
        // 1) 로딩 : 파일 1개 = Document 1개
        TextReader reader = new TextReader(policyDoc);
        // 메타데이터 설계 (교안 10장) - 적재 시점에 넣지 않으면 검색 시점에 쓸 수 없다
        reader.getCustomMetadata().putAll(Map.of(
                "source", "company-policy.txt",
                "category", "인사규정",
                "department", "인사팀",
                "updated", "2025-11-01"
        ));
        List<Document> docs = reader.get();

        // 2) 청킹
        List<Document> chunks = split(docs);

        // 3) 임베딩 + 저장 (add() 내부에서 EmbeddingModel 호출)
        vectorStore.add(chunks);

        log.info("적재 완료: 원본 {}건 -> 청크 {}건 (chunkSize={})",
                docs.size(), chunks.size(), chunkSize);
        return new IngestResult(docs.size(), chunks.size(), chunkSize);
    }

    /** PDF 적재 - 페이지 단위 Document + page 메타데이터 자동 부여 */
    public IngestResult ingestPdf() {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(contractDoc,
                PdfDocumentReaderConfig.builder()
                        .withPagesPerDocument(1)
                        .build());
        List<Document> docs = reader.get();

        // 추출 품질 검수 (교안 10장) - 스캔본 PDF는 텍스트가 비어 조용히 실패한다
        long emptyPages = docs.stream()
                .filter(d -> d.getText() == null || d.getText().isBlank())
                .count();
        if (emptyPages > 0) {
            log.warn("경고: 텍스트가 비어 있는 페이지 {}건 - 스캔본 PDF일 수 있습니다 (OCR 필요)", emptyPages);
        }

        docs.forEach(d -> d.getMetadata().put("source", "sample-contract.pdf"));
        List<Document> chunks = split(docs);
        vectorStore.add(chunks);

        log.info("PDF 적재 완료: {} - 페이지 {}건 -> 청크 {}건", "sample-contract.pdf", docs.size(), chunks.size());
        return new IngestResult(docs.size(), chunks.size(), chunkSize);
    }

    private List<Document> split(List<Document> docs) {
//        TokenTextSplitter splitter = new TokenTextSplitter(
//                chunkSize,  // defaultChunkSize (토큰)
//                100,        // minChunkSizeChars
//                5,          // minChunkLengthToEmbed
//                10000,      // maxNumChunks
//                true        // keepSeparator
//        );
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(chunkSize)         // 기본 텍스트 분할 토큰 크기
                .withMinChunkSizeChars(100)             // 최소 문자(Char) 단위 크기
                .withMinChunkLengthToEmbed(5)           // 임베딩할 최소 길이
                .withMaxNumChunks(10000)                // 최대 생성 가능 Chunk 수
                .withKeepSeparator(true)               // 구분자(Separator) 유지 여부
                .build();

        return splitter.apply(docs);   // 메타데이터는 청크에 자동 승계된다
    }

    public record IngestResult(int documents, int chunks, int chunkSize) {}
}
