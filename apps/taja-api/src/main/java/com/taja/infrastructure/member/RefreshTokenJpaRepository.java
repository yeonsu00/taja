package com.taja.infrastructure.member;

import com.taja.domain.member.RefreshToken;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByValue(String value);

    long deleteByValue(String value);

    Optional<RefreshToken> findByKey(String key);

    void deleteByKey(String key);

    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.key IN :keys")
    void deleteByKeyIn(@Param("keys") List<String> keys);
}
