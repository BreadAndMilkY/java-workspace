# 文档分割器

`TokenTextSplitter`是 RAG（检索增强生成）应用中一个关键的文档预处理工具，它的核心作用是将长文档智能地切割成适合大语言模型（LLM）处理的小文本块。

| 核心作用       | 解决的问题                                                      | 最终目标                                 |
|:-----------|:-----------------------------------------------------------|:-------------------------------------|
| 适配LLM上下文窗口 | LLM有单次处理的文本长度上限（如4K或8K tokens）。长文档（如报告、手册）会远超此限制，导致无法完整输入。 | 将长文档“化整为零”，使每个小块都能被LLM有效处理。          |
| 提升检索精度与效率  | 未分块的长文档包含多个主题，检索时容易返回包含大量无关信息的“大杂烩”结果，干扰判断且计算量大。           | 让每个文本块聚焦单一主题或语义单元，使向量检索更精准、更快速。      |
| 保障生成内容质量   | 如果输入LLM的上下文信息冗长或包含无关内容（噪声），LLM可能被误导，生成不相关或不准确的回答。          | 为LLM提供“精准、干净”的上下文，从源头提升生成答案的相关性和准确性。 |

### 🔧 工作原理与关键参数

`TokenTextSplitter`的独特之处在于它按 Token（而非简单字符）进行分割。Token 是 LLM 处理文本的基本单位，因此这种分割方式能确保分块结果与 LLM 的内部处理逻辑完全一致。

它的工作流程和关键参数如下：

1. 编码：使用与 LLM 一致的编码方式（如 OpenAI 的 `tiktoken`库）将输入文本转换为 Token 序列。
2. 切割：根据设定的 `defaultChunkSize`（目标块大小）将 Token 序列初步切割。
3. 优化：尝试在每个切分点附近寻找自然断点（如句号、换行符），以避免在句子中间生硬地切断，尽可能保持语义完整。
4. 后处理：修剪空白字符，并根据 `minChunkLengthToEmbed`等参数过滤掉过短的无效块。

关键配置参数示例（以 Spring AI 实现为例）：

```
new TokenTextSplitter(800, 350, 5, 10000, true).split(documents);
```

| 参数名                     | 说明                          | 默认值示例   |
|:------------------------|:----------------------------|:--------|
| `defaultChunkSize`      | 每个文本块的目标 Token 数量。          | `800`   |
| `minChunkSizeChars`     | 每个文本块的最小字符数，低于此值则不分割。       | `350`   |
| `minChunkLengthToEmbed` | 可进行向量嵌入的文本块最小长度，短于此值的块会被丢弃。 | `5`     |
| `maxNumChunks`          | 单个文档被分割的最大块数，防止超长文档产生过多分块。  | `10000` |
| `keepSeparator`         | 是否保留用于分割的标点或符号（如句号、换行符）。    | `true`  |

### 💡 应用示例与策略

在实际使用中，通常先使用文档加载器读取原始文件，然后应用 `TokenTextSplitter`。

[_20_RagEltSplitterController.java](../01-quick-start/src/main/java/com/zhengqing/saa/api/_20_RagEltSplitterController.java)

```java
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
        List<Document> splitDocs = new TokenTextSplitter(800, 350, 5, 10000, true).split(documents);
        // 4. 返回分割后的文档列表
        return splitDocs;
    }

}
```

选择分块大小（如 `defaultChunkSize`）时，需要在语义完整性和信息聚焦度之间做权衡：

- 块太大（例如2000 tokens）：可能包含多个主题，检索时容易引入噪声，降低精度。
- 块太小（例如100 tokens）：可能导致语义被割裂，例如将一个完整的操作步骤分散到多个块中，使LLM难以理解完整上下文。
- 常见实践：对于一般知识库文章，256 到 512 个 tokens 是一个不错的起点。对于技术文档或法律条文等结构严谨的文本，可以尝试更大的块（如1024）。

### ⚠️ 注意事项

- 并非万能：`TokenTextSplitter`是一种基于长度和简单规则的通用分割器。对于结构复杂的文档（如HTML、Markdown），按结构分块（Structural Chunking）或递归分块（Recursive Chunking）策略可能效果更好，它们能识别标题、代码块等逻辑结构。
- 重叠策略：一些高级的分块器支持设置 `chunk_overlap`，让相邻块之间有一小部分内容重叠。这有助于防止关键信息恰好在分块边界被切断，保持上下文的连贯性。

