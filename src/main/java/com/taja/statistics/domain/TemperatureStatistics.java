package com.taja.statistics.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "temperature_statistics",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_temperature_statistics_station_temp", 
            columnNames = {"stationId", "temperatureRange"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class TemperatureStatistics extends StatisticsBase {

    public static final double TEMPERATURE_RANGE_INTERVAL = 5.0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long temperatureStatisticsId;

    @Column(nullable = false)
    private Double temperatureRange;

    @Builder
    public TemperatureStatistics(Long temperatureStatisticsId, Long stationId, Double temperatureRange,
                                 Integer avgParkingBikeCount, Long sampleCount) {
        super(stationId, avgParkingBikeCount, sampleCount);
        this.temperatureStatisticsId = temperatureStatisticsId;
        this.temperatureRange = temperatureRange;
    }

    public static TemperatureStatistics create(Long stationId, Double temperatureRange, Integer avgParkingBikeCount) {
        return TemperatureStatistics.builder()
                .stationId(stationId)
                .temperatureRange(temperatureRange)
                .avgParkingBikeCount(avgParkingBikeCount)
                .sampleCount(1L)
                .build();
    }

    public static Double getTemperatureRangeStart(Double temperature) {
        return Math.floor(temperature / TEMPERATURE_RANGE_INTERVAL) * TEMPERATURE_RANGE_INTERVAL;
    }
}
