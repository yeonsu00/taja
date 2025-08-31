package com.taja.collector.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResultDto(
        @JsonProperty("CODE") String code,
        @JsonProperty("MESSAGE") String message
) {
}
