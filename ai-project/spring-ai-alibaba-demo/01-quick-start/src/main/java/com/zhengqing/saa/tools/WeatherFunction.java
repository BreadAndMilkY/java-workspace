package com.zhengqing.saa.tools;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.function.Function;

/**
 * <p> 天气工具 </p>
 *
 * @author zhengqingya
 * @description
 * @date 2025/10/3 1:11
 */
@Slf4j
@Service(value = "getWeather")
public class WeatherFunction implements Function<String, String> {

    @Override
    @Description("获取天气")
    public String apply(@JsonPropertyDescription("城市") String city) {
        log.info("获取{}天气", city);
        return city + "天气：晴";
    }

}
