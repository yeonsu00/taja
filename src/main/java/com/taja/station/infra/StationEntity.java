package com.taja.station.infra;

import com.taja.station.domain.Station;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "station")
@RequiredArgsConstructor
public class StationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stationId;

    private String name;

    private String number;

    private String district;

    private String address;

    private double latitude;

    private double longitude;

    private int lcd;

    private int qr;

    private String operationMethod;

    @Builder
    public StationEntity(String name, String number, String district, String address, double latitude, double longitude, int lcd, int qr) {
        this.name = name;
        this.number = number;
        this.district = district;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lcd = lcd;
        this.qr = qr;
        this.operationMethod = getOperationMethod(lcd, qr);
    }

    private String getOperationMethod(int lcd, int qr) {
        if (lcd > 0 && qr > 0) {
            return "LCD/QR";
        } else if (lcd > 0) {
            return "LCD";
        } else if (qr > 0) {
            return "QR";
        }
        return "None";
    }

    public static StationEntity fromStation(Station station) {
        return StationEntity.builder()
                .name(station.getName())
                .number(station.getNumber())
                .district(station.getDistrict())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .lcd(station.getLcd())
                .qr(station.getQr())
                .build();
    }
}
