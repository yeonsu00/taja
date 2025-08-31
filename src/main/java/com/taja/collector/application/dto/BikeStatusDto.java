package com.taja.collector.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record BikeStatusDto(
        @JsonProperty("list_total_count") int totalCount,
        @JsonProperty("RESULT") ResultDto result,
        @JsonProperty("row") List<StationDto> stations
) {
}
