package com.taja.station.infra;

import com.taja.station.domain.Station;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "station")
@RequiredArgsConstructor
@Getter
public class StationEntity {

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

    private Integer lcd;

    private Integer qr;

    private String operationMethod; // "LCD", "QR", "LCD/QR"

    @Builder
    public StationEntity(String name, Integer number, String district, String address, double latitude, double longitude, Integer lcd, Integer qr, String operationMethod) {
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
                .operationMethod(station.getOperationMethod())
                .build();
    }

    public void update(Station station) {
        this.name = station.getName();
        this.district = station.getDistrict();
        this.address = station.getAddress();
        this.latitude = station.getLatitude();
        this.longitude = station.getLongitude();
        this.lcd = station.getLcd();
        this.qr = station.getQr();
        this.operationMethod = station.getOperationMethod();
    }
}
