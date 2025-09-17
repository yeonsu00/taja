package com.taja.member.application;

import com.taja.member.domain.Member;

public interface MemberRepository {
    Member findByEmail(String email);

    void save(Member member);
}
