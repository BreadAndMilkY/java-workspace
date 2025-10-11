package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/rag/fact-check")
@Tag(name = "25-RAG-幻觉评估")
public class _25_RagFactCheckController {

    private ChatModel chatModel;

    public _25_RagFactCheckController(DashScopeChatModel dashScopeChatModel) {
        chatModel = dashScopeChatModel;
    }

    /**
     * http://localhost:888/rag/fact-check/test
     */
    @GetMapping("/test")
    public Object test() {
        // 准备评估数据
        ArrayList<Document> documents = Lists.newArrayList(
                Document.builder().text("iPhone18 基础价格: 5999元, 税率: 13%, 运费: 0元").build(),
                Document.builder().text("iPhone20 基础价格: 12999元, 税率: 13%, 运费: 50元").build(),
                Document.builder().text("iPhone10 基础价格: 1299元, 税率: 13%, 运费: 0元").build()
        );


        // AI回答
        String aiRes = "iPhone20 6000元";

        // 执行评估
        FactCheckingEvaluator evaluator = new FactCheckingEvaluator(ChatClient.builder(chatModel));
        EvaluationRequest evaluationRequest = new EvaluationRequest(documents, aiRes);
        EvaluationResponse evaluationResponse = evaluator.evaluate(evaluationRequest);
        return evaluationResponse; // {"pass":false,"score":0.0,"feedback":"","metadata":{}}
    }

}
