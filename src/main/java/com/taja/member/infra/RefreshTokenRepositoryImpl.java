package com.taja.member.infra;

import com.taja.global.exception.TokenException;
import com.taja.member.application.RefreshTokenRepository;
import com.taja.member.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository refreshTokenJpaRepository;

    @Override
    public void save(RefreshToken refreshToken) {
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.fromRefreshToken(refreshToken);
        refreshTokenJpaRepository.save(refreshTokenEntity);
    }

    @Override
    public RefreshToken findByValue(String value) {
        RefreshTokenEntity refreshTokenEntity = refreshTokenJpaRepository.findByValue(value)
                .orElseThrow(() -> new TokenException("리프레시 토큰을 찾을 수 없습니다."));
        return refreshTokenEntity.toRefreshToken();
    }
}
