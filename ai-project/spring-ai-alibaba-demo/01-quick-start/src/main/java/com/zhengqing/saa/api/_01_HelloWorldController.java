package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/helloworld")
@Tag(name = "01-helloworld")
public class _01_HelloWorldController {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @GetMapping("/api-key") // http://localhost:888/api-key
    public Object apiKey() {
        return apiKey;
    }

    @Resource(type = DashScopeChatModel.class)
    private ChatModel chatModel;

    /**
     * 简单调用
     * http://localhost:888/helloworld/simple/chat?msg=你是谁？
     */
    @Operation(summary = "简单调用")
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatModel.call(msg);
    }

    /**
     * 流式调用
     * http://localhost:888/helloworld/stream/chat?msg=你是谁？
     */
    @Operation(summary = "流式调用")
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatModel.stream(msg);
    }

}
