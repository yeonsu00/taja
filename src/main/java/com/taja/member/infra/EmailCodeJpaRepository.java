package com.taja.member.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailCodeJpaRepository extends JpaRepository<EmailCodeEntity, Long> {
}
