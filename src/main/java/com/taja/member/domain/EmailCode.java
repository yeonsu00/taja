package com.taja.member.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class EmailCode {

    private Long emailCodeId;

    private String email;

    private String code;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    @Builder
    private EmailCode(Long emailCodeId, String email, String code, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.emailCodeId = emailCodeId;
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static EmailCode of(String email, String authCode, LocalDateTime createdAt, LocalDateTime expiresAt) {
        return EmailCode.builder()
                .email(email)
                .code(authCode)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .build();
    }
}
