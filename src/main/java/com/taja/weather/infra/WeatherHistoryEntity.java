package com.taja.weather.infra;

import com.taja.weather.domain.WeatherHistory;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "weather_history")
@RequiredArgsConstructor
public class WeatherHistoryEntity {

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
    private WeatherHistoryEntity(Long weatherHistoryId, LocalDate baseDate, LocalTime baseTime, String district,
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

    public static WeatherHistoryEntity fromWeatherHistory(WeatherHistory weatherHistory) {
        return WeatherHistoryEntity.builder()
                .baseDate(weatherHistory.getBaseDate())
                .baseTime(weatherHistory.getBaseTime())
                .district(weatherHistory.getDistrict())
                .temperature(weatherHistory.getTemperature())
                .hourlyRain(weatherHistory.getHourlyRain())
                .windSpeed(weatherHistory.getWindSpeed())
                .requestedAt(weatherHistory.getRequestedAt())
                .build();
    }
}
