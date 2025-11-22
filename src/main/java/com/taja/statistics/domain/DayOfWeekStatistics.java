package com.taja.statistics.domain;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.DayOfWeek;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "day_of_week_statistics",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_day_of_week_statistics_station_day", 
            columnNames = {"stationId", "dayOfWeek"}
        )
    }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class DayOfWeekStatistics extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayOfWeekStatisticsId;

    @Column(nullable = false)
    private Long stationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    private Integer avgParkingBikeCount;

    @Builder
    private DayOfWeekStatistics(Long dayOfWeekStatisticsId, Long stationId, DayOfWeek dayOfWeek,
                                Integer avgParkingBikeCount) {
        this.dayOfWeekStatisticsId = dayOfWeekStatisticsId;
        this.stationId = stationId;
        this.dayOfWeek = dayOfWeek;
        this.avgParkingBikeCount = avgParkingBikeCount;
    }

    public static DayOfWeekStatistics create(Long stationId, DayOfWeek dayOfWeek, Integer avgParkingBikeCount) {
        return DayOfWeekStatistics.builder()
                .stationId(stationId)
                .dayOfWeek(dayOfWeek)
                .avgParkingBikeCount(avgParkingBikeCount)
                .build();
    }

    public void updateAvgParkingBikeCount(Integer newAvgParkingBikeCount) {
        this.avgParkingBikeCount = (newAvgParkingBikeCount + this.avgParkingBikeCount) / 2;
    }
}
