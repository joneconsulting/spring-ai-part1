package com.example.tool.api;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Section 10 Part 2 실습 API — MCP 클라이언트
 *
 *  GET /api/mcp/ask   MCP 서버가 제공하는 도구를 사용해 질문에 답한다
 *
 * 교안 10장 7절: 우리가 작성한 도구 코드는 0줄이다. @Tool 은 코드를 쓰지만 MCP 는 설정을 쓴다.
 *
 * ※ MCP_ENABLED=true 이고 Node.js(npx) 가 설치되어 있어야 동작한다.
 *   MCP 를 쓰지 않으면 ToolCallbackProvider 빈이 없을 수 있으므로 ObjectProvider 로 선택 주입한다.
 */
@RestController
public class McpController {

    private final ChatClient.Builder builder;
    // spring-ai-starter-mcp-client 가 활성화되면 ToolCallbackProvider 빈이 등록된다.
    private final ObjectProvider<ToolCallbackProvider> mcpToolProvider;

    public McpController(ChatClient.Builder builder,
                         ObjectProvider<ToolCallbackProvider> mcpToolProvider) {
        this.builder = builder;
        this.mcpToolProvider = mcpToolProvider;
    }

    @GetMapping("/api/mcp/ask")
    public Map<String, Object> ask(@RequestParam String q) {
        return null;
    }
}
