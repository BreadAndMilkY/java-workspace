package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@Slf4j
@SpringBootTest
public class _01_HelloWorld {

    @Value("${spring.ai.dashscope.api-key}")
    private String apiKey;

    @Resource(type = DashScopeChatModel.class)
    private ChatModel chatModel;

    @BeforeEach
    void init() {
        LoggingSystem.get(LoggingSystem.class.getClassLoader()).setLogLevel("ROOT", LogLevel.ERROR);
        LoggingSystem.get(LoggingSystem.class.getClassLoader()).setLogLevel("com.zhengqing", LogLevel.ERROR);
    }

    @Test
    public void testApiKey() {
        System.out.println(apiKey);
    }

    @Test // 简单调用
    public void test_SimpleChat() throws Exception {
        String answer = chatModel.call("你是谁？");
        System.out.println(answer);
    }

    @Test // 流式调用
    public void test_streamChat() throws Exception {
        Flux<String> answer = chatModel.stream("你是谁？");
        System.out.println(answer);
    }

}