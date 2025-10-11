package com.zhengqing.saa.api.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent/evaluation")
@Tag(name = "AI Agent: 评估者-优化者模式 (Evaluator-Optimizer)")
public class _05_EvaluationAgentController {

    private final ChatClient optimizer;
    private final ChatClient evaluator;

    public _05_EvaluationAgentController(DashScopeChatModel chatModel) {
        this.optimizer = ChatClient.builder(chatModel)
                .defaultSystem("你是一个文案优化专家，擅长提升文案的质量、流畅度和吸引力")
                .build();
        this.evaluator = ChatClient.builder(chatModel)
                .defaultSystem("你是一个文案质量评估专家，能够客观评价文案的质量并给出具体分数")
                .build();
    }

    /**
     * http://localhost:888/agent/evaluation/optimize-content?initialContent=欢迎购买我们的产品
     */
    @GetMapping("/optimize-content")
    public String optimizeContent(@RequestParam(defaultValue = "欢迎购买我们的产品") String initialContent) {
        String currentContent = initialContent;
        int maxIterations = 3;
        double qualityThreshold = 0.8;

        StringBuilder result = new StringBuilder();
        result.append("初始文案: ").append(initialContent).append("\n\n");

        for (int i = 0; i < maxIterations; i++) {
            // 评估当前内容质量
            double qualityScore = evaluateQuality(currentContent);
            result.append("第").append(i + 1).append("轮评估分数: ").append(qualityScore).append("\n");

            // 如果质量达标则停止优化
            if (qualityScore >= qualityThreshold) {
                result.append("优化完成，质量达标！\n");
                break;
            }

            // 否则进行优化
            currentContent = optimizeText(currentContent);
            result.append("第").append(i + 1).append("轮优化结果: ").append(currentContent).append("\n\n");
        }

        result.append("最终文案: ").append(currentContent);
        return result.toString();
    }

    private double evaluateQuality(String content) {
        String evaluation = evaluator.prompt()
                .user(u -> u.text("评估以下文案的质量（0-1分），只需要返回分数：{content}  html格式响应")
                        .param("content", content))
                .call()
                .content();
        return parseQualityScore(evaluation);
    }

    private String optimizeText(String content) {
        return optimizer.prompt()
                .user(u -> u.text("优化以下文案以提高质量，使其更加流畅和有吸引力：{content}  html格式响应")
                        .param("content", content))
                .call()
                .content();
    }

    private double parseQualityScore(String evaluation) {
        // 使用正则表达式提取分数
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\d+\\.\\d+|\\d+");
        java.util.regex.Matcher matcher = pattern.matcher(evaluation);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0.7; // 默认分数
    }
}
