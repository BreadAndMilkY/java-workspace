package com.zhengqing.saa.api.graph._01_chain;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.graph.GraphRepresentation;
import com.alibaba.cloud.ai.graph.GraphRepresentation.Type;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.OverAllStateFactory;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.action.AsyncNodeAction;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.alibaba.cloud.ai.graph.state.strategy.ReplaceStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

import static com.alibaba.cloud.ai.graph.StateGraph.END;
import static com.alibaba.cloud.ai.graph.StateGraph.START;

/**
 * 简单提示链模式配置类
 * 实现一个三步提示链：获取信息 -> 规划路线 -> 预算估算
 */
@Configuration
public class ChainGraphConfiguration {

    /**
     * 创建一个简单的旅行计划提示链图
     *
     * @param chatModel DashScope聊天模型
     * @return 配置好的状态图
     * @throws GraphStateException 图构建异常
     */
    @Bean
    public StateGraph chainGraph(DashScopeChatModel chatModel) throws GraphStateException {
        // 构建聊天客户端
        ChatClient client = ChatClient.builder(chatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        // 定义状态工厂 - 注册各个步骤使用的状态键
        OverAllStateFactory factory = () -> {
            OverAllState state = new OverAllState();
            state.registerKeyAndStrategy("destination", new ReplaceStrategy());      // 目的地参数
            state.registerKeyAndStrategy("destinationInfo", new ReplaceStrategy());  // 目的地信息
            state.registerKeyAndStrategy("travelRoute", new ReplaceStrategy());      // 旅行路线
            state.registerKeyAndStrategy("budgetEstimate", new ReplaceStrategy());   // 预算估算
            state.registerKeyAndStrategy("finalResult", new ReplaceStrategy());      // 最终结果
            return state;
        };

        // 创建状态图
        StateGraph graph = new StateGraph("SimpleTravelChain", factory);

        // 添加三个处理节点
        graph.addNode("getDestinationInfo", AsyncNodeAction.node_async(new GetDestinationInfoNode(client)));  // 获取目的地信息
        graph.addNode("planTravelRoute", AsyncNodeAction.node_async(new PlanTravelRouteNode(client)));        // 规划旅行路线
        graph.addNode("estimateBudget", AsyncNodeAction.node_async(new EstimateBudgetNode(client)));          // 预算估算

        // 定义执行顺序
        graph.addEdge(START, "getDestinationInfo");           // 开始 -> 获取信息
        graph.addEdge("getDestinationInfo", "planTravelRoute"); // 获取信息 -> 规划路线
        graph.addEdge("planTravelRoute", "estimateBudget");     // 规划路线 -> 预算估算
        graph.addEdge("estimateBudget", END);                   // 预算估算 -> 结束

        // 打印可视化图表
        GraphRepresentation representation = graph.getGraph(Type.PLANTUML, "simple travel chain");
        System.err.println("\n=== Simple Travel Chain UML Flow ===");
        System.err.println(representation.content());
        System.err.println("=====================================\n");

        return graph;
    }

    /**
     * 第一步节点：获取目的地信息
     */
    static class GetDestinationInfoNode implements NodeAction {
        private final ChatClient client;

        public GetDestinationInfoNode(ChatClient client) {
            this.client = client;
        }

        @Override
        public Map<String, Object> apply(OverAllState state) {
            // 从状态中获取目的地参数
            String destination = (String) state.value("destination").orElse("");

            // 调用大模型获取目的地信息
            ChatResponse response = client.prompt()
                    .system("你是一个旅游专家，请用HTML格式介绍指定城市的旅游景点")
                    .user("请介绍" + destination + "的旅游景点，使用HTML格式响应")
                    .call()
                    .chatResponse();

            String info = response.getResult().getOutput().getText();

            // 返回结果，保存到状态中
            return Map.of("destinationInfo", info);
        }
    }

    /**
     * 第二步节点：规划旅行路线
     */
    static class PlanTravelRouteNode implements NodeAction {
        private final ChatClient client;

        public PlanTravelRouteNode(ChatClient client) {
            this.client = client;
        }

        @Override
        public Map<String, Object> apply(OverAllState state) {
            // 从状态中获取目的地信息
            String destinationInfo = (String) state.value("destinationInfo").orElse("");

            // 基于景点信息规划路线
            ChatResponse response = client.prompt()
                    .system("你是一个旅游规划师，请基于景点信息规划合理路线")
                    .user("基于以下景点信息，规划一条合理的旅游路线：" + destinationInfo + " 使用HTML格式响应")
                    .call()
                    .chatResponse();

            String route = response.getResult().getOutput().getText();

            // 返回结果，保存到状态中
            return Map.of("travelRoute", route);
        }
    }

    /**
     * 第三步节点：预算估算
     */
    static class EstimateBudgetNode implements NodeAction {
        private final ChatClient client;

        public EstimateBudgetNode(ChatClient client) {
            this.client = client;
        }

        @Override
        public Map<String, Object> apply(OverAllState state) {
            // 从状态中获取旅行路线
            String travelRoute = (String) state.value("travelRoute").orElse("");

            // 根据路线估算预算
            ChatResponse response = client.prompt()
                    .system("你是一个旅游预算师，请根据路线估算大致费用")
                    .user("根据这条路线，估算大致费用：" + travelRoute + " 使用HTML格式响应")
                    .call()
                    .chatResponse();

            String budget = response.getResult().getOutput().getText();

            // 组装最终结果
            String destination = (String) state.value("destination").orElse("");
            String destinationInfo = (String) state.value("destinationInfo").orElse("");
            String finalResult = String.format(
                    "<h1>%s旅行计划</h1>" +
                            "<h2>景点信息</h2>%s" +
                            "<h2>推荐路线</h2>%s" +
                            "<h2>预算估算</h2>%s",
                    destination, destinationInfo, travelRoute, budget
            );

            // 返回最终结果
            return Map.of("finalResult", finalResult);
        }
    }
}