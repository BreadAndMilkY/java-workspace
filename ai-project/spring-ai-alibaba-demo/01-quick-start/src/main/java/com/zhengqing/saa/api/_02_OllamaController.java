package com.zhengqing.saa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
@RequestMapping("/ollama")
@Tag(name = "02-ollama")
public class _02_OllamaController {

    @Resource(type = OllamaChatModel.class)
    private ChatModel chatModel;

    /**
     * 简单调用
     * http://localhost:888/ollama/simple/chat?msg=你是谁？
     */
    @Operation(summary = "简单调用")
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatModel.call(msg);
    }

    /**
     * 流式调用
     * http://localhost:888/ollama/stream/chat?msg=你是谁？
     */
    @Operation(summary = "流式调用")
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatModel.stream(msg);
    }

}
