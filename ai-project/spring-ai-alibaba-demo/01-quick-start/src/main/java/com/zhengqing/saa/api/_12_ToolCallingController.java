package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.zhengqing.saa.tools.TimeTools;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/tool-calling")
@Tag(name = "12-工具调用")
public class _12_ToolCallingController {

    private ChatClient chatClient;
    @Autowired
    private TimeTools timeTools;

    public _12_ToolCallingController(DashScopeChatModel dashScopeChatModel) {
        chatClient = ChatClient.builder(dashScopeChatModel)
                // 全局注册工具
//                .defaultTools(timeTools)
//                .defaultToolNames("getWeather")
                .build();
    }

    /**
     * 法1：方法工具
     * http://localhost:888/tool-calling/time?msg=现在时间?
     * 现在的时间是2025年10月3日02点35分52秒。
     * <p>
     * http://localhost:888/tool-calling/time?msg=当前年月日？
     * 当前日期是2025年10月3日。
     */
    @GetMapping("/time")
    public Flux<String> time(@RequestParam String msg) {
        return chatClient.prompt().user(msg)
                .tools(timeTools)
                .stream().content();
    }

    /**
     * 法2：函数工具
     * http://localhost:888/tool-calling/weather?msg=成都天气?
     * 现在的时间是2025年10月3日02点35分52秒。
     */
    @GetMapping("/weather")
    public Flux<String> weather(@RequestParam String msg) {
        return chatClient.prompt().user(msg)
                .toolNames(new String[]{"getWeather"})
                .stream().content();
    }

}
