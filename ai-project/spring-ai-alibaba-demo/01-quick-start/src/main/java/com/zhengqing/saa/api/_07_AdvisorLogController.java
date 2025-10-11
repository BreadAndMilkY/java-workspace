package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/advisor")
@Tag(name = "07-Advisor对话拦截--日志记录")
public class _07_AdvisorLogController {

    private ChatClient chatClient;

    public _07_AdvisorLogController(DashScopeChatModel dashScopeChatModel) {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * http://localhost:888/advisor/log?msg=你好
     */
    @GetMapping("/log")
    public Flux<String> log(@RequestParam String msg) {
        return chatClient.prompt()
                .user(msg)
                .stream().content();
    }

}
