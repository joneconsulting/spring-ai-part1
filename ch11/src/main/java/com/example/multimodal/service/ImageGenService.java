package com.example.multimodal.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.stereotype.Service;

/**
 * 이미지 생성 — ImageModel (교안 11장 2절)
 *
 * ChatModel 과 완전히 같은 패턴: 주입받아서 call().
 * 실무 주의: 반환 URL 은 만료되므로 즉시 저장, 비용·지연이 크므로 초안 생성 용도.
 */
@Service
public class ImageGenService {

    private static final Logger log = LoggerFactory.getLogger(ImageGenService.class);

    private final ImageModel imageModel;

    public ImageGenService(ImageModel imageModel) {
        this.imageModel = imageModel;
    }

    /** 프롬프트로 이미지를 생성하고 URL 또는 base64 데이터를 반환한다. */
    public String generate(String prompt) {
        log.info("[Image] generate - prompt='{}'", prompt);

        ImageResponse res = imageModel.call(new ImagePrompt(prompt,
                OpenAiImageOptions.builder()
                        .model("gpt-image-1-mini")
                        .responseFormat("b64_json")
                        .size("1024x1024")
                        .build()));

        if (res == null || res.getResult() == null || res.getResult().getOutput() == null) {
            throw new IllegalStateException("Image generation failed. Check OpenAI image model access and request payload.");
        }

        String url = res.getResult().getOutput().getUrl();
        if (url == null || url.isBlank()) {
            String base64 = res.getResult().getOutput().getB64Json();
            if (base64 == null || base64.isBlank()) {
                throw new IllegalStateException("Image generation response did not contain either URL or base64 data.");
            }
            url = "data:image/png;base64," + base64;
        }

        log.info("[Image] 생성 완료: {}", url);
        return url;
    }
}
