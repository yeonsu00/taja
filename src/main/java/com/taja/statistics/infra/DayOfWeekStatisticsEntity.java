package com.taja.statistics.infra;

import com.taja.global.BaseEntity;
import com.taja.statistics.domain.DayOfWeekStatistics;
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
public class DayOfWeekStatisticsEntity extends BaseEntity {

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
    private DayOfWeekStatisticsEntity(Long dayOfWeekStatisticsId, Long stationId, DayOfWeek dayOfWeek,
                                     Integer avgParkingBikeCount) {
        this.dayOfWeekStatisticsId = dayOfWeekStatisticsId;
        this.stationId = stationId;
        this.dayOfWeek = dayOfWeek;
        this.avgParkingBikeCount = avgParkingBikeCount;
    }

    public static DayOfWeekStatisticsEntity fromDomain(DayOfWeekStatistics statistics) {
        return DayOfWeekStatisticsEntity.builder()
                .dayOfWeekStatisticsId(statistics.getDayOfWeekStatisticsId())
                .stationId(statistics.getStationId())
                .dayOfWeek(statistics.getDayOfWeek())
                .avgParkingBikeCount(statistics.getAvgParkingBikeCount())
                .build();
    }

    public DayOfWeekStatistics toDomain() {
        return DayOfWeekStatistics.builder()
                .dayOfWeekStatisticsId(this.dayOfWeekStatisticsId)
                .stationId(this.stationId)
                .dayOfWeek(this.dayOfWeek)
                .avgParkingBikeCount(this.avgParkingBikeCount)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }

    public void update(DayOfWeekStatistics statistics) {
        this.avgParkingBikeCount = statistics.getAvgParkingBikeCount();
    }
}
