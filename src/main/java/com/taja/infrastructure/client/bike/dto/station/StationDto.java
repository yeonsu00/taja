package com.taja.infrastructure.client.bike.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public record StationDto(
        @JsonProperty("STA_LOC") String location,
        @JsonProperty("RENT_ID") String rentId,
        @JsonProperty("RENT_NO") String rentNumber,
        @JsonProperty("RENT_NM") String rentName,
        @JsonProperty("RENT_ID_NM") String rentIdName,
        @JsonProperty("HOLD_NUM") String holdNumber,
        @JsonProperty("STA_ADD1") String address1,
        @JsonProperty("STA_ADD2") String address2,
        @JsonProperty("STA_LAT") String latitude,
        @JsonProperty("STA_LONG") String longitude,
        @JsonProperty("START_INDEX") int startIndex,
        @JsonProperty("END_INDEX") int endIndex,
        @JsonProperty("RNUM") String rowNumber
) {

    public static List<Station> toStations(List<StationDto> stationDtos) {
        return stationDtos.stream()
                .map(StationDto::toStation)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<Station> toStation() {
        if (rentName == null || rentNumber == null || location == null
                || address1 == null || latitude == null || longitude == null) {
            log.warn("대여소 데이터 필터링 - 필수 필드 null: RENT_NM={}, RENT_NO={}, STA_LOC={}, STA_ADD1={}, STA_LAT={}, STA_LONG={}, HOLD_NUM={}",
                    rentName, rentNumber, location, address1, latitude, longitude, holdNumber);
            return Optional.empty();
        }

        try {
            Integer number = Integer.parseInt(rentNumber);
            
            Integer holdCount = null;
            if (holdNumber != null && !holdNumber.trim().isEmpty()) {
                holdCount = Integer.parseInt(holdNumber.trim());
            }

            double lat = latitude.isEmpty() ? 0.0 : Double.parseDouble(latitude);
            double lon = longitude.isEmpty() ? 0.0 : Double.parseDouble(longitude);

            Station station = Station.builder()
                    .name(rentName.trim())
                    .number(number)
                    .district(location.trim())
                    .address(address1.trim())
                    .latitude(lat)
                    .longitude(lon)
                    .totalHoldCount(holdCount)
                    .operationMode(OperationMode.NEW)
                    .build();

            return Optional.of(station);

        } catch (NumberFormatException e) {
            log.warn("대여소 데이터 필터링 - 숫자 파싱 실패: RENT_NM={}, RENT_NO={}, STA_LAT={}, STA_LONG={}, 오류={}",
                    rentName, rentNumber, latitude, longitude, e.getMessage());
            return Optional.empty();
        }
    }



}
