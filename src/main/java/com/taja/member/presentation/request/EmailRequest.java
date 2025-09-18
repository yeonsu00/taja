package com.taja.member.presentation.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailRequest(
        @NotBlank(message = "이메일은 필수 입력입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email
) {
}
