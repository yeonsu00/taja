package com.taja.station.domain;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "stations")
@RequiredArgsConstructor
@Getter
public class Station extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private Integer number;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private Integer lcdHoldCount;

    private Integer qrHoldCount;

    private Integer totalHoldCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OperationMode operationMode;

    @Builder
    public Station(Long stationId, String name, Integer number, String district, String address, double latitude, double longitude,
                   Integer lcdHoldCount, Integer qrHoldCount, Integer totalHoldCount, OperationMode operationMode) {
        this.stationId = stationId;
        this.name = name;
        this.number = number;
        this.district = district;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lcdHoldCount = lcdHoldCount;
        this.qrHoldCount = qrHoldCount;
        this.totalHoldCount = totalHoldCount;
        this.operationMode = operationMode;
    }

    public void update(Station station) {
        this.name = station.getName();
        this.district = station.getDistrict();
        this.address = station.getAddress();
        this.latitude = station.getLatitude();
        this.longitude = station.getLongitude();
        this.lcdHoldCount = station.getLcdHoldCount();
        this.qrHoldCount = station.getQrHoldCount();
        this.totalHoldCount = station.getTotalHoldCount();
        this.operationMode = station.getOperationMode();
    }

    public int calculateDistanceTo(double centerLat, double centerLon) {
        double KM_PER_DEGREE_LAT = 111.0;

        double latDelta = this.latitude - centerLat;
        double lonDelta = this.longitude - centerLon;

        double x = lonDelta * KM_PER_DEGREE_LAT * Math.cos(Math.toRadians(centerLat));
        double y = latDelta * KM_PER_DEGREE_LAT;

        double distanceM = Math.sqrt(x * x + y * y) * 1000;

        return (int) (Math.floor(distanceM / 10) * 10);
    }

    public static List<Long> toStationIds(List<Station> stations) {
        return stations.stream()
                .map(Station::getStationId)
                .toList();
    }

}
