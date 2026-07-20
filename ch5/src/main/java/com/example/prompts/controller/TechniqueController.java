package com.example.prompts.controller;

import com.example.prompts.dto.BeforeAfterResponse;
import com.example.prompts.service.ReasoningService;
import com.example.prompts.service.SentimentClassifyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/** S10·S11 Zero-shot vs Few-shot · S12 CoT · S13 기법 선택 치트시트 */
@RestController
@RequestMapping("/api/s05/technique")
public class TechniqueController {

    private final SentimentClassifyService sentimentService;
    private final ReasoningService reasoningService;

    public TechniqueController(SentimentClassifyService sentimentService,
                               ReasoningService reasoningService) {
        this.sentimentService = sentimentService;
        this.reasoningService = reasoningService;
    }

    // ── S10·S11 ───────────────────────────────────────────────

    /** Zero-shot — 장황한 응답이 나오는 것을 먼저 확인 */
    @GetMapping("/zero-shot")
    public Map<String, String> zeroShot(
            @RequestParam(defaultValue = "가격 대비 성능은 괜찮은데 배터리가 하루도 못 갑니다") String review) {
        return Map.of("review", review, "answer", sentimentService.zeroShot(review));
    }

    /** Few-shot — 한 단어 출력으로 수렴 */
    @GetMapping("/few-shot")
    public Map<String, String> fewShot(
            @RequestParam(defaultValue = "가격 대비 성능은 괜찮은데 배터리가 하루도 못 갑니다") String review) {
        return Map.of("review", review, "answer", sentimentService.fewShot(review));
    }

    /** 나란히 비교 — "예시가 곧 사양(Spec)" */
    @GetMapping("/shot/compare")
    public BeforeAfterResponse shotCompare(
            @RequestParam(defaultValue = "가격 대비 성능은 괜찮은데 배터리가 하루도 못 갑니다") String review) {
        return sentimentService.compare(review);
    }

    // ── S12 ───────────────────────────────────────────────────

    /** CoT 유/무 비교 — 조건 3개 이상이 얽힌 판단 */
    @GetMapping("/cot/compare")
    public BeforeAfterResponse cotCompare(@RequestParam(required = false) String problem) {
        return reasoningService.compare(
                problem == null ? ReasoningService.SAMPLE_PROBLEM : problem);
    }

    // ── S13 ───────────────────────────────────────────────────

    /** 기법 선택 치트시트 — 증상으로 찾아 쓰는 처방전 (다운로드 자료와 동일 내용) */
    @GetMapping("/cheatsheet")
    public List<Map<String, String>> cheatsheet() {
        return List.of(
                entry("출력이 들쭉날쭉하다", "출력 형식 명시 + Few-shot 예시",
                        "형식은 설명보다 예시가 고정", "/api/s05/technique/shot/compare"),
                entry("답변 관점이 어긋난다", "역할(Persona) 부여",
                        "전문성 수준과 관점 고정", "/api/s05/role/separated"),
                entry("없는 사실을 지어낸다", "제약 명시 (\"모르면 모른다고\")",
                        "환각의 1차 방어선", "/api/s05/system-prompt/out-of-policy"),
                entry("복잡한 판단을 틀린다", "Chain-of-Thought",
                        "단계별 검토 후 결론", "/api/s05/technique/cot/compare"),
                entry("도메인 분류가 부정확하다", "Few-shot 2~5개",
                        "우리 도메인의 정답 예시 제공", "/api/s05/technique/few-shot"),
                entry("프롬프트가 코드에 흩어져 있다", "Template + 외부 파일 분리",
                        "리뷰·버전 관리 대상으로", "/api/s05/template/prompt-file"));
    }

    private Map<String, String> entry(String symptom, String prescription, String why, String tryIt) {
        return Map.of("증상", symptom, "처방", prescription, "이유", why, "실습", tryIt);
    }
}
