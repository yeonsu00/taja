package com.taja.statistics.domain;

import com.taja.global.BaseEntity;
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
public class HourlyStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long hourlyStatisticsId;

    @Column(nullable = false)
    private Long stationId;

    @Column(nullable = false)
    private Integer hour;

    private Integer avgParkingBikeCount;

    @Builder
    private HourlyStatistics(Long hourlyStatisticsId, Long stationId, Integer hour, Integer avgParkingBikeCount) {
        this.hourlyStatisticsId = hourlyStatisticsId;
        this.stationId = stationId;
        this.hour = hour;
        this.avgParkingBikeCount = avgParkingBikeCount;
    }

    public static HourlyStatistics create(Long stationId, Integer hour, Integer avgParkingBikeCount) {
        return HourlyStatistics.builder()
                .stationId(stationId)
                .hour(hour)
                .avgParkingBikeCount(avgParkingBikeCount)
                .build();
    }

    public void updateAvgParkingBikeCount(Integer newAvgParkingBikeCount) {
        this.avgParkingBikeCount = (newAvgParkingBikeCount + this.avgParkingBikeCount) / 2;
    }
}
