package com.taja.statistics.infra;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "temperature_statistics")
@RequiredArgsConstructor
public class TemperatureStatisticsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long temperatureStatisticsId;

    @Column(nullable = false)
    private Long stationId;

    @Column(nullable = false)
    private Double temperatureRange;

    private Integer avgParkingBikeCount;
}
