//package com.zhengqing.saa.config;
//
//import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
//import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
//import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
//import org.springframework.ai.chat.client.ChatClient;
//import org.springframework.ai.chat.model.ChatModel;
//import org.springframework.ai.chat.prompt.ChatOptions;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class MutliModelConfig {
//
//    @Value("${spring.ai.dashscope.api-key}")
//    private String apiKey;
//
//    private final String DEEPSEEK_MODEL = "deepseek-v3";
//    private final String QWEN_MODEL = "qwen-max";
//
//    @Bean("deepseekChatModel")
//    public ChatModel deepseekChatModel() {
//        return DashScopeChatModel.builder()
//                .dashScopeApi(DashScopeApi.builder().apiKey(apiKey).build())
//                .defaultOptions(DashScopeChatOptions.builder().withModel(DEEPSEEK_MODEL).build())
//                .build();
//    }
//
//
//    @Bean("qwenChatModel")
//    public ChatModel qwenChatModel() {
//        return DashScopeChatModel.builder()
//                .dashScopeApi(DashScopeApi.builder().apiKey(apiKey).build())
//                .defaultOptions(DashScopeChatOptions.builder().withModel(QWEN_MODEL).build())
//                .build();
//    }
//
//    @Bean("deepseekChatClient")
//    public ChatClient deepseekChatClient(@Qualifier("deepseekChatModel") ChatModel chatModel) {
//        return ChatClient.builder(chatModel)
//                .defaultOptions(ChatOptions.builder().model(DEEPSEEK_MODEL).build())
//                .build();
//    }
//
//    @Bean("qwenChatClient")
//    public ChatClient qwenChatClient(@Qualifier("qwenChatModel") ChatModel chatModel) {
//        return ChatClient.builder(chatModel)
//                .defaultOptions(ChatOptions.builder().model(QWEN_MODEL).build())
//                .build();
//    }
//
//}
