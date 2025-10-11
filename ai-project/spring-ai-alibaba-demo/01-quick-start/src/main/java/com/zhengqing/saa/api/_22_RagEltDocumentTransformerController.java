package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag/elt/document-transformer")
@Tag(name = "22-RAG-ELT-DocumentTransformer（对文档进行转换操作）")
public class _22_RagEltDocumentTransformerController {

    @Value("classpath:rag/phone.md")
    private Resource MdRes;


    private ChatModel chatModel;
    private VectorStore vectorStore;

    public _22_RagEltDocumentTransformerController(DashScopeChatModel dashScopeChatModel,
                                                   DashScopeEmbeddingModel dashScopeEmbeddingModel) {
        chatModel = dashScopeChatModel;
        vectorStore = SimpleVectorStore.builder(dashScopeEmbeddingModel).build();
    }


    /**
     * KeywordMetadataEnricher 文档关键字提取
     * http://localhost:888/rag/elt/document-transformer/keyword
     */
    @GetMapping("/keyword")
    public Object keyword() {
        // 1. 创建Markdown文档读取器
        MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(MdRes, MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("filename", MdRes.getFilename())
                .build());
        // 2. 读取文档内容
        List<Document> documents = markdownReader.read();
        // 3. 关键字提取
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 3);
        List<Document> enricherDocs = enricher.apply(documents);
        return enricherDocs;
    }

    /**
     * FilterExpressionBuilder Metadata元数据过滤
     * http://localhost:888/rag/elt/document-transformer/metadata-filter
     */
    @GetMapping("/metadata-filter")
    public Object metadataFilter() {
        // 1. 创建Markdown文档读取器
        MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(MdRes, MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("filename", MdRes.getFilename())
                .build());
        // 2. 读取文档内容
        List<Document> documents = markdownReader.read();
        // 3. 关键字提取
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(chatModel, 3);
        List<Document> enricherDocs = enricher.apply(documents);
        // 4. 存储向量
        vectorStore.add(enricherDocs);
        // 5. 相似性搜索
        List<Document> result = vectorStore.similaritySearch(SearchRequest.builder()
                .query("手机处理器")
                .topK(3)
                // 基于元数据的过滤条件
//                .filterExpression("filename=='phone.md'")
                .filterExpression(new FilterExpressionBuilder().eq("filename", "phone.md").build())
                .build());
        // 6. 返回结果
        return result;
    }

    /**
     * SummaryMetadataEnricher 摘要生成
     * http://localhost:888/rag/elt/document-transformer/summary
     */
    @GetMapping("/summary")
    public Object summary() {
        // 1. 创建Markdown文档读取器
        MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(MdRes, MarkdownDocumentReaderConfig.builder()
                .withAdditionalMetadata("filename", MdRes.getFilename())
                .build());
        // 2. 读取文档内容
        List<Document> documents = markdownReader.read();
        // 3. 摘要生成
        SummaryMetadataEnricher enricher = new SummaryMetadataEnricher(chatModel,
                // 指定要生成的摘要类型，这里包括当前文档和下一部分的摘要。
                List.of(SummaryMetadataEnricher.SummaryType.CURRENT, SummaryMetadataEnricher.SummaryType.NEXT),
                // 使用自定义提示模板
                """
                                请为以下文档内容生成摘要:
                                {context_str}
                        
                                要求:
                                1. 摘要长度不超过100字
                                2. 突出关键信息
                                3. 保持原意不变
                        """,
                // 生成摘要时使用所有元数据
                MetadataMode.ALL);
        List<Document> enricherDocs = enricher.apply(documents);
        return enricherDocs;
    }

}
