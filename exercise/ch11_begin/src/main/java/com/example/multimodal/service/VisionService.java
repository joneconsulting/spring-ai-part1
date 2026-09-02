package com.example.multimodal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.util.List;

/**
 * Vision — 이미지를 이해하는 LLM (교안 11장 3절)
 *
 * 이 섹션에서 실무 가치가 가장 높다.
 * ChatClient 에 media 로 이미지를 첨부하기만 하면 되고,
 * 구조화 응답(.entity())과 결합하면 "이미지 -> 객체" 파이프라인이 완성된다.
 */
@Service
public class VisionService {

    private static final Logger log = LoggerFactory.getLogger(VisionService.class);

    private final ChatClient chatClient;

    public VisionService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    /** 이미지를 자연어로 설명 */
    public String describe(Resource image) {
        log.info("[Vision] describe - {}", image.getFilename());

        return null;
    }

    /**
     * 영수증 이미지에서 정보를 "구조화 객체"로 추출한다. (교안 11장 3절)
     * 이것이 Vision + 구조화 응답의 핵심 — 이미지에서 바로 Receipt 객체로 매핑된다.
     */
    public Receipt extractReceipt(Resource image) {
        log.info("[Vision] extractReceipt - {}", image.getFilename());

        return null;   // ← 이미지 -> 객체
    }

    /** 영수증 구조화 모델 */
    public record Receipt(
            String storeName,
            String date,
            Integer totalAmount,
            List<String> items
    ) {}
}
