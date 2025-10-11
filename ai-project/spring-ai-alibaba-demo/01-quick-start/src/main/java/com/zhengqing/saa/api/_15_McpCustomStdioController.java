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
@Tag(name = "15-MCP-自定义mcp服务--STDIO")
public class _15_McpCustomStdioController {

    private ChatClient chatClient;

    public _15_McpCustomStdioController(DashScopeChatModel dashScopeChatModel,
                                        ToolCallbackProvider tools) {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultToolCallbacks(tools) // 注入MCP工具
                .build();
    }


    /**
     * http://localhost:888/mcp/custom/stdio?msg=成都天气?
     */
    @GetMapping("/stdio")
    public Flux<String> stdio(@RequestParam String msg) {
        return chatClient.prompt().user(msg)
                .stream().content();
    }

}
