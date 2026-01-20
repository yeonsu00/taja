package com.taja.domain.statistics;

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
public class DayOfWeekStatistics extends StatisticsBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dayOfWeekStatisticsId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek dayOfWeek;

    @Builder
    private DayOfWeekStatistics(Long dayOfWeekStatisticsId, Long stationId, DayOfWeek dayOfWeek,
                                Integer avgParkingBikeCount, Long sampleCount) {
        super(stationId, avgParkingBikeCount, sampleCount);
        this.dayOfWeekStatisticsId = dayOfWeekStatisticsId;
        this.dayOfWeek = dayOfWeek;
    }

    public static DayOfWeekStatistics create(Long stationId, DayOfWeek dayOfWeek, Integer avgParkingBikeCount) {
        return DayOfWeekStatistics.builder()
                .stationId(stationId)
                .dayOfWeek(dayOfWeek)
                .avgParkingBikeCount(avgParkingBikeCount)
                .sampleCount(1L)
                .build();
    }
}
