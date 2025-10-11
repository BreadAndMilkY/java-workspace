package com.zhengqing.saa.api;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.alibaba.cloud.ai.dashscope.image.DashScopeImageModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.image.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/multi-modality")
@Tag(name = "03-多模态")
public class _03_MultiModalityController {

    @Resource(type = DashScopeImageModel.class)
    private ImageModel imageModel;
    @Resource(type = DashScopeChatModel.class)
    private ChatModel chatModel;


    /**
     * http://127.0.0.1:888/multi-modality/text2Image
     */
    @Operation(summary = "文生图")
    @GetMapping("/text2Image")
    public Object text2Image(
            @RequestParam(value = "subject", defaultValue = "一只会编程的猫") String subject,
            @RequestParam(value = "environment", defaultValue = "办公室") String environment,
            @RequestParam(value = "height", defaultValue = "1664") Integer height,
            @RequestParam(value = "width", defaultValue = "928") Integer width,
            @RequestParam(value = "style", defaultValue = "生动") String style) {

        String prompt = String.format(
                "一个%s，置身于%s的环境中，使用%s的艺术风格，高清4K画质，细节精致",
                subject, environment, style
        );

        ImageOptions options = ImageOptionsBuilder.builder()
                .model("qwen-image-plus")
                .height(height)
                .width(width)
                .build();

        ImageResponse response = imageModel.call(new ImagePrompt(prompt, options));
//        return response.getResult().getOutput().getUrl(); // Image generation still pending
        return response;
    }


    /**
     * 多模态 -- 图片理解
     * http://127.0.0.1:888/multi-modality/image2Text
     */
    @Operation(summary = "图片理解")
    @GetMapping("/image2Text")
    public String image2Text() {

        DashScopeChatOptions chatOptions = DashScopeChatOptions.builder()
                .withMultiModel(true) // 开启多模态
                .withModel("qwen3-vl-plus")
//                .withTopP(0.7)
                .build();


        Prompt prompt = Prompt.builder()
                .chatOptions(chatOptions)
                .messages(
                        UserMessage.builder()
                                .text("请用中文解释图片内容")
                                .media(new Media(MimeTypeUtils.IMAGE_PNG, new ClassPathResource("/static/multimodal.png")))
                                .build()
                )
                .build();

        return chatModel.call(prompt).getResult().getOutput().getText();
    }


}
