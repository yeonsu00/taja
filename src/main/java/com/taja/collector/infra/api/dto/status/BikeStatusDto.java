package com.taja.collector.infra.api.dto.status;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record BikeStatusDto(
        @JsonProperty("list_total_count") int totalCount,
        @JsonProperty("RESULT") ResultDto result,
        @JsonProperty("row") List<StationStatusDto> stationStatuses
) {
}
