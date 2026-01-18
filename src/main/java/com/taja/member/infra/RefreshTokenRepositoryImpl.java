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
        refreshTokenJpaRepository.findByKey(refreshToken.getKey())
                .ifPresentOrElse(existing -> {
                    existing.updateValue(refreshToken.getValue());
                }, () -> {
                    refreshTokenJpaRepository.save(refreshToken);
                });
    }

    @Override
    public RefreshToken findByValue(String value) {
        return refreshTokenJpaRepository.findByValue(value)
                .orElseThrow(() -> new TokenException("리프레시 토큰을 찾을 수 없습니다."));
    }

    @Override
    public void deleteByValue(String value) {
        long deletedCount = refreshTokenJpaRepository.deleteByValue(value);

        if (deletedCount == 0) {
            throw new TokenException("삭제할 리프레시 토큰을 찾을 수 없습니다.");
        }
    }
}
