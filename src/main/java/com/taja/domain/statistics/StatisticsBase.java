package com.taja.domain.statistics;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class StatisticsBase extends BaseEntity {

    @Column(nullable = false)
    private Long stationId;

    private Integer avgParkingBikeCount;

    private Long sampleCount;

    protected StatisticsBase(Long stationId, Integer avgParkingBikeCount, Long sampleCount) {
        this.stationId = stationId;
        this.avgParkingBikeCount = avgParkingBikeCount;
        this.sampleCount = sampleCount;
    }

    public void updateAvgParkingBikeCount(Integer newAvgParkingBikeCount) {
        if (this.sampleCount == null || this.sampleCount == 0) {
            this.avgParkingBikeCount = newAvgParkingBikeCount;
            this.sampleCount = 1L;
        } else {
            long totalSum = (long) this.avgParkingBikeCount * this.sampleCount + newAvgParkingBikeCount;
            this.sampleCount++;
            this.avgParkingBikeCount = (int) (totalSum / this.sampleCount);
        }
    }
}
