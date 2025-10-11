package com.zhengqing.saa.tools;

import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

/**
 * <p> 时间工具 </p>
 *
 * @author zhengqingya
 * @description
 * @date 2025/10/3 1:11
 */
@Slf4j
@Service
public class TimeTools {

    // returnDirect: true:直接返回工具结果 false:传递回大模型，由大模型处理最终响应结果。
    @Tool(description = "获取当前时间，默认时间格式：YYYY-MM-DD HH:mm:ss", returnDirect = false)
    public String getCurrentTime(@ToolParam(description = "时间格式") String format) {
        log.info("获取当前时间格式：{}", format);
        return DateUtil.now();
    }

}
