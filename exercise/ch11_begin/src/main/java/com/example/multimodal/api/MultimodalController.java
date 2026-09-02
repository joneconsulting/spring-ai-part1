package com.example.multimodal.api;

import com.example.multimodal.service.AudioService;
import com.example.multimodal.service.ImageGenService;
import com.example.multimodal.service.VisionService;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Section 11 실습 API — 4개 모달 데모
 *
 *  GET  /api/image?prompt=...          이미지 생성 → URL
 *  POST /api/vision/describe (file)     이미지 설명
 *  POST /api/vision/receipt  (file)     영수증 → 구조화 객체 ★
 *  POST /api/stt             (file)     음성 → 텍스트
 *  GET  /api/tts?text=...               텍스트 → 음성(mp3)
 *
 * 모두 같은 패턴(주입받아 call)이라는 점을 강조한다. (교안 11장 1절)
 */
@RestController
public class MultimodalController {

    private final ImageGenService imageGenService;
    private final VisionService visionService;
    private final AudioService audioService;

    public MultimodalController(ImageGenService imageGenService,
                                VisionService visionService,
                                AudioService audioService) {
        this.imageGenService = imageGenService;
        this.visionService = visionService;
        this.audioService = audioService;
    }

    @GetMapping("/api/image")
    public Map<String, Object> generateImage(@RequestParam String prompt) {
        return null;
    }

    @PostMapping("/api/vision/describe")
    public Map<String, Object> describe(@RequestParam("file") MultipartFile file) throws IOException {
        return null;
    }

    @PostMapping("/api/vision/receipt")
    public VisionService.Receipt extractReceipt(@RequestParam("file") MultipartFile file) throws IOException {
        return null;
    }

    @PostMapping("/api/stt")
    public Map<String, Object> transcribe(@RequestParam("file") MultipartFile file) throws IOException {
        return null;
    }

    @GetMapping("/api/tts")
    public ResponseEntity<byte[]> tts(@RequestParam String text) {
        return null;
    }
}
