package com.taja.application.statistics.dto;

import com.taja.domain.station.Station;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record StationDistricts(Map<Long, String> stationIdToDistrictMap) {

    public static StationDistricts from(List<Station> stations) {
        Map<Long, String> stationIdToDistrictMap = stations.stream()
                .collect(Collectors.toMap(
                        Station::getStationId,
                        Station::getDistrict,
                        (existing, replacement) -> existing
                ));
        return new StationDistricts(stationIdToDistrictMap);
    }

    public String getDistrict(Long stationId) {
        String district = stationIdToDistrictMap.get(stationId);

        if (district == null) {
            throw new IllegalArgumentException("대여소 정보를 찾을 수 없습니다. (Station ID: " + stationId + ")");
        }

        return district;
    }

}
