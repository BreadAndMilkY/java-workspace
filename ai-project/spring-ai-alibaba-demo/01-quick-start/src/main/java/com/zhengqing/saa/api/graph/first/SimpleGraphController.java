package com.zhengqing.saa.api.graph.first;

import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/graph")
@Tag(name = "Graph")
public class SimpleGraphController {

    private final CompiledGraph compiledGraph;

    // 注入定义好的StateGraph，并编译成CompiledGraph
    public SimpleGraphController(@Qualifier("simpleGraph") StateGraph stateGraph) throws GraphStateException {
        this.compiledGraph = stateGraph.compile();
    }

    /**
     * http://localhost:888/graph/expand?query=你是谁？
     */
    @GetMapping("/expand")
    public Map<String, Object> expandQuery(@RequestParam String query) throws GraphRunnerException {
        // 设置初始状态，这里传入的"query"会作为工作流的输入
        Map<String, Object> initialState = Map.of("query", query);
        // 执行工作流
        Optional<OverAllState> result = compiledGraph.invoke(initialState);
        // 从最终状态中获取结果
        return result.map(OverAllState::data).orElse(Map.of());
    }
    
}