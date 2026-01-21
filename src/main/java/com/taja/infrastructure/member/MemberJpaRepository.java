package com.taja.infrastructure.member;

import com.taja.domain.member.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String name);

    boolean existsByName(String name);
}
