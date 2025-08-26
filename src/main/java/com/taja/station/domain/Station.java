package com.taja.station.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Station {

    private String name;

    private String number;

    private String address;

    private double latitude;

    private double longitude;

    @Builder
    public Station(String name, String number, String address, double latitude, double longitude) {
        this.name = name;
        this.number = number;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
