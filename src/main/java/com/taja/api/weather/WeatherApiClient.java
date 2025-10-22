package com.taja.api.weather;

import com.taja.api.weather.dto.WeatherApiResponseDto;
import com.taja.global.exception.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherApiClient {

    private final WebClient webClient = WebClient.create();

    private static final String SUCCESS_RESULT_CODE = "00";

    @Value("${api.weather.base-url}")
    private String baseUrl;

    @Value("${api.weather.key}")
    private String apiKey;

    public Mono<WeatherApiResponseDto> getUltraShortNowcast(String baseDate, String baseTime, int xPoint, int yPoint) {
        return webClient.get()
                .uri(baseUrl, uriBuilder -> uriBuilder
                        .queryParam("authKey", apiKey)
                        .queryParam("dataType", "JSON")
                        .queryParam("numOfRows", 10)
                        .queryParam("pageNo", 1)
                        .queryParam("base_date", baseDate)
                        .queryParam("base_time", baseTime)
                        .queryParam("nx", xPoint)
                        .queryParam("ny", yPoint)
                        .build()
                )
                .retrieve()
                .bodyToMono(WeatherApiResponseDto.class)
                .flatMap(weatherApiResponseDto -> {
                    if (weatherApiResponseDto == null || weatherApiResponseDto.response() == null || weatherApiResponseDto.response().header() == null) {
                        return Mono.error(new ApiException("JSON_PARSING_ERROR", "API 응답 DTO 파싱에 실패했습니다."));
                    }

                    String resultCode = weatherApiResponseDto.response().header().resultCode();
                    String resultMsg = weatherApiResponseDto.response().header().resultMsg();

                    if (SUCCESS_RESULT_CODE.equals(resultCode)) {
                        return Mono.just(weatherApiResponseDto);
                    }

                    return Mono.error(new ApiException(resultCode, resultMsg));
                });
    }
}
