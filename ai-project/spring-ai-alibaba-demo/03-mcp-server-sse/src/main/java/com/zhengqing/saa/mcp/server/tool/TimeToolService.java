package com.zhengqing.saa.mcp.server.tool;

import cn.hutool.core.date.DateUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class TimeToolService {

    @Tool(description = "获取时间")
    public String getTime() {
        return DateUtil.now();
    }

}