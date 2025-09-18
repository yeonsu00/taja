package com.taja.member.infra;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCodeJpaRepository extends JpaRepository<EmailCodeEntity, Long> {
    Optional<EmailCodeEntity> findByEmail(String email);

    Optional<EmailCodeEntity> findByEmailAndCode(String email, String code);
}
