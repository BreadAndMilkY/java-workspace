package com.zhengqing.saa.api.graph._01_chain;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 简单提示链控制器
 * 提供同步和异步两种调用方式
 */
@RestController
@RequestMapping("/graph")
public class ChainController {

    private final CompiledGraph compiledGraph;

    /**
     * 构造函数，注入并编译状态图
     *
     * @param chainGraph 状态图定义
     * @throws Exception 编译异常
     */
    @Autowired
    public ChainController(@Qualifier("chainGraph") StateGraph chainGraph) throws Exception {
        SaverConfig saverConfig = SaverConfig.builder().build();
        // 编译图
        this.compiledGraph = chainGraph.compile(CompileConfig.builder()
                .saverConfig(saverConfig)
                .build());
    }

    /**
     * 同步执行提示链
     * http://localhost:888/graph/chain?destination=成都
     *
     * @param destination 目的地参数
     * @return 最终结果
     */
    @SneakyThrows
    @GetMapping("chain")
    public Object executeChain(@RequestParam String destination) {
        // 调用图执行，传入初始参数
        return compiledGraph.invoke(Map.of("destination", destination)).get().data().get("finalResult");
    }

    /**
     * 流式执行提示链（异步）
     * http://localhost:888/graph/chain/stream?destination=成都
     *
     * @param destination 目的地参数
     * @return 流式结果 - 只返回最终结果数据
     */
    @GetMapping(path = "chain/stream", produces = "text/event-stream")
    public Flux<Object> executeChainStream(@RequestParam String destination) {
        // 配置流式模式为快照模式
        RunnableConfig config = RunnableConfig.builder()
                .streamMode(CompiledGraph.StreamMode.SNAPSHOTS)
                .build();

        // 创建响应式流，只返回最终结果
        return Flux.create(sink -> {
            try {
                // 执行流式调用
                compiledGraph.stream(Map.of("destination", destination), config)
                        .forEachAsync(node -> {
                            // 只发送最终结果数据
                            Map<String, Object> stateData = node.state().data();
                            if (stateData.containsKey("finalResult")) {
                                sink.next(stateData.get("finalResult"));
                            }
                        })
                        .whenComplete((v, e) -> {
                            // 处理完成或错误情况
                            if (e != null) {
                                sink.error(e);
                            } else {
                                sink.complete();
                            }
                        });
            } catch (GraphRunnerException e) {
                sink.error(e);
            }
        });
    }

}