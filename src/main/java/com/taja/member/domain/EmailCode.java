package com.taja.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "email_code")
@RequiredArgsConstructor
@Getter
public class EmailCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailCodeId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
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

    public void update(String code, LocalDateTime createdAt, LocalDateTime expiresAt) {
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public boolean isValid() {
        return LocalDateTime.now().isBefore(this.expiresAt);
    }
}
