package com.taja.application.member;

public record TokenDto(
        String accessToken,
        String refreshToken
) {
}
