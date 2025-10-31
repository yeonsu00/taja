package com.taja.member.application.dto;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
