package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.memory.jdbc.MysqlChatMemoryRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat-memory")
@Tag(name = "10-对话记忆")
public class _10_ChatMemoryController {


    private final ChatClient chatClient;

    // 注入 JdbcTemplate, ChatClient
    public _10_ChatMemoryController(JdbcTemplate jdbcTemplate, DashScopeChatModel dashScopeChatModel) {
        // 构造 ChatMemoryRepository 和 ChatMemory
        ChatMemoryRepository chatMemoryRepository = MysqlChatMemoryRepository.mysqlBuilder()
                .jdbcTemplate(jdbcTemplate)
                .build();
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10) // 消息存储条数 -- mysql中最多保留消息记录数
                .build();

        this.chatClient = ChatClient.builder(dashScopeChatModel)
                // 增加聊天记忆能力
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                // 实现 Logger 的 Advisor
                .defaultAdvisors(new SimpleLoggerAdvisor())
                // 设置 ChatClient 中 ChatModel 的 Options 参数
                .defaultOptions(DashScopeChatOptions.builder().withTopP(0.7).build())
                .build();
    }

    /**
     * 使用自定义的 Advisor 增加聊天记忆能力
     * eg:
     * http://127.0.0.1:888/chat-memory/chat/123?msg=你好，我叫郑清，之后的会话中都带上我的名字
     * 你好，郑清！很高兴认识你～接下来的对话我都会记得你的名字的。有什么想聊的或者需要帮忙的，尽管告诉我吧，郑清！ 😊
     * http://127.0.0.1:888/chat-memory/chat/123?msg=我叫什么名字？
     * 你叫郑清，我一直都记得呢，郑清！😊
     * http://127.0.0.1:888/chat-memory/chat/111?msg=我叫什么名字？
     * 抱歉，我无法知道你的名字。你可以告诉我你的名字吗？😊
     */
    @GetMapping("/chat/{id}")
    public Flux<String> advisorChat(HttpServletResponse response, @PathVariable String id, @RequestParam String msg) {
        response.setCharacterEncoding("UTF-8");
        return this.chatClient.prompt(msg)
                .advisors(
                        a -> a
                                .param(ChatMemory.CONVERSATION_ID, id) // 多用户记忆隔离
//                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100)
                ).stream().content();
    }

}
