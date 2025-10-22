package com.taja.weather.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class WeatherHistory {

    private Long weatherHistoryId;

    private LocalDate baseDate;

    private LocalTime baseTime;

    private String district;

    private Double temperature;

    private Double hourlyRain;

    private Double windSpeed;

    private LocalDateTime requestedAt;

    @Builder
    private WeatherHistory(Long weatherHistoryId, LocalDate baseDate, LocalTime baseTime, String district,
                           Double temperature, Double hourlyRain, Double windSpeed, LocalDateTime requestedAt) {
        this.weatherHistoryId = weatherHistoryId;
        this.baseDate = baseDate;
        this.baseTime = baseTime;
        this.district = district;
        this.temperature = temperature;
        this.hourlyRain = hourlyRain;
        this.windSpeed = windSpeed;
        this.requestedAt = requestedAt;
    }

    public static WeatherHistory of(LocalDate baseDate, LocalTime baseTime, String district, Double temperature,
                                    Double hourlyRain, Double windSpeed) {
        return WeatherHistory.builder()
                .baseDate(baseDate)
                .baseTime(baseTime)
                .district(district)
                .temperature(temperature)
                .hourlyRain(hourlyRain)
                .windSpeed(windSpeed)
                .build();
    }

    public void updateRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }
}
