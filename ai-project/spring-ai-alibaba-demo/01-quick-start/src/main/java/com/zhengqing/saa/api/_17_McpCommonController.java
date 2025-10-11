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
@RequestMapping("/mcp/common")
@Tag(name = "17-MCP-外部通用mcp")
public class _17_McpCommonController {

    private ChatClient chatClient;

    public _17_McpCommonController(DashScopeChatModel dashScopeChatModel,
                                   ToolCallbackProvider tools) {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultToolCallbacks(tools) // 注入MCP工具
                .build();
    }


    /**
     * http://localhost:888/mcp/common/chat?msg=新增redis缓存数据,key为name,value为666
     * http://localhost:888/mcp/common/chat?msg=查询redis缓存中的name值
     */
    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam String msg) {
        return chatClient.prompt().user(msg)
                .stream().content();
    }

}
