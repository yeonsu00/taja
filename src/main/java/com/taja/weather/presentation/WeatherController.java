package com.taja.weather.presentation;

import com.taja.global.response.CommonApiResponse;
import com.taja.weather.application.WeatherService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/weather")
public class WeatherController {

    private final WeatherService weatherService;

    @PostMapping("/district/upload")
    public CommonApiResponse<String> readDistrictPointFile(@RequestParam("file") MultipartFile file) {
        int count = weatherService.uploadDistrictPointFromFile(file);
        return CommonApiResponse.success(count + "개 자치구의 지점 좌표가 등록 및 수정되었습니다.");
    }

    @PostMapping("/upload")
    public CommonApiResponse<String> readWeatherHistories() {
        LocalDateTime now = LocalDateTime.now();
        weatherService.saveWeatherHistories(now);
        return CommonApiResponse.success("초단기실황 날씨 데이터가 등록되었습니다.");
    }

}
