package com.zhengqing.saa.api.graph.first;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import org.springframework.ai.chat.client.ChatClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p> 问题扩展节点 </p>
 *
 * @author zhengqingya
 * @description
 * @date 2025/10/14 19:56
 */
public class ExpanderNode implements NodeAction {
    private final ChatClient chatClient;

    public ExpanderNode(DashScopeChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        // 1. 从全局状态中获取输入
        String query = state.value("query", "");
        // 2. 构建提示词并调用大模型
        String promptTemplate = "请为以下问题生成3个不同角度的变体问题：{query}";
        String result = chatClient.prompt()
                .user(u -> u.text(promptTemplate).param("query", query))
                .call()
                .content();
        // 3. 处理结果（假设结果以换行分隔）
        List<String> queryVariants = Arrays.asList(result.split("\n"));
        // 4. 将结果放入Map，框架会自动将其合并到全局状态中
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("expander_content", queryVariants);
        return resultMap;
    }
    
}