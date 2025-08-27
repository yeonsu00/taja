package com.taja.station.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Station {

    private String name;

    private String number;

    private String district;

    private String address;

    private double latitude;

    private double longitude;

    private int lcd;

    private int qr;

    @Builder
    public Station(String name, String number, String district, String address, double latitude, double longitude, int lcd, int qr) {
        this.name = name;
        this.number = number;
        this.district = district;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lcd = lcd;
        this.qr = qr;
    }

}
