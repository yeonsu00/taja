package com.taja.collector.infra.api.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taja.station.domain.Station;
import java.util.Optional;

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

    public Optional<Station> toStation() {
        if (rentName == null || rentNumber == null || location == null
                || address1 == null || latitude == null || longitude == null || holdNumber == null) {
            return Optional.empty();
        }

        try {
            Integer number = Integer.parseInt(rentNumber);
            Integer holdCount = Integer.parseInt(holdNumber);

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
                    .operationMethod("NEW")
                    .build();

            return Optional.of(station);

        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

}
