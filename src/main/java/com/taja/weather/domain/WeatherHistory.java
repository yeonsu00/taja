package com.taja.weather.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "weather_history",
    indexes = {
        @Index(name = "idx_weather_base_date_district_time", 
               columnList = "baseDate, district, baseTime")
    }
)
@RequiredArgsConstructor
@Getter
public class WeatherHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long weatherHistoryId;

    private LocalDate baseDate;

    private LocalTime baseTime;

    private String district;

    private Double temperature;

    private Double hourlyRain; // 1시간 강수량

    private Double windSpeed; // 풍속

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
