package com.zhengqing.saa.config;

import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestClient;

import java.time.Duration;

//@Configuration
public class AiConfig {

//    @Bean
//    public VectorStore vectorStore(DashScopeEmbeddingModel dashScopeEmbeddingModel) {
//        return SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
//    }

    /**
     * 自定义RestClient.Builder -- 解决HTTP请求超时问题
     */
    @Bean
    @Scope("prototype")
    RestClient.Builder restClientBuilder(RestClientBuilderConfigurer restClientBuilderConfigurer) {
        return restClientBuilderConfigurer.configure(RestClient.builder()
                .requestFactory(ClientHttpRequestFactories.get(
                        ClientHttpRequestFactorySettings.DEFAULTS
                                .withReadTimeout(Duration.ofSeconds(60))
                                .withConnectTimeout(Duration.ofSeconds(60))
                ))
        );
    }

}
