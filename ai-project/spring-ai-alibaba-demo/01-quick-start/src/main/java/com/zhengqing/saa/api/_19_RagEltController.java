package com.zhengqing.saa.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag/elt")
@Tag(name = "19-RAG-ELT")
public class _19_RagEltController {

    @Value("classpath:rag/pet.txt")
    private Resource textRes;

    /**
     * 文本读取器
     * http://localhost:888/rag/elt/text
     */
    @GetMapping("/text")
    public Object text() {
        // 1. 创建文本读取器
        TextReader textReader = new TextReader(textRes);
        // 2. 读取文档内容
        List<Document> documents = textReader.read();
        // 3. 返回结果
        return documents;
    }

    @Value("classpath:rag/iphone.md")
    private Resource MdRes;

    /**
     * markdown读取器
     * http://localhost:888/rag/elt/markdown
     */
    @GetMapping("/markdown")
    public Object markdown() {
        // 1. 创建Markdown文档读取器
        MarkdownDocumentReader markdownReader = new MarkdownDocumentReader(MdRes, MarkdownDocumentReaderConfig.builder()
                // 添加额外的元数据信息，将文件名作为metadata存储在生成的Document对象中
                .withAdditionalMetadata("filename", MdRes.getFilename())
                // 设置是否将水平线(---)作为文档分割符，默认为true，设为false时表示不创建水平线分割的独立文档
                .withHorizontalRuleCreateDocument(false)
                // 设置是否包含代码块内容，设为false表示读取时排除Markdown中的代码块部分
                .withIncludeCodeBlock(false)
                // 设置是否包含引用块内容，设为false表示读取时排除Markdown中的引用块部分
                .withIncludeBlockquote(false)
                .build());
        // 2. 读取Markdown文档内容
        List<Document> documents = markdownReader.read();
        // 3. 返回文档列表
        return documents;
    }


}
