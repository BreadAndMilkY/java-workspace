package com.zhengqing.saa.api;

import com.zhengqing.saa.splitter.RegulationDocumentSplitter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rag/elt/custom-splitter")
@Tag(name = "21-RAG-ELT-自定义文档分割器")
public class _21_RagEltCustomSplitterController {

    @Value("classpath:rag/custom.txt")
    private Resource textRes;

    /**
     * http://localhost:888/rag/elt/custom-splitter/text?isCustom=true
     */
    @GetMapping("/text")
    public Object text(@RequestParam Boolean isCustom) {
        // 1. 创建文本读取器
        TextReader textReader = new TextReader(textRes);
        // 2. 读取文档内容
        List<Document> documents = textReader.read();

        if (!isCustom) {
            // 3. 使用普通文本分割器对文档进行分割
            return new TokenTextSplitter().split(documents);
        }
        // 3. 使用自定义分割器对文档进行分割
        RegulationDocumentSplitter splitter = RegulationDocumentSplitter.builder()
                .withChunkSize(300)          // 条文通常较短
                .withChunkOverlap(120)        // 适当增加重叠部分
                .withMinChunkSizeChars(150)   // 最小块大小
                .withKeepSeparator(true)      // 保留分隔符（如"第一条"）
                .withPreserveRegulationStructure(true) // 保持条款结构
                .build();
        return splitter.split(documents);
    }

}
