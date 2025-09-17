package com.taja.member.presentation.request;

public record LoginRequest(
        String email,
        String password
) {
}
