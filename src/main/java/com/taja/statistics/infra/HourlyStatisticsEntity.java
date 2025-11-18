package com.taja.statistics.infra;

import com.taja.global.BaseEntity;
import com.taja.statistics.domain.HourlyStatistics;
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
@Table(name = "hourly_statistics",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_hourly_statistics_station_hour",
                        columnNames = {"stationId", "hour"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class HourlyStatisticsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hourlyStatisticsId;

    @Column(nullable = false)
    private Long stationId;

    @Column(nullable = false)
    private Integer hour;

    private Integer avgParkingBikeCount;

    @Builder
    private HourlyStatisticsEntity(Long hourlyStatisticsId, Long stationId, Integer hour, Integer avgParkingBikeCount) {
        this.hourlyStatisticsId = hourlyStatisticsId;
        this.stationId = stationId;
        this.hour = hour;
        this.avgParkingBikeCount = avgParkingBikeCount;
    }

    public static HourlyStatisticsEntity fromHourlyStatistics(HourlyStatistics statistics) {
        return HourlyStatisticsEntity.builder()
                .hourlyStatisticsId(statistics.getHourlyStatisticsId())
                .stationId(statistics.getStationId())
                .hour(statistics.getHour())
                .avgParkingBikeCount(statistics.getAvgParkingBikeCount())
                .build();
    }

    public HourlyStatistics toHourlyStatistics() {
        return HourlyStatistics.builder()
                .hourlyStatisticsId(this.hourlyStatisticsId)
                .stationId(this.stationId)
                .hour(this.hour)
                .avgParkingBikeCount(this.avgParkingBikeCount)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    public void updateAvgParkingBikeCount(Integer newAvgParkingBikeCount) {
        this.avgParkingBikeCount = newAvgParkingBikeCount;
    }
}
