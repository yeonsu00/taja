package com.taja.application.member;

import com.taja.domain.member.Member;

public interface MemberRepository {
    Member findByEmail(String email);

    void save(Member member);

    boolean existsByName(String name);

    void deleteByEmail(String email);
}
