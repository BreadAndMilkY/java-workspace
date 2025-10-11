package com.zhengqing.saa.api.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/agent/parallel")
@Tag(name = "AI Agent: 并行化模式 (Parallel)")
public class _03_ParallelAgentController {

    private final ChatClient chatClient;

    public _03_ParallelAgentController(DashScopeChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * http://localhost:888/agent/parallel/analyze-document?document=Java学习需要掌握基础语法和实践项目
     */
    @GetMapping("/analyze-document")
    public List<String> analyzeDocument(@RequestParam String document) {
        // 并行执行多个分析任务
        CompletableFuture<String> sentimentAnalysis = CompletableFuture.supplyAsync(() ->
                chatClient.prompt()
                        .user(u -> u.text("分析以下文档的情感倾向：{document}").param("document", document))
                        .call()
                        .content()
        );

        CompletableFuture<String> keyPoints = CompletableFuture.supplyAsync(() ->
                chatClient.prompt()
                        .user(u -> u.text("提取以下文档的关键要点：{document}").param("document", document))
                        .call()
                        .content()
        );

        CompletableFuture<String> summary = CompletableFuture.supplyAsync(() ->
                chatClient.prompt()
                        .user(u -> u.text("为以下文档生成摘要：{document}").param("document", document))
                        .call()
                        .content()
        );

        // 等待所有任务完成并返回结果
        return CompletableFuture.allOf(sentimentAnalysis, keyPoints, summary)
                .thenApply(v -> Arrays.asList(
                        sentimentAnalysis.join(),
                        keyPoints.join(),
                        summary.join()
                ))
                .join();
    }
}
