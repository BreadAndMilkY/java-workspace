package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.advisor.RetrievalRerankAdvisor;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.alibaba.cloud.ai.dashscope.rerank.DashScopeRerankModel;
import com.alibaba.cloud.ai.model.RerankModel;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/rag/rerank")
@Tag(name = "24-RetrievalRerankAdvisor重排序")
public class _24_RerankController {

    private RerankModel rerankModel;
    private ChatClient chatClient;
    private VectorStore vectorStore;

    public _24_RerankController(DashScopeChatModel dashScopeChatModel,
                                DashScopeEmbeddingModel dashScopeEmbeddingModel,
                                DashScopeRerankModel dashScopeRerankModel) {
        chatClient = ChatClient.builder(dashScopeChatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
        vectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
        rerankModel = dashScopeRerankModel;
    }

    /**
     * http://localhost:888/rag/rerank/chat?msg=iphone20价格
     */
    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam String msg) {
        vectorStore.add(Lists.newArrayList(
                Document.builder().text("iPhone18 基础价格: 5999元, 税率: 13%, 运费: 0元").build(),
                Document.builder().text("iPhone20 基础价格: 12999元, 税率: 13%, 运费: 50元").build(),
                Document.builder().text("iPhone10 基础价格: 1299元, 税率: 13%, 运费: 0元").build()
        ));

        // 重排序
        RetrievalRerankAdvisor rerankAdvisor = new RetrievalRerankAdvisor(vectorStore, rerankModel,
                SearchRequest.builder().topK(10).build());

        return chatClient.prompt().user(msg)
                .advisors(rerankAdvisor)
                .stream().content();
    }

}
