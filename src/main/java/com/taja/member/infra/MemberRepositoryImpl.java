package com.taja.member.infra;

import com.taja.global.exception.MemberException;
import com.taja.member.application.MemberRepository;
import com.taja.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member findByEmail(String email) {
        MemberEntity memberEntity = memberJpaRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(email + " 사용자를 찾을 수 없습니다."));
        return memberEntity.toMember();
    }

    @Override
    public void save(Member member) {
        MemberEntity memberEntity = MemberEntity.fromMember(member);
        memberJpaRepository.save(memberEntity);
    }

    @Override
    public boolean existsByName(String name) {
        return memberJpaRepository.existsByName(name);
    }
}
