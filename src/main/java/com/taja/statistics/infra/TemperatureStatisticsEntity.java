package com.taja.statistics.infra;

import com.taja.global.BaseEntity;
import com.taja.statistics.domain.TemperatureStatistics;
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
public class TemperatureStatisticsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long temperatureStatisticsId;

    @Column(nullable = false)
    private Long stationId;

    @Column(nullable = false)
    private Double temperatureRange;

    private Integer avgParkingBikeCount;

    @Builder
    private TemperatureStatisticsEntity(Long temperatureStatisticsId, Long stationId, Double temperatureRange,
                                       Integer avgParkingBikeCount) {
        this.temperatureStatisticsId = temperatureStatisticsId;
        this.stationId = stationId;
        this.temperatureRange = temperatureRange;
        this.avgParkingBikeCount = avgParkingBikeCount;
    }

    public static TemperatureStatisticsEntity fromDomain(TemperatureStatistics statistics) {
        return TemperatureStatisticsEntity.builder()
                .temperatureStatisticsId(statistics.getTemperatureStatisticsId())
                .stationId(statistics.getStationId())
                .temperatureRange(statistics.getTemperatureRange())
                .avgParkingBikeCount(statistics.getAvgParkingBikeCount())
                .build();
    }

    public TemperatureStatistics toDomain() {
        return TemperatureStatistics.builder()
                .temperatureStatisticsId(this.temperatureStatisticsId)
                .stationId(this.stationId)
                .temperatureRange(this.temperatureRange)
                .avgParkingBikeCount(this.avgParkingBikeCount)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    public void update(TemperatureStatistics statistics) {
        this.avgParkingBikeCount = statistics.getAvgParkingBikeCount();
    }
}
