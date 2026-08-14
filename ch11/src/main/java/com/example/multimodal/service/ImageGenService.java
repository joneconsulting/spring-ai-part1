package com.example.multimodal.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.UUID;

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

    /**
     * 프롬프트로 이미지를 생성하고, base64 응답이면 파일로 저장한 뒤 파일 경로를 반환한다.
     */
    public String generate(String prompt) {
        log.info("[Image] generate - prompt='{}'", prompt);

        ImageResponse res = imageModel.call(new ImagePrompt(prompt,
                OpenAiImageOptions.builder()
                        .model("gpt-image-1-mini")
                        .size("1024x1024")
                        .build()));

        if (res == null || res.getResult() == null || res.getResult().getOutput() == null) {
            throw new IllegalStateException("Image generation failed. Check OpenAI image model access and request payload.");
        }

        String imageUrl = res.getResult().getOutput().getUrl();
        String base64 = res.getResult().getOutput().getB64Json();

        if ((imageUrl == null || imageUrl.isBlank()) && (base64 == null || base64.isBlank())) {
            throw new IllegalStateException("Image generation response did not contain either URL or base64 data.");
        }

        if (imageUrl == null || imageUrl.isBlank()) {
            try {
                String fileName = UUID.randomUUID() + ".png";
                Path uploadDir = Paths.get("uploads");
                Files.createDirectories(uploadDir);

                byte[] imageBytes = Base64.getDecoder().decode(base64);
                Path target = uploadDir.resolve(fileName);
                Files.write(target, imageBytes);

                imageUrl = "/images/" + fileName;
                log.info("[Image] base64 -> file saved: {}", target);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to save generated image to file.", e);
            }
        }

        log.info("[Image] 생성 완료: {}", imageUrl);
        return imageUrl;
    }
}
