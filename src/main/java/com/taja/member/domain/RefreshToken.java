package com.taja.member.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshToken {

    private Long refreshTokenId;

    private String key;

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
}
