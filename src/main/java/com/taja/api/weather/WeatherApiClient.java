package com.taja.api.weather;

import com.taja.api.weather.dto.WeatherApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class WeatherApiClient {

    private final WebClient webClient = WebClient.create();

    @Value("${api.weather.base-url}")
    private String baseUrl;

    @Value("${api.weather.key}")
    private String apiKey;

    public WeatherApiResponseDto getUltraShortNowcast(String baseDate, String baseTime, int xPoint, int yPoint) {
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
                        .build() // 3. URI 빌드 완료
                )
                .retrieve()
                .bodyToMono(WeatherApiResponseDto.class)
                .block();
    }
}
