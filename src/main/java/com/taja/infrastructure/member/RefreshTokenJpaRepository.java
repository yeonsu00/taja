package com.taja.infrastructure.member;

import com.taja.domain.member.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByValue(String value);

    long deleteByValue(String value);

    Optional<RefreshToken> findByKey(String key);
}
