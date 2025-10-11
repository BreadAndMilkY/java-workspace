package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/observability")
@Tag(name = "26-可观测")
public class _26_ObservabilityController {

    private ChatModel chatModel;

    public _26_ObservabilityController(DashScopeChatModel dashScopeChatModel) {
        chatModel = dashScopeChatModel;
    }

    /**
     * http://localhost:888/observability/test?prompt=你是谁？
     */
    @GetMapping("/test")
    public Flux<String> test(@RequestParam(defaultValue = "你好") String prompt) {
        return chatModel.stream(prompt);
    }

}
