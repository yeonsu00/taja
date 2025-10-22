package com.taja.weather.application;

import com.taja.api.weather.WeatherApiClient;
import com.taja.api.weather.dto.WeatherApiResponseDto;
import com.taja.weather.domain.DistrictPoint;
import com.taja.weather.domain.WeatherHistory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WeatherApiService {

    private final WeatherApiClient weatherApiClient;

    public List<WeatherHistory> loadWeatherHistories(List<DistrictPoint> districtPoints, LocalDateTime requestedAt) {
        String baseDate = requestedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = requestedAt.format(DateTimeFormatter.ofPattern("HHmm"));

        List<WeatherHistory> weatherHistories = new ArrayList<>();

        for (DistrictPoint districtPoint : districtPoints) {
            WeatherApiResponseDto weatherApiResponseDto = weatherApiClient.getUltraShortNowcast(baseDate, baseTime,
                    districtPoint.getXPoint(), districtPoint.getYPoint());
            weatherHistories.add(weatherApiResponseDto.toWeatherHistory(districtPoint.getDistrictName()));
        }

        return weatherHistories;
    }

}
