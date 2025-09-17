package com.taja.member.application;

import com.taja.member.domain.RefreshToken;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);

    RefreshToken findByValue(String value);
}
