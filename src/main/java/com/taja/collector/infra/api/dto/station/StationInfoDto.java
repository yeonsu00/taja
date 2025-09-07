package com.taja.collector.infra.api.dto.station;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.taja.collector.infra.api.dto.status.ResultDto;
import java.util.List;

public record StationInfoDto(
        @JsonProperty("list_total_count") String listTotalCount,
        @JsonProperty("RESULT") ResultDto result,
        @JsonProperty("row") List<StationDto> stations
) {
}
