package com.taja.infrastructure.client.weather;

import com.taja.infrastructure.client.weather.dto.WeatherApiResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "weatherFeignClient", url = "${api.weather.base-url}")
public interface KmaWeatherFeignClient {

    @GetMapping
    WeatherApiResponseDto getUltraShortNowcast(
            @RequestParam("authKey") String authKey,
            @RequestParam("dataType") String dataType,
            @RequestParam("numOfRows") int numOfRows,
            @RequestParam("pageNo") int pageNo,
            @RequestParam("base_date") String baseDate,
            @RequestParam("base_time") String baseTime,
            @RequestParam("nx") int nx,
            @RequestParam("ny") int ny);

}
