package com.zhengqing.saa.splitter;

import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 学校规章制度专用文档分割器 - 针对规章制度结构优化
 * 支持按"第.*条"分割，保留规章制度的结构完整性
 */
public class RegulationDocumentSplitter extends TextSplitter {

    private final int chunkSize;
    private final int chunkOverlap;
    private final int minChunkSizeChars;
    private final List<String> separators;
    private final boolean keepSeparator;
    private final boolean preserveRegulationStructure;

    // 使用Builder模式
    public static Builder builder() {
        return new Builder();
    }

    public RegulationDocumentSplitter() {
        // 为学校规章制度提供优化的默认参数
        this(1000, 100, 200,
                Arrays.asList("第.*条", "\n\n", "\n", "。", "；", "，", " "),
                true, true);
    }

    public RegulationDocumentSplitter(int chunkSize, int chunkOverlap, int minChunkSizeChars,
                                      List<String> separators, boolean keepSeparator,
                                      boolean preserveRegulationStructure) {
        this.chunkSize = chunkSize;
        this.chunkOverlap = chunkOverlap;
        this.minChunkSizeChars = minChunkSizeChars;
        this.separators = separators;
        this.keepSeparator = keepSeparator;
        this.preserveRegulationStructure = preserveRegulationStructure;
    }

    @Override
    public List<Document> split(List<Document> documents) {
        List<Document> allChunks = new ArrayList<>();
        for (Document doc : documents) {
            List<String> textChunks = this.splitText(doc.getText());
            for (String chunkText : textChunks) {
                // 继承原始文档的元数据，这对后续检索和溯源至关重要
                Document chunkDoc = new Document(chunkText, doc.getMetadata());
                allChunks.add(chunkDoc);
            }
        }
        return allChunks;
    }

    @Override
    protected List<String> splitText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new ArrayList<>();
        }

        // 如果是学校规章制度且需要保持条款结构，先按条款分割
        if (preserveRegulationStructure) {
            return splitByRegulations(text);
        } else {
            return recursiveSplit(text, this.chunkSize, this.separators);
        }
    }

    /**
     * 专门针对学校规章制度的分割方法 - 按条款分割
     */
    private List<String> splitByRegulations(String text) {
        List<String> regulations = new ArrayList<>();

        // 使用正则表达式匹配所有条款
        Pattern regulationPattern = Pattern.compile("(第[零一二三四五六七八九十百千]+条[^第]*)");
        Matcher matcher = regulationPattern.matcher(text);

        int lastEnd = 0;
        while (matcher.find()) {
            // 添加条款之前的内容（如果有）
            if (matcher.start() > lastEnd) {
                String beforeRegulation = text.substring(lastEnd, matcher.start()).trim();
                if (!beforeRegulation.isEmpty() && beforeRegulation.length() >= minChunkSizeChars) {
                    regulations.add(beforeRegulation);
                }
            }

            // 添加当前条款
            String regulation = matcher.group(1).trim();
            regulations.add(regulation);
            lastEnd = matcher.end();
        }

        // 添加最后一部分内容（如果有）
        if (lastEnd < text.length()) {
            String remaining = text.substring(lastEnd).trim();
            if (!remaining.isEmpty() && remaining.length() >= minChunkSizeChars) {
                regulations.add(remaining);
            }
        }

        // 对于过长的条款，进行进一步分割
        List<String> finalChunks = new ArrayList<>();
        for (String regulation : regulations) {
            if (regulation.length() > chunkSize) {
                // 对过长的条款使用递归分割
                List<String> subChunks = recursiveSplit(regulation, chunkSize,
                        separators.subList(1, separators.size())); // 跳过"第.*条"分隔符
                finalChunks.addAll(subChunks);
            } else {
                finalChunks.add(regulation);
            }
        }

        return finalChunks;
    }

    /**
     * 递归分块核心逻辑
     */
    private List<String> recursiveSplit(String text, int targetSize, List<String> separatorList) {
        List<String> finalChunks = new ArrayList<>();
        if (text.length() <= targetSize) {
            finalChunks.add(text);
            return finalChunks;
        }

        // 查找最佳分隔符
        SeparatorMatch bestMatch = findBestSeparator(text, separatorList);
        if (bestMatch == null) {
            return fixedLengthSplit(text, targetSize);
        }

        String separator = bestMatch.getSeparator();
        boolean isRegex = bestMatch.isRegex();
        String actualSeparator = bestMatch.getActualMatch();

        // 使用找到的分隔符分割文本
        List<String> sections = splitWithSeparator(text, separator, isRegex, actualSeparator);
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();

        for (String section : sections) {
            String candidate;
            if (isRegex && keepSeparator) {
                candidate = currentChunk.length() > 0 ?
                        currentChunk.toString() + actualSeparator + section : section;
            } else {
                candidate = currentChunk.length() > 0 ?
                        currentChunk.toString() + (keepSeparator ? actualSeparator : "") + section : section;
            }

            if (candidate.length() > targetSize) {
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                    currentChunk = new StringBuilder();
                    // 添加重叠部分
                    if (chunkOverlap > 0 && !chunks.isEmpty()) {
                        String lastChunk = chunks.get(chunks.size() - 1);
                        int overlapStart = Math.max(0, lastChunk.length() - chunkOverlap);
                        String overlapText = lastChunk.substring(overlapStart);
                        currentChunk.append(overlapText);
                    }
                }
                // 对当前过长的section进行递归分割
                List<String> subChunks = recursiveSplit(section, targetSize,
                        separatorList.subList(1, separatorList.size()));
                chunks.addAll(subChunks);
            } else {
                if (isRegex && keepSeparator) {
                    currentChunk.append(currentChunk.length() > 0 ? actualSeparator + section : section);
                } else {
                    currentChunk.append(currentChunk.length() > 0 ?
                            (keepSeparator ? actualSeparator : "") + section : section);
                }
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        // 过滤过小的块
        for (String chunk : chunks) {
            if (chunk.length() >= minChunkSizeChars) {
                finalChunks.add(chunk.trim());
            }
        }

        return finalChunks;
    }

    /**
     * 查找最佳分隔符
     */
    private SeparatorMatch findBestSeparator(String text, List<String> separators) {
        for (String sep : separators) {
            if (isRegexPattern(sep)) {
                try {
                    Pattern pattern = Pattern.compile(sep);
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        return new SeparatorMatch(sep, true, matcher.group());
                    }
                } catch (Exception e) {
                    continue;
                }
            } else {
                if (text.contains(sep)) {
                    return new SeparatorMatch(sep, false, sep);
                }
            }
        }
        return null;
    }

    /**
     * 判断字符串是否为正则表达式模式
     */
    private boolean isRegexPattern(String str) {
        return str.contains(".*") || str.contains(".+") || str.contains("?") ||
                str.contains("^") || str.contains("$") || str.contains("[") ||
                str.contains("]") || str.contains("(") || str.contains(")") ||
                str.contains("|") || str.contains("{") || str.contains("}");
    }

    /**
     * 使用分隔符分割文本
     */
    private List<String> splitWithSeparator(String text, String separator, boolean isRegex, String actualSeparator) {
        if (isRegex) {
            return Arrays.asList(text.split(Pattern.quote(actualSeparator), -1));
        } else {
            return Arrays.asList(text.split(Pattern.quote(separator), -1));
        }
    }

    private List<String> fixedLengthSplit(String text, int targetSize) {
        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + targetSize, text.length());
            String chunk = text.substring(start, end);
            if (chunk.length() >= minChunkSizeChars) {
                chunks.add(chunk);
            }
            start = end;
        }
        return chunks;
    }

    /**
     * 内部类，用于存储分隔符匹配结果
     */
    private static class SeparatorMatch {
        private final String separator;
        private final boolean isRegex;
        private final String actualMatch;

        public SeparatorMatch(String separator, boolean isRegex, String actualMatch) {
            this.separator = separator;
            this.isRegex = isRegex;
            this.actualMatch = actualMatch;
        }

        public String getSeparator() {
            return separator;
        }

        public boolean isRegex() {
            return isRegex;
        }

        public String getActualMatch() {
            return actualMatch;
        }
    }

    public static final class Builder {
        private int chunkSize = 1000;
        private int chunkOverlap = 100;
        private int minChunkSizeChars = 200;
        private List<String> separators = Arrays.asList("第.*条", "\n\n", "\n", "。", "；", "，", " ");
        private boolean keepSeparator = true;
        private boolean preserveRegulationStructure = true;

        private Builder() {
        }

        public Builder withChunkSize(int chunkSize) {
            this.chunkSize = chunkSize;
            return this;
        }

        public Builder withChunkOverlap(int chunkOverlap) {
            this.chunkOverlap = chunkOverlap;
            return this;
        }

        public Builder withMinChunkSizeChars(int minChunkSizeChars) {
            this.minChunkSizeChars = minChunkSizeChars;
            return this;
        }

        public Builder withSeparators(List<String> separators) {
            this.separators = separators;
            return this;
        }

        public Builder withKeepSeparator(boolean keepSeparator) {
            this.keepSeparator = keepSeparator;
            return this;
        }

        public Builder withPreserveRegulationStructure(boolean preserveRegulationStructure) {
            this.preserveRegulationStructure = preserveRegulationStructure;
            return this;
        }

        public RegulationDocumentSplitter build() {
            return new RegulationDocumentSplitter(chunkSize, chunkOverlap, minChunkSizeChars,
                    separators, keepSeparator, preserveRegulationStructure);
        }
    }
}
