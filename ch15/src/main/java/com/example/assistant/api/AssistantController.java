package com.example.assistant.api;

import com.example.assistant.guardrail.InputGuardrail;
import com.example.assistant.rag.SpringAiRagEngine;
import com.example.assistant.routing.DifyEngine;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Section 15 통합 API — 사내 문서 AI Assistant (교안 15장)
 *
 *  POST /api/assistant/ingest          경로 A(Spring AI) 문서 적재
 *  POST /api/assistant/ask?route=A|B   가드레일 → 선택한 경로로 질의
 *  POST /api/assistant/compare         같은 질문을 A/B 두 경로에 던져 비교 ★
 *
 * 핵심 설계 (교안 15장 1절):
 *   - 가드레일이 "최상단" — A/B 어느 경로로 가든 먼저 동작
 *   - 같은 질문을 A/B 로 보낼 수 있어 이 구조 자체가 비교 실험 도구가 된다
 */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final InputGuardrail guardrail;
    private final SpringAiRagEngine ragEngine;   // 경로 A
    private final DifyEngine difyEngine;         // 경로 B

    public AssistantController(InputGuardrail guardrail,
                               SpringAiRagEngine ragEngine,
                               DifyEngine difyEngine) {
        this.guardrail = guardrail;
        this.ragEngine = ragEngine;
        this.difyEngine = difyEngine;
    }

    @PostMapping("/ingest")
    public Map<String, Object> ingest() {
        return ragEngine.ingest();
    }

    /** 가드레일 통과 후 선택한 경로로 질의 */
    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestParam String q,
                                   @RequestParam(defaultValue = "A") String route,
                                   @RequestParam(defaultValue = "demo-user") String user) {
        // ① 가드레일 — 최상단 (교안 15장 2절)
        if (guardrail.shouldBlock(q)) {
            return Map.of("blocked", true, "answer", "요청을 처리할 수 없습니다.");
        }

        // ② 경로 선택
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("route", route);
        result.put("question", q);

        if ("B".equalsIgnoreCase(route)) {
            DifyEngine.Answer a = difyEngine.ask(q, user);
            result.put("engine", "Dify (경로 B)");
            result.put("answer", a.text());
            result.put("mocked", a.mocked());
            result.put("fellBack", a.fellBack());
        } else {
            SpringAiRagEngine.Answer a = ragEngine.ask(q);
            result.put("engine", "Spring AI RAG (경로 A)");
            result.put("answer", a.text());
            result.put("sources", a.sources());
        }
        return result;
    }

    /** A/B 나란히 비교 (교안 15장 3절) */
    @PostMapping("/compare")
    public Map<String, Object> compare(@RequestParam String q,
                                       @RequestParam(defaultValue = "demo-user") String user) {
        if (guardrail.shouldBlock(q)) {
            return Map.of("blocked", true, "answer", "요청을 처리할 수 없습니다.");
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("question", q);

        long t1 = System.currentTimeMillis();
        SpringAiRagEngine.Answer a = ragEngine.ask(q);
        long aMs = System.currentTimeMillis() - t1;

        long t2 = System.currentTimeMillis();
        DifyEngine.Answer b = difyEngine.ask(q, user);
        long bMs = System.currentTimeMillis() - t2;

        result.put("pathA_springAI", Map.of("answer", a.text(), "sources", a.sources(), "latencyMs", aMs));
        result.put("pathB_dify", Map.of("answer", b.text(), "mocked", b.mocked(), "latencyMs", bMs));
        result.put("hint", "결론은 '누가 이겼나'가 아니라 '어떤 상황에 무엇이 유리한가' - 교안 15장 3절");
        return result;
    }
}
