package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/mcp/custom")
@Tag(name = "16-MCP-自定义mcp服务--SSE")
public class _16_McpCustomSseController {

    private ChatClient chatClient;

    public _16_McpCustomSseController(DashScopeChatModel dashScopeChatModel,
                                      ToolCallbackProvider tools) {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultToolCallbacks(tools) // 注入MCP工具
                .build();
    }


    /**
     * http://localhost:888/mcp/custom/sse?msg=现在时间?
     */
    @GetMapping("/sse")
    public Flux<String> sse(@RequestParam String msg) {
        return chatClient.prompt().user(msg)
                .stream().content();
    }

}
