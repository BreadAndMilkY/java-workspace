package com.zhengqing.saa.api.graph.first;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.KeyStrategy;
import com.alibaba.cloud.ai.graph.KeyStrategyFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * <p> 配置StateGraph，实现一个简单的工作流，用于问题扩展 </p>
 *
 * @author zhengqingya
 * @description
 * @date 2025/10/14 19:56
 */
@Configuration
public class SimpleGraphConfiguration {

    @SneakyThrows
    @Bean
    public StateGraph simpleGraph(DashScopeChatModel chatModel) {
        // 全局变量的替换策略（ReplaceStrategy:替换，AppendStrategy:追加）
        KeyStrategyFactory keyStrategyFactory = () -> {
            HashMap<String, KeyStrategy> strategies = new HashMap<>();
            strategies.put("query", new ReplaceStrategy());
            strategies.put("expander_content", new ReplaceStrategy());
            return strategies;
        };
        // 构建状态图
        StateGraph stateGraph = new StateGraph("问题扩展工作流", keyStrategyFactory)
                // 添加节点，并命名为 "expander"
                .addNode("expander", AsyncNodeAction.node_async(new ExpanderNode(chatModel)))
                // 添加边：从开始节点指向 "expander" 节点
                .addEdge(StateGraph.START, "expander")
                // 添加边：从 "expander" 节点指向结束节点
                .addEdge("expander", StateGraph.END);

        // 将图打印出来，可以使用 PlantUML 插件查看
        GraphRepresentation representation = stateGraph.getGraph(GraphRepresentation.Type.PLANTUML, "expander flow");
        System.err.println("\n=== expander UML Flow ===");
        System.err.println(representation.content());
        System.err.println("==================================\n");
        return stateGraph;
    }
    
}