package com.zhengqing.saa.api.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/agent/routing")
@Tag(name = "AI Agent: 路由模式 (Routing)")
public class _02_RoutingAgentController {

    private final ChatClient chatClient;
    private final Map<String, ChatClient> specializedAgents;

    public _02_RoutingAgentController(DashScopeChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
        this.specializedAgents = new HashMap<>();
        // 初始化不同类型的专业Agent
        specializedAgents.put("technical", ChatClient.builder(chatModel).defaultSystem("你是一个技术专家，专门回答编程和技术问题").build());
        specializedAgents.put("billing", ChatClient.builder(chatModel).defaultSystem("你是一个财务专家，专门处理账单和费用相关问题").build());
        specializedAgents.put("complaint", ChatClient.builder(chatModel).defaultSystem("你是一个客服专家，专门处理用户投诉和建议").build());
    }


    /**
     * http://localhost:888/agent/routing/route-query?userQuery=java如何学习,html格式响应
     */
    @GetMapping("/route-query")
    public Flux<String> routeQuery(@RequestParam String userQuery) {
        // 判断问题类型
        String queryType = chatClient.prompt()
                .user(u -> u.text("""
                                请判断以下问题属于哪一类：
                                1. technical(技术问题)
                                2. billing(账单咨询)
                                3. complaint(投诉)。
                                
                                要求：只响应分类英文结果。
                                
                                问题：{userQuery}
                                """)
                        .param("userQuery", userQuery))
                .call()
                .content();

        // 路由到相应Agent处理
        ChatClient targetAgent = specializedAgents.getOrDefault(queryType, chatClient);
        return targetAgent.prompt()
                .user(userQuery)
                .stream()
                .content();
    }
    
}
