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
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/agent/coordinator")
@Tag(name = "AI Agent: 协调者-工作者模式 (Coordinator-Worker)")
public class _04_CoordinatorAgentController {

    private final ChatClient coordinator;
    private final Map<String, ChatClient> workers;

    public _04_CoordinatorAgentController(DashScopeChatModel chatModel) {
        this.coordinator = ChatClient.builder(chatModel)
                .defaultSystem("你是一个项目协调者，负责分解任务并分配给不同的工作者")
                .build();

        this.workers = new HashMap<>();
        workers.put("frontend", ChatClient.builder(chatModel).defaultSystem("你是一个专业前端开发者，负责用户界面和交互实现").build());
        workers.put("backend", ChatClient.builder(chatModel).defaultSystem("你是一个专业后端开发者，负责服务器逻辑和数据处理").build());
        workers.put("tester", ChatClient.builder(chatModel).defaultSystem("你是一个专业测试工程师，负责质量保证和测试用例设计").build());
    }

    /**
     * http://localhost:888/agent/coordinator/project-task?projectRequirement=开发一个用户登录系统
     */
    @GetMapping("/project-task")
    public Flux<String> handleProjectTask(@RequestParam(defaultValue = "开发一个用户登录系统") String projectRequirement) {
        // 协调者分解任务
        String taskBreakdown = coordinator.prompt()
                .user(u -> u.text("将以下需求分解为前端、后端、测试任务：{projectRequirement}")
                        .param("projectRequirement", projectRequirement))
                .call()
                .content();

        // 分配给不同工作者执行
        Map<String, String> results = new ConcurrentHashMap<>();

        workers.entrySet().parallelStream().forEach(entry -> {
            String workerResult = entry.getValue().prompt()
                    .user(u -> u.text("请完成分配给你的任务：{taskBreakdown}")
                            .param("taskBreakdown", taskBreakdown))
                    .call()
                    .content();
            results.put(entry.getKey(), workerResult);
        });

        // 协调者整合结果
        return coordinator.prompt()
                .user(u -> u.text("整合以下各部分工作成果：{results} html格式响应")
                        .param("results", results.toString()))
                .stream()
                .content();
    }

}