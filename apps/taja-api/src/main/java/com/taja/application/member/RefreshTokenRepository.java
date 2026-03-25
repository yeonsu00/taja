package com.taja.application.member;

import com.taja.domain.member.RefreshToken;
import java.util.List;

public interface RefreshTokenRepository {
    void save(RefreshToken refreshToken);

    RefreshToken findByValue(String value);

    void deleteByValue(String value);

    void deleteByKey(String key);

    void deleteByKeyIn(List<String> keys);
}
