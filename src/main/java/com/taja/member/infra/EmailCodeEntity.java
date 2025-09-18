package com.taja.member.infra;

import com.taja.member.domain.EmailCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "email_code")
@RequiredArgsConstructor
public class EmailCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emailCodeId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    private EmailCodeEntity(Long emailCodeId, String email, String code, LocalDateTime createdAt,
                           LocalDateTime expiresAt) {
        this.emailCodeId = emailCodeId;
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public static EmailCodeEntity fromEmailCode(EmailCode emailCode) {
        return EmailCodeEntity.builder()
                .email(emailCode.getEmail())
                .code(emailCode.getCode())
                .createdAt(emailCode.getCreatedAt())
                .expiresAt(emailCode.getExpiresAt())
                .build();
    }
}
