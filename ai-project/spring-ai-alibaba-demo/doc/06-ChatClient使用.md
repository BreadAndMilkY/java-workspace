# ChatClient使用

### 一、ChatClient VS ChatModel

1、定位和抽象层级

- `ChatClient`: 更高层次的抽象，提供了简化的聊天接口和流式处理能力
- `ChatModel`: 底层模型接口，直接与具体的AI模型交互

2、使用复杂度

- `ChatClient`:
    - 提供构建器模式(`ChatClient.Builder`)
    - 内置默认配置和便捷方法
    - 更易于上手和快速开发
- `ChatModel`:
    - 需要手动处理请求/响应对象
    - 更多底层控制选项
    - 需要更多样板代码

3、适用场景

- `ChatClient`: 适合快速开发、原型设计和大多数常见应用场景
- `ChatModel`: 适合需要精细控制、自定义逻辑或特殊需求的场景

### 二、快速上手

[_04_ChatClientController.java](../01-quick-start/src/main/java/com/zhengqing/saa/api/_04_ChatClientController.java)

```java
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat-client")
@Tag(name = "04-ChatClient")
public class _04_ChatClientController {

    private final ChatClient chatClient;

    public _04_ChatClientController(DashScopeChatModel dashScopeChatModel) {
        // 如果引入多个大模型依赖，需要指定具体模型
        chatClient = ChatClient.builder(dashScopeChatModel).build();
    }

    /**
     * 简单调用
     * http://localhost:888/chat-client/simple/chat?msg=你是谁？
     */
    @Operation(summary = "简单调用")
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatClient.prompt().user(msg).call().content();
    }

    /**
     * 流式调用
     * http://localhost:888/chat-client/stream/chat?msg=你是谁？
     */
    @Operation(summary = "流式调用")
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam(defaultValue = "你是谁？") String msg) {
        return chatClient.prompt().user(msg).stream().content();
    }

}
```