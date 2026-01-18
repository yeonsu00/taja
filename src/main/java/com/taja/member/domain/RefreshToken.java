package com.taja.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@RequiredArgsConstructor
@Getter
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    @Column(name = "token_key", nullable = false, unique = true)
    private String key;

    @Column(name = "token_value", nullable = false, unique = true)
    private String value;

    @Builder
    private RefreshToken(Long refreshTokenId, String key, String value) {
        this.refreshTokenId = refreshTokenId;
        this.key = key;
        this.value = value;
    }

    public static RefreshToken of(String key, String value) {
        return RefreshToken.builder()
                .key(key)
                .value(value)
                .build();
    }

    public void updateValue(String value) {
        this.value = value;
    }
}
