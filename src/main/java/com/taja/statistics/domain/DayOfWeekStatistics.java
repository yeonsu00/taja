package com.taja.statistics.domain;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class DayOfWeekStatistics {

    private Long dayOfWeekStatisticsId;

    private Long stationId;

    private DayOfWeek dayOfWeek;

    private Integer avgParkingBikeCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public DayOfWeekStatistics(Long dayOfWeekStatisticsId, Long stationId, DayOfWeek dayOfWeek,
                              Integer avgParkingBikeCount, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.dayOfWeekStatisticsId = dayOfWeekStatisticsId;
        this.stationId = stationId;
        this.dayOfWeek = dayOfWeek;
        this.avgParkingBikeCount = avgParkingBikeCount;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static DayOfWeekStatistics create(Long stationId, DayOfWeek dayOfWeek, Integer avgParkingBikeCount) {
        return DayOfWeekStatistics.builder()
                .stationId(stationId)
                .dayOfWeek(dayOfWeek)
                .avgParkingBikeCount(avgParkingBikeCount)
                .build();
    }

    public DayOfWeekStatistics updateAverage(Integer newAvgParkingBikeCount) {
        return DayOfWeekStatistics.builder()
                .dayOfWeekStatisticsId(this.dayOfWeekStatisticsId)
                .stationId(this.stationId)
                .dayOfWeek(this.dayOfWeek)
                .avgParkingBikeCount(newAvgParkingBikeCount)
                .createdAt(this.createdAt)
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
