# Advisor对话拦截 -- 实现日志记录

在 Spring AI 中，Advisor 主要用于：

- 拦截 ChatClient 的对话请求和响应
- 在对话过程中添加通用处理逻辑
- 实现日志记录、性能监控、安全检查等功能

---

### 代码示例

开启SimpleLoggerAdvisor所在的debug日志

```yaml
logging:
  level:
    org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor: DEBUG
```

[_07_AdvisorLogController.java](../01-quick-start/src/main/java/com/zhengqing/saa/api/_07_AdvisorLogController.java)

```java
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/advisor")
@Tag(name = "07-Advisor对话拦截--日志记录")
public class _07_AdvisorLogController {

    private ChatClient chatClient;

    public _07_AdvisorLogController(DashScopeChatModel dashScopeChatModel) {
        chatClient = ChatClient.builder(dashScopeChatModel)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    /**
     * http://localhost:888/advisor/log?msg=你好
     */
    @GetMapping("/log")
    public Flux<String> log(@RequestParam String msg) {
        return chatClient.prompt()
                .user(msg)
                .stream().content();
    }

}
```

控制台日志输出：

```shell
2025-10-02T22:51:09.267+08:00 DEBUG 21392 --- [-nio-888-exec-1] o.s.a.c.c.advisor.SimpleLoggerAdvisor    : request: ChatClientRequest[prompt=Prompt{messages=[UserMessage{content='你好', properties={messageType=USER}, messageType=USER}], modelOptions=DashScopeChatOptions: {"model":"qwen-plus","temperature":0.8,"enable_search":false,"incremental_output":true,"enable_thinking":false,"multi_model":false}}, context={}]
2025-10-02T22:51:10.442+08:00 DEBUG 21392 --- [oundedElastic-1] o.s.a.c.c.advisor.SimpleLoggerAdvisor    : response: {
  "result" : {
    "metadata" : {
      "finishReason" : "STOP",
      "contentFilters" : [ ],
      "empty" : true
    },
    "output" : {
      "messageType" : "ASSISTANT",
      "metadata" : {
        "search_info" : "",
        "role" : "ASSISTANT",
        "messageType" : "ASSISTANT",
        "finishReason" : "STOP",
        "id" : "a5c3c9f9-294e-4c02-8431-0aa9045dce4e",
        "reasoningContent" : ""
      },
      "toolCalls" : [ ],
      "media" : [ ],
      "text" : "你好呀！✨ 很高兴见到你！今天过得怎么样呀？希望你度过了愉快的一天。我随时准备好陪你聊天、帮你解决问题，或者就这样轻松愉快地闲聊一会儿。有什么想跟我分享的吗？ 🌟"
    }
  },
  "metadata" : {
    "id" : "a5c3c9f9-294e-4c02-8431-0aa9045dce4e",
    "model" : "",
    "rateLimit" : {
      "tokensLimit" : 0,
      "requestsLimit" : 0,
      "tokensReset" : 0.0,
      "tokensRemaining" : 0,
      "requestsReset" : 0.0,
      "requestsRemaining" : 0
    },
    "usage" : {
      "promptTokens" : 9,
      "completionTokens" : 51,
      "totalTokens" : 60,
      "nativeUsage" : {
        "promptTokens" : 9,
        "totalTokens" : 60,
        "completionTokens" : 51
      }
    },
    "promptMetadata" : [ ],
    "empty" : true
  },
  "results" : [ {
    "metadata" : {
      "finishReason" : "STOP",
      "contentFilters" : [ ],
      "empty" : true
    },
    "output" : {
      "messageType" : "ASSISTANT",
      "metadata" : {
        "search_info" : "",
        "role" : "ASSISTANT",
        "messageType" : "ASSISTANT",
        "finishReason" : "STOP",
        "id" : "a5c3c9f9-294e-4c02-8431-0aa9045dce4e",
        "reasoningContent" : ""
      },
      "toolCalls" : [ ],
      "media" : [ ],
      "text" : "你好呀！✨ 很高兴见到你！今天过得怎么样呀？希望你度过了愉快的一天。我随时准备好陪你聊天、帮你解决问题，或者就这样轻松愉快地闲聊一会儿。有什么想跟我分享的吗？ 🌟"
    }
  } ]
}
```