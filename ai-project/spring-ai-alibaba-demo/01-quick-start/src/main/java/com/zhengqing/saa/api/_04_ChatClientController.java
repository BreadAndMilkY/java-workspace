package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat-client")
@Tag(name = "04-ChatClient")
public class _04_ChatClientController {

    private final ChatClient chatClient;

    public _04_ChatClientController(DashScopeChatModel dashScopeChatModel) {
        // 如果引入多个大模型依赖，需要指定具体模型
        chatClient = ChatClient.builder(dashScopeChatModel).build();
    }

    /**
     * 简单调用
     * http://localhost:888/chat-client/simple/chat?msg=你是谁？
     */
    @Operation(summary = "简单调用")
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatClient.prompt().user(msg).call().content();
    }

    /**
     * 流式调用
     * http://localhost:888/chat-client/stream/chat?msg=你是谁？
     */
    @Operation(summary = "流式调用")
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatClient.prompt().user(msg).stream().content();
    }

}
