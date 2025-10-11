package com.zhengqing.saa.mcp.server.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
public class MyWeatherService {

    @Tool(description = "获取天气")
    public String getWeather(@ToolParam(description = "城市") String city) {
        // 模拟数据，实际应用中应调用真实API
        return city + "天气：雨";
    }

}