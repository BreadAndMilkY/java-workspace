package com.zhengqing.saa.mcp.server;

import com.zhengqing.saa.mcp.server.tool.TimeToolService;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpServerSeeApplication {

    public static void main(String[] args) {
        SpringApplication.run(McpServerSeeApplication.class, args);
    }

    @Bean
    public ToolCallbackProvider timeTools(TimeToolService timeToolService) {
        // 注册 MCP 工具
        return MethodToolCallbackProvider.builder()
                .toolObjects(timeToolService)
                .build();
    }

}