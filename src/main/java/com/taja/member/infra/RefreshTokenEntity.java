package com.taja.member.infra;

import com.taja.member.domain.RefreshToken;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "refresh_tokens")
@RequiredArgsConstructor
public class RefreshTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;

    @Column(name = "token_key", nullable = false, unique = true)
    private String key;

    @Column(name = "token_value", nullable = false, unique = true)
    private String value;

    @Builder
    private RefreshTokenEntity(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public static RefreshTokenEntity fromRefreshToken(RefreshToken refreshToken) {
        return RefreshTokenEntity.builder()
                .key(refreshToken.getKey())
                .value(refreshToken.getValue())
                .build();
    }

    public RefreshToken toRefreshToken() {
        return RefreshToken.builder()
                .refreshTokenId(refreshTokenId)
                .key(key)
                .value(value)
                .build();
    }
}
