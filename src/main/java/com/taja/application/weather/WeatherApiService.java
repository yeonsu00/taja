package com.taja.application.weather;

import com.taja.infrastructure.api.weather.WeatherApiBatchFetcher;
import com.taja.infrastructure.api.weather.WeatherApiClient;
import com.taja.domain.weather.DistrictPoint;
import com.taja.domain.weather.WeatherHistory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherApiService {

    private final WeatherApiClient weatherApiClient;
    private final WeatherApiBatchFetcher weatherApiBatchFetcher;
    private final WeatherHistoryRepository weatherHistoryRepository;

    private static final int CONCURRENT_REQUESTS = 5;

    public void loadAndSaveWeatherHistories(List<DistrictPoint> districtPoints, LocalDateTime requestedAt) {
        String baseDate = requestedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = requestedAt.format(DateTimeFormatter.ofPattern("HHmm"));

        Function<DistrictPoint, Mono<WeatherHistory>> fetchFunction = (point) -> weatherApiClient.getUltraShortNowcast(
                        baseDate, baseTime, point.getXPoint(), point.getYPoint())
                .map(weatherApiResponseDto -> weatherApiResponseDto.toWeatherHistory(point.getDistrictName()));

        weatherApiBatchFetcher.fetchAllConcurrently(districtPoints, fetchFunction, CONCURRENT_REQUESTS,
                        DistrictPoint::getDistrictName)
                .publishOn(Schedulers.boundedElastic())
                .subscribe(
                        weatherHistories -> {
                            weatherHistoryRepository.saveAll(weatherHistories, requestedAt);
                            log.info("{}건의 날씨 정보를 DB에 저장했습니다.", weatherHistories.size());
                        },
                        error -> {
                            log.error("날씨 정보 수집 작업 중 오류 발생", error);
                        }
                );
    }
}
