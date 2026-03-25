package com.taja.application.member;

import com.taja.domain.member.Member;
import java.util.List;

public interface MemberRepository {
    Member findByEmail(String email);

    void save(Member member);

    boolean existsByName(String name);

    void deleteByEmail(String email);

    List<Member> findByEmailStartingWith(String prefix);

    void deleteAllByIdIn(List<Long> memberIds);
}
