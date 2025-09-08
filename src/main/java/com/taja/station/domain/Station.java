package com.taja.station.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Station {

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
    public Station(String name, Integer number, String district, String address, double latitude, double longitude,
                   Integer lcdHoldCount, Integer qrHoldCount, Integer totalHoldCount, String operationMethod) {
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

}
