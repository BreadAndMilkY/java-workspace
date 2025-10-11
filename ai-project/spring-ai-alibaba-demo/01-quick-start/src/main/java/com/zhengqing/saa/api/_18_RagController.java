package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rag")
@Tag(name = "18-RAG")
public class _18_RagController {

    private ChatClient chatClient;
    private EmbeddingModel embeddingModel;
    private VectorStore vectorStore;

    public _18_RagController(DashScopeChatModel dashScopeChatModel,
                             DashScopeEmbeddingModel dashScopeEmbeddingModel) {
        chatClient = ChatClient.builder(dashScopeChatModel).build();
        embeddingModel = dashScopeEmbeddingModel;
        vectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
    }

    /**
     * 嵌入模型 (Embedding Model)
     * http://localhost:888/rag/embedding?msg=你好
     */
    @GetMapping("/embedding")
    public Object embedding(@RequestParam String msg) {
        EmbeddingResponse embeddingResponse = this.embeddingModel.embedForResponse(List.of(msg));
        return Map.of("embedding", embeddingResponse);
    }

    /**
     * 向量数据库
     * http://localhost:888/rag/vectorStore?topK=3&threshold=0.1&msg=有哪些开源的向量数据库？
     * http://localhost:888/rag/vectorStore?topK=3&threshold=0.1&msg=LangChain
     */
    @GetMapping("/vectorStore")
    public Object vectorStore(@RequestParam String msg, @RequestParam int topK, @RequestParam double threshold) {
        // 底层暂未实现具体逻辑...
//        vectorStore.delete(new Filter.Expression(Filter.ExpressionType.NOT, new Filter.Key("xx")));
        // 1、存储向量 -- 添加文档到向量数据库
        vectorStore.add(Lists.newArrayList(
                Document.builder().text("LangChain是一个用于开发由语言模型驱动的应用程序的框架。").build(),
                Document.builder().text("Milvus是一款开源向量数据库，专为大规模向量相似性搜索而设计。").build()
        ));

        // 2、相似性搜索 -- 根据查询内容搜索相似文档
//        return vectorStore.similaritySearch("有哪些开源的向量数据库？"); // 执行搜索并返回结果
        return vectorStore.similaritySearch(SearchRequest.builder()
                .query(msg) // 用户输入的查询内容
                .topK(topK) // 返回最相似的前n个结果
                .similarityThreshold(threshold) // 设置相似度阈值，仅返回相似度高于xx的结果
                .build());
    }

    /**
     * 接入 ChatClient -- 方式1
     * http://localhost:888/rag/chat?msg=iPhone18的总费用
     * http://localhost:888/rag/chat?msg=iPhone20的总费用
     */
    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam String msg) {
        // 1、存储商品费用信息到向量数据库
        vectorStore.add(Lists.newArrayList(
                Document.builder().text("iPhone18 基础价格: 5999元, 税率: 13%, 运费: 0元").build(),
                Document.builder().text("MacBook Pro8 基础价格: 12999元, 税率: 13%, 运费: 50元").build(),
                Document.builder().text("AirPods8 基础价格: 1299元, 税率: 13%, 运费: 0元").build()
        ));

        // 2、从向量数据库中检索相关商品信息
        List<Document> relevantDocs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(msg)
                        .topK(3)
                        .similarityThreshold(0.5)
                        .build()
        );

        // 3、将检索到的商品信息作为上下文传递给 ChatClient
        String context = relevantDocs.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        // 4、使用 ChatClient 生成基于检索信息的回答
        return chatClient.prompt()
                .user(msg)
                .system("请基于以下商品费用信息计算总费用，需要考虑基础价格、税率和运费：\n" + context + "\n请给出详细的计算过程和最终结果。html格式响应")
                .stream()
                .content();
    }

    /**
     * 接入 ChatClient -- 方式2
     * http://localhost:888/rag/chat2?msg=iPhone18的总费用
     * http://localhost:888/rag/chat2?msg=iPhone20的总费用
     */
    @GetMapping("/chat2")
    public Flux<String> chat2(@RequestParam String msg) {
        // 1、存储商品费用信息到向量数据库
        vectorStore.add(Lists.newArrayList(
                Document.builder().text("iPhone18 基础价格: 5999元, 税率: 13%, 运费: 0元").build(),
                Document.builder().text("MacBook Pro8 基础价格: 12999元, 税率: 13%, 运费: 50元").build(),
                Document.builder().text("AirPods8 基础价格: 1299元, 税率: 13%, 运费: 0元").build()
        ));

        /**
         * 2、定义自定义提示模板（PromptTemplate） -- 用于指导AI模型如何基于检索到的背景信息来回答用户问题
         * 占位符自动注入上下文
         * {question_answer_context}：会被QuestionAnswerAdvisor自动替换为从向量数据库检索到的相关文档内容
         * {query}：会被用户的实际问题内容替换
         */
        PromptTemplate customTemplate = PromptTemplate.builder()
                .template("""
                        请严格根据以下背景信息回答问题，给出详细的计算过程和最终结果，html格式响应。如果信息中没有答案，请直接说"我不知道"。
                        背景信息：
                        {question_answer_context}
                        用户问题：{query}
                        """)
                .build();

        // 3、使用 ChatClient 配合 QuestionAnswerAdvisor 自动生成回答
        return chatClient.prompt()
                .user(msg)
                .advisors(
                        // 日志调试
                        SimpleLoggerAdvisor.builder().build(),
                        // 接入RAG能力 - 使用 QuestionAnswerAdvisor 自动处理向量检索
                        QuestionAnswerAdvisor.builder(vectorStore)
                                .promptTemplate(customTemplate)
                                .searchRequest(
                                        SearchRequest.builder()
                                                .topK(3)
                                                .similarityThreshold(0.65)
                                                .build()
                                ).build()
                )
                .stream()
                .content();
    }

}
