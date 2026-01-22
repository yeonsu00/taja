package com.taja.application.weather;

import com.taja.domain.weather.DistrictPoint;
import com.taja.domain.weather.WeatherHistory;
import com.taja.infrastructure.api.weather.WeatherApiClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherApiService {

    private final WeatherApiClient weatherApiClient;
    private final WeatherHistoryRepository weatherHistoryRepository;

    public void loadAndSaveWeatherHistories(
            List<DistrictPoint> districtPoints, LocalDateTime requestedAt) {
        String baseDate = requestedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = requestedAt.format(DateTimeFormatter.ofPattern("HHmm"));

        List<WeatherHistory> weatherHistories =
                districtPoints.stream()
                        .map(
                                point -> {
                                    try {
                                        return weatherApiClient
                                                .getUltraShortNowcast(
                                                        baseDate,
                                                        baseTime,
                                                        point.getXPoint(),
                                                        point.getYPoint())
                                                .map(
                                                        weatherApiResponseDto ->
                                                                weatherApiResponseDto.toWeatherHistory(
                                                                        point.getDistrictName()))
                                                .block();
                                    } catch (Exception e) {
                                        log.error(
                                                "날씨 정보를 가져오는 중 오류가 발생했습니다. district: {}",
                                                point.getDistrictName(),
                                                e);
                                        return null;
                                    }
                                })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        weatherHistoryRepository.saveAll(weatherHistories, requestedAt);
        log.info("{}건의 날씨 정보를 DB에 저장했습니다.", weatherHistories.size());
    }
}
