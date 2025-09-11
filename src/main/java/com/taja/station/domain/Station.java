package com.taja.station.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Station {

    private Long stationId;

    private String name;

    private Integer number;

    private String district;

    private String address;

    private double latitude;

    private double longitude;

    private Integer lcdHoldCount;

    private Integer qrHoldCount;

    private Integer totalHoldCount;

    private String operationMethod;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Station(Long stationId, String name, Integer number, String district, String address, double latitude, double longitude,
                   Integer lcdHoldCount, Integer qrHoldCount, Integer totalHoldCount, String operationMethod) {
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
        this.operationMethod = operationMethod;
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

}
