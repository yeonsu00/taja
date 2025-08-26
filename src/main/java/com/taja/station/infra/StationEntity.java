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

    private String address;

    private double latitude;

    private double longitude;

    @Builder
    public StationEntity(String name, String number, String address, double latitude, double longitude) {
        this.name = name;
        this.number = number;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static StationEntity fromStation(Station station) {
        return StationEntity.builder()
                .name(station.getName())
                .number(station.getNumber())
                .address(station.getAddress())
                .latitude(station.getLatitude())
                .longitude(station.getLongitude())
                .build();
    }
}
