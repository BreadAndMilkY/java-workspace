package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/prompt")
@Tag(name = "06-提示词")
public class _06_PromptController {

    private ChatClient chatClient;
    private ChatModel chatModel;

    @Value("classpath:/prompt-template/tell-joke.txt")
    private org.springframework.core.io.Resource promptTemplateRes;

    public _06_PromptController(DashScopeChatModel dashScopeChatModel) {
        chatClient = ChatClient.builder(dashScopeChatModel).build();
        chatModel = dashScopeChatModel;
    }

    /**
     * 一、提示词 浅尝 ------------------------------------------
     * call()：返回完整响应结果（等待AI生成完整回答后一次性返回）
     * stream()：返回流式响应结果（采用流式传输，边生成边返回，提供实时反馈体验）
     */

    private static final String DEFAULT_PROMPT = """
            你是一位资深Java架构师，专注于企业级Java后端开发。
            请严格按照以下规则回答：
            1. 只回答Java及相关技术栈的问题；
            2. 提供准确、专业的技术解答；
            3. 对于非Java后端相关问题，请礼貌说明超出了专业范围。
            """;

    /**
     * http://localhost:888/prompt/chat-client?msg=今天吃什么？
     */
    @GetMapping("/chat-client")
    public String chatClient(@RequestParam String msg) {
        return chatClient.prompt()
                .system(DEFAULT_PROMPT)
                .user(msg)
                .call().content();
    }

    /**
     * http://localhost:888/prompt/chat-model?msg=java学习路线？
     */
    @GetMapping("/chat-model")
    public String chatModel(@RequestParam String msg) {
        SystemMessage systemMessage = new SystemMessage(DEFAULT_PROMPT);
        UserMessage userMessage = new UserMessage(msg);
        Prompt prompt = new Prompt(systemMessage, userMessage);
        return chatModel.call(prompt).getResult().getOutput().getText();
    }

    // *****************************************************************

    /**
     * http://localhost:888/prompt/stream/chat-client?msg=今天吃什么？
     */
    @GetMapping("/stream/chat-client")
    public Flux<String> streamChatClient(@RequestParam String msg) {
        return chatClient.prompt()
                .system(DEFAULT_PROMPT)
                .user(msg)
                .stream().content();
    }

    /**
     * http://localhost:888/prompt/stream/chat-model?msg=java学习路线？
     */
    @GetMapping("/stream/chat-model")
    public Flux<String> streamChatModel(@RequestParam String msg) {
        SystemMessage systemMessage = new SystemMessage(DEFAULT_PROMPT);
        UserMessage userMessage = new UserMessage(msg);
        Prompt prompt = new Prompt(systemMessage, userMessage);
        return chatModel.stream(prompt).map(e -> e.getResult().getOutput().getText());
    }

    // 二、提示词模板 ------------------------------------------

    /**
     * http://localhost:888/prompt/prompt-template?topic=无聊
     */
    @GetMapping("/prompt-template")
    public Flux<String> promptTemplate(@RequestParam String topic) {
        // 创建提示词模板
        PromptTemplate promptTemplate = new PromptTemplate("给我讲一个有关于{topic}的笑话");
        // 创建 Prompt 实例
        Prompt prompt = promptTemplate.create(Map.of("topic", topic));
        // 流式返回结果
        return chatModel.stream(prompt).map(chatResponse -> chatResponse.getResult().getOutput().getText());
    }

    /**
     * 获取模板文件内容
     * http://localhost:888/prompt/prompt-template2?topic=无聊
     */
    @GetMapping("/prompt-template2")
    public Flux<String> promptTemplate2(@RequestParam String topic) {
        // 创建提示词模板
        PromptTemplate promptTemplate = new PromptTemplate(promptTemplateRes);
        // 创建 Prompt 实例
        Prompt prompt = promptTemplate.create(Map.of("topic", topic));
        // 流式返回结果
        return chatModel.stream(prompt).map(chatResponse -> chatResponse.getResult().getOutput().getText());
    }


    /**
     * 角色设定 & 边界划分
     * http://localhost:888/prompt/prompt-template3?sysTopic=Java开发&userTopic=Spring框架
     * http://localhost:888/prompt/prompt-template3?sysTopic=Java开发&userTopic=机器学习
     */
    @GetMapping("/prompt-template3")
    public Flux<String> promptTemplate3(@RequestParam String sysTopic, @RequestParam String userTopic) {
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate("""
                你是一位专业的{sysTopic}领域专家
                请遵循以下规则进行回答：
                1. 严格围绕{sysTopic}相关话题进行解答
                2. 使用准确、专业的术语
                3. 回答简洁明了，逻辑清晰
                4. 如遇到非{sysTopic}相关问题，请礼貌说明不在专业范围内
                """);
        Message systemMessage = systemPromptTemplate.createMessage(Map.of("sysTopic", sysTopic));

        PromptTemplate userPromptTemplate = new PromptTemplate("""
                请详细科普以下{userTopic}相关内容：
                1. 基本概念定义
                2. 核心原理或特点
                3. 实际应用场景
                4. 相关注意事项
                """);
        Message userMessage = userPromptTemplate.createMessage(Map.of("userTopic", userTopic));

        Prompt prompt = new Prompt(List.of(systemMessage, userMessage));
        return chatClient.prompt(prompt).stream().content();
    }

}
