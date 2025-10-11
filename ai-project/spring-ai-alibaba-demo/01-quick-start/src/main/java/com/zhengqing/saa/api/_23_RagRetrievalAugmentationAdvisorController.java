package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.generation.augmentation.ContextualQueryAugmenter;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rag/retrieval-augmentation")
@Tag(name = "23-RAG-检索增强生成")
public class _23_RagRetrievalAugmentationAdvisorController {

    @Value("classpath:rag/iphone.md")
    private Resource MdRes;

    private ChatModel chatModel;
    private ChatClient chatClient;
    private VectorStore vectorStore;

    public _23_RagRetrievalAugmentationAdvisorController(DashScopeChatModel dashScopeChatModel,
                                                         DashScopeEmbeddingModel dashScopeEmbeddingModel) {
        chatModel = dashScopeChatModel;
        chatClient = ChatClient.builder(dashScopeChatModel).defaultAdvisors(new SimpleLoggerAdvisor()).build();
        vectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
    }

    /**
     * RetrievalAugmentationAdvisor 检索增强生成
     * http://localhost:888/rag/retrieval-augmentation/chat?msg=下雨天，我不想出门，你能不能帮我了解下手机店里的iphone18价格？
     * http://localhost:888/rag/retrieval-augmentation/chat?msg=iphone20价格
     */
    @GetMapping("/chat")
    public Flux<String> chat(@RequestParam String msg) {
        // 1. 创建Markdown文档读取器
        MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(MdRes, MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("filename", MdRes.getFilename())
                .build());
        // 2. 读取文档内容
        List<Document> documents = markdownReader.read();
        vectorStore.add(documents);


        // 3. 检索增强生成
        Advisor advisor = RetrievalAugmentationAdvisor.builder()
                // documentRetriever：文档检索器，用于从向量数据库检索文档。
                .documentRetriever(VectorStoreDocumentRetriever.builder()
                        .vectorStore(vectorStore)
                        .similarityThreshold(0.5)
                        .topK(3)
                        .build())
                // queryAugmenter：查询增强器，用于处理用户查询并生成增强后的查询。（会自动将用户查询与检索到的文档内容结合，形成增强的上下文供大模型使用）
                .queryAugmenter(ContextualQueryAugmenter.builder()
                        /**
                         * 当未检索到相关文档时的处理策略
                         * true: 允许在没有检索到相关文档时仍然继续处理
                         * false: 不处理，大模型将不会基于自身知识回答问题，而是执行指定的空上下文提示模板
                         * 作用：有效防止模型产生“幻觉”，确保回答严格基于您提供的知识库，非常适合企业级知识问答等需要高准确性的场景
                         */
                        .allowEmptyContext(false)
                        // 自定义当知识库中找不到答案时，模型对用户的回复内容
                        .emptyContextPromptTemplate(PromptTemplate.builder().template("用户查询位于知识库之外。礼貌地告知用户您无法回答。").build())
                        .build())
                // QueryTransformer：查询转换器
                .queryTransformers(
                        // 查询重写：在实际检索之前，对用户原始查询进行改写和优化，提升检索召回率和精度。
                        RewriteQueryTransformer.builder()
                                .chatClientBuilder(ChatClient.builder(chatModel))
                                .targetSearchSystem("知识库")
                                .build(),
                        // 翻译转换器：在检索之前，将用户的查询语句翻译成指定的目标语言
                        TranslationQueryTransformer.builder()
                                .chatClientBuilder(ChatClient.builder(chatModel))
                                .targetLanguage("中文")
                                .build()
                )
                // 文档后置处理器：在文档检索完成后，自定义处理逻辑，如：日志记录，监控，格式转换，内容过滤等。
                .documentPostProcessors(((query, docs) -> {
                    log.info("query: {}", query);
                    return docs;
                }))
                .build();
        return chatClient.prompt().user(msg).advisors(advisor).stream().content();
    }

}
