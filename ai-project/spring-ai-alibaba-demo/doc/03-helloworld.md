# 快速入门

- jdk17
- springboot 3.4.0
- spring ai 1.0.0
- spring ai alibaba 1.0.0.3

### 一、`pom.xml`引入依赖

```
<properties>
    <!-- Spring Boot -->
    <spring-boot.version>3.4.0</spring-boot.version>
    
    <!-- Spring AI -->
    <spring-ai.version>1.0.0</spring-ai.version>

    <!-- Spring AI Alibaba -->
    <!-- Install Spring AI Alibaba in your local. -->
    <spring-ai-alibaba.version>1.0.0.3</spring-ai-alibaba.version>
</properties>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    
        <!-- 用于为所有组件做统一版本管理 -->
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>${spring-ai.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        <dependency>
            <groupId>com.alibaba.cloud.ai</groupId>
            <artifactId>spring-ai-alibaba-bom</artifactId>
            <version>${spring-ai-alibaba.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

```
<dependency>
    <groupId>com.alibaba.cloud.ai</groupId>
    <artifactId>spring-ai-alibaba-starter-dashscope</artifactId>
</dependency>
```

### 二、yaml配置

`${AI_DASHSCOPE_API_KEY}` 配置见 [01-windows环境api-key配置.md](01-windows环境api-key配置.md)

```yaml
server:
  port: 888
  servlet:
    encoding:
      enabled: true
      charset: utf-8
      force: true

spring:
  ai:
    dashscope:
      api-key: ${AI_DASHSCOPE_API_KEY}
```

### 三、AI智能对话浅尝

[_01_HelloWorldController.java](../01-quick-start/src/main/java/com/zhengqing/saa/api/_01_HelloWorldController.java)

```java
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
@RestController
public class _01_HelloWorldController {

    private final ChatModel chatModel;

    /**
     * 简单调用
     * http://localhost:888/simple/chat?msg=你是谁？
     */
    @GetMapping("/simple/chat")
    public String simpleChat(@RequestParam String msg) {
        return chatModel.call(msg);
    }

    /**
     * 流式调用
     * http://localhost:888/stream/chat?msg=你是谁？
     */
    @GetMapping("/stream/chat")
    public Flux<String> streamChat(@RequestParam String msg) {
        return chatModel.stream(msg);
    }

}
```