package com.taja.member.infra;

import com.taja.member.domain.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByValue(String value);

    long deleteByValue(String value);

    Optional<RefreshToken> findByKey(String key);
}
