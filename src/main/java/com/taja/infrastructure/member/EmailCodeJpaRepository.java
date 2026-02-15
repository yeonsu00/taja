package com.taja.infrastructure.member;

import com.taja.domain.member.EmailCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCodeJpaRepository extends JpaRepository<EmailCode, Long> {
    Optional<EmailCode> findByEmail(String email);

    Optional<EmailCode> findByEmailAndCode(String email, String code);
}
