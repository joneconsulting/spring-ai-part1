package com.example.expert.api;

import com.example.expert.domain.ExpertResponse;
import com.example.expert.domain.Requirement;
import com.example.expert.service.AiExpertService;
import com.example.expert.service.IngestionService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Section 9 실습 API
 *
 *  POST /api/expert/ingest      인스턴스 스펙·요금 문서 적재
 *  POST /api/expert/recommend   구조화 판정 + 근거 + HITL 라우팅
 */
@RestController
@RequestMapping("/api/expert")
public class ExpertController {

    private final IngestionService ingestionService;
    private final AiExpertService aiExpertService;

    public ExpertController(IngestionService ingestionService, AiExpertService aiExpertService) {
        this.ingestionService = ingestionService;
        this.aiExpertService = aiExpertService;
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest() {
        return ingestionService.ingest();
    }

    @PostMapping("/recommend")
    public ExpertResponse recommend(@RequestBody Requirement requirement) {
        return aiExpertService.recommend(requirement);
    }
}
