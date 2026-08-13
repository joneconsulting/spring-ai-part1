package com.example.tool.api;

import com.example.tool.tools.OrderTools;
import com.example.tool.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Section 10 Part 1 실습 API — Tool Calling
 *
 *  GET /api/tool/ask       Tool 을 사용할 수 있는 ChatClient 에 질문
 *  GET /api/tool/ask-notool 비교용 — Tool 없이 답변 (실시간 정보 못 가져옴)
 *
 * 교안 10장 1절: LLM 은 "호출 요청"만 반환하고 실행은 우리 앱이 한다.
 *              LLM 호출이 2회 발생한다(판단 + 최종 생성).
 */
@RestController
public class ToolController {

    private final ChatClient toolChatClient;
    private final ChatClient plainChatClient;

    public ToolController(ChatClient.Builder builder,
                          WeatherTools weatherTools,
                          OrderTools orderTools) {
        // 두 종류의 Tool 을 등록한 ChatClient
        this.toolChatClient = builder.clone()
                .defaultSystem("""
                        너는 친절한 고객 지원 도우미다.
                        날씨나 주문 조회가 필요하면 반드시 제공된 도구를 사용해라.
                        도구가 반환한 사실만으로 답하고, 추측하지 마라.""")
                .defaultTools(weatherTools, orderTools)
                .build();

        // 비교용 — Tool 없음
        this.plainChatClient = builder.clone().build();
    }

    @GetMapping("/api/tool/ask")
    public Map<String, Object> ask(@RequestParam String q) {
        String answer = toolChatClient.prompt().user(q).call().content();
        return Map.of("mode", "WITH-TOOLS", "question", q, "answer", answer);
    }

    @GetMapping("/api/tool/ask-notool")
    public Map<String, Object> askWithoutTool(@RequestParam String q) {
        String answer = plainChatClient.prompt().user(q).call().content();
        return Map.of("mode", "NO-TOOLS", "question", q, "answer", answer);
    }
}
