package com.taja.member.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CheckDuplicateNameRequest(
        @NotBlank(message = "닉네임은 필수 입력입니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]{1,8}$", message = "영어 대/소문자, 숫자, 한글만 허용합니다. 최대 8자입니다.")
        String name
) {
}
