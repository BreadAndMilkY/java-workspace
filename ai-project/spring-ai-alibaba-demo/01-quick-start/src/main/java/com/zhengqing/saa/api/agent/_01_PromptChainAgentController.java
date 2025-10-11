package com.zhengqing.saa.api.agent;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/agent/chain")
@Tag(name = "AI Agent: 提示链模式 (Prompt Chain)")
public class _01_PromptChainAgentController {

    private final ChatClient chatClient;

    public _01_PromptChainAgentController(DashScopeChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }


    /**
     * http://localhost:888/agent/chain/travel-plan?destination=成都3日游
     */
    @GetMapping("/travel-plan")
    public String travelPlan(@RequestParam String destination) {
        // 第一步：获取目的地信息
        String destinationInfo = chatClient.prompt()
                .user(u -> u.text("请介绍{destination}的旅游景点  html格式响应").param("destination", destination))
                .call()
                .content();

        // 第二步：基于景点信息规划路线
        String travelRoute = chatClient.prompt()
                .user(u -> u.text("基于以下景点信息，规划一条合理的旅游路线：{destinationInfo}  html格式响应").param("destinationInfo", destinationInfo))
                .call()
                .content();

        // 第三步：预算估算
        String budgetEstimate = chatClient.prompt()
                .user(u -> u.text("根据这条路线，估算大致费用：{travelRoute}  html格式响应").param("travelRoute", travelRoute))
                .call()
                .content();

        return String.format("目的地：%s\n\n景点信息：\n%s\n\n推荐路线：\n%s\n\n预算估算：\n%s",
                destination, destinationInfo, travelRoute, budgetEstimate);
    }
}
