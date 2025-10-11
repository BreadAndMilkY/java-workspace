package com.zhengqing.saa.api;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag/elt/split")
@Tag(name = "20-RAG-ELT-文档分割器")
public class _20_RagEltSplitterController {

    @Value("classpath:rag/pet.txt")
    private Resource textRes;

    /**
     * 文本读取器
     * http://localhost:888/rag/elt/split/text
     */
    @GetMapping("/text")
    public Object text() {
        // 1. 创建文本读取器
        TextReader textReader = new TextReader(textRes);
        // 2. 读取文档内容
        List<Document> documents = textReader.read();
        // 3. 文档分割处理
//        List<Document> splitDocs = new TokenTextSplitter().split(documents);
        /**
         * `TokenTextSplitter` 构造函数的参数作用如下：
         *
         * - `chunkSize`: 每个文本块的目标大小（以token为单位），控制分割后每个文档片段的长度
         * - `minChunkSizeChars`: 文本块的最小字符数大小，确保分割后的文本块不会过小
         * - `minChunkLengthToEmbed`: 最小嵌入长度，用于确定文本块是否足够长以进行向量化处理
         * - `maxNumChunks`: 最大文本块数量，限制分割后生成的文档块总数
         * - `keepSeparator`: 是否保留分隔符，控制在分割过程中是否保留原文本中的分隔符号
         */
        List<Document> splitDocs = new TokenTextSplitter(800, 350, 5, 10000, true).split(documents);
        // 4. 返回结果
        return splitDocs;
    }

    @Value("classpath:rag/iphone.md")
    private Resource MdRes;

    /**
     * markdown读取器
     * http://localhost:888/rag/elt/split/markdown
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

        // 3. 使用TokenTextSplitter对文档进行分割
        //        List<Document> splitDocs = new TokenTextSplitter().split(documents);
        /**
         * `TokenTextSplitter` 构造函数的参数作用如下：
         *
         * - `chunkSize`: 每个文本块的目标大小（以token为单位），控制分割后每个文档片段的长度
         * - `minChunkSizeChars`: 文本块的最小字符数大小，确保分割后的文本块不会过小
         * - `minChunkLengthToEmbed`: 最小嵌入长度，用于确定文本块是否足够长以进行向量化处理
         * - `maxNumChunks`: 最大文本块数量，限制分割后生成的文档块总数
         * - `keepSeparator`: 是否保留分隔符，控制在分割过程中是否保留原文本中的分隔符号
         */
        List<Document> splitDocs = new TokenTextSplitter(800, 350, 5, 10000, true).split(documents);
        // 4. 返回分割后的文档列表
        return splitDocs;
    }

}
