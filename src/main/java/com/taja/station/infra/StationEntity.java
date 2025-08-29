package com.taja.station.infra;

import com.taja.station.domain.Station;
import jakarta.persistence.Column;
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

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String number;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    private Integer lcd;

    private Integer qr;

    private String operationMethod; // "LCD", "QR", "LCD/QR", "None"

    @Builder
    public StationEntity(String name, String number, String district, String address, double latitude, double longitude, Integer lcd, Integer qr) {
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

    private String getOperationMethod(Integer lcd, Integer qr) {
        if (lcd != null && qr != null ) {
            return "LCD/QR";
        } else if (lcd != null) {
            return "LCD";
        } else if (qr != null) {
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
