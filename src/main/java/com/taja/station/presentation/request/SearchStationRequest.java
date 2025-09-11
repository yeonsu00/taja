package com.taja.station.presentation.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record SearchStationRequest(

        @NotBlank(message = "검색어를 입력해주세요.")
        @NotEmpty(message = "검색어는 비어 있을 수 없습니다.")
        String keyword,

        @NotNull(message = "사용자 위치 위도(lat)는 필수 입력값입니다.")
        @DecimalMin(value = "-90.0", message = "위도는 -90 이상이어야 합니다.")
        @DecimalMax(value = "90.0", message = "위도는 90 이하이어야 합니다.")
        Double lat,

        @NotNull(message = "사용자 위치 경도(lon)는 필수 입력값입니다.")
        @DecimalMin(value = "-180.0", message = "경도는 -180 이상이어야 합니다.")
        @DecimalMax(value = "180.0", message = "경도는 180 이하이어야 합니다.")
        Double lon

) {
}
