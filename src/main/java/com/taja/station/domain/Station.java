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

    private Integer lcd;

    private Integer qr;

    private String operationMethod;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    public Station(String name, Integer number, String district, String address, double latitude, double longitude,
                   Integer lcd, Integer qr, String operationMethod) {
        this.name = name;
        this.number = number;
        this.district = district;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lcd = lcd;
        this.qr = qr;
        this.operationMethod = operationMethod;
    }

}
