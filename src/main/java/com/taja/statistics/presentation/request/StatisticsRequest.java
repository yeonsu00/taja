package com.taja.statistics.presentation.request;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

public record StatisticsRequest(
        @NotBlank(message = "요청일자는 필수 입력값입니다.")
        LocalDate requestedAt
) {
}
