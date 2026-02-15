package com.taja.infrastructure.client.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taja.domain.weather.WeatherHistory;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public record WeatherApiResponseDto(
        @JsonProperty("response") WeatherResponseDto response
) {

    public WeatherHistory toWeatherHistory(String district) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");

        Double temperature = response.body().items().findValueByCategory("T1H");
        Double hourlyRain = response.body().items().findValueByCategory("RN1");
        Double windSpeed = response.body().items().findValueByCategory("WSD");

        return WeatherHistory.of(
                LocalDate.parse(response.body().items().item().getFirst().baseDate(), dateFormatter),
                LocalTime.parse(response.body().items().item().getFirst().baseTime(), timeFormatter),
                district,
                temperature,
                hourlyRain,
                windSpeed
        );
    }

}
