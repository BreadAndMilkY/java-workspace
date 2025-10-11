package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.google.common.collect.Lists;
import com.zhengqing.saa.advisor.RewritePromptAdvisor;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SafeGuardAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/advisor")
@Tag(name = "09-Advisor对话拦截--自定义拦截器")
public class _09_AdvisorCustomController {

    private ChatClient chatClient;

    public _09_AdvisorCustomController(DashScopeChatModel dashScopeChatModel) {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(
                        new SimpleLoggerAdvisor(),
                        new SafeGuardAdvisor(
                                Lists.newArrayList("敏感词", "WC"),
                                "由于包含敏感内容，我无法对此进行回复。我们可以重新表述或讨论其他话题吗？",
                                1),
                        new RewritePromptAdvisor()
                )
                .build();
    }

    /**
     * http://localhost:888/advisor/rewrite-prompt?msg=你好
     * http://localhost:888/advisor/rewrite-prompt?msg=WC
     */
    @GetMapping("/rewrite-prompt")
    public Flux<String> rewritePrompt(@RequestParam String msg) {
        return chatClient.prompt()
                .user(msg)
                .stream().content();
    }

}
