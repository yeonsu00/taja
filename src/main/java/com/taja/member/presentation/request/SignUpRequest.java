package com.taja.member.presentation.request;

public record SignUpRequest(
        String name,
        String email,
        String password
) {
}
