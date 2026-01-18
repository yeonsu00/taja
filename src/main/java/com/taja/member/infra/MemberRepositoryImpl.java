package com.taja.member.infra;

import com.taja.global.exception.DuplicateMemberException;
import com.taja.global.exception.MemberException;
import com.taja.member.application.MemberRepository;
import com.taja.member.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member findByEmail(String email) {
        return memberJpaRepository.findByEmail(email)
                .orElseThrow(() -> new MemberException(email + " 사용자를 찾을 수 없습니다."));
    }

    @Override
    public void save(Member member) {
        try {
            memberJpaRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateMemberException("이미 회원가입된 사용자입니다.");
        }
    }

    @Override
    public boolean existsByName(String name) {
        return memberJpaRepository.existsByName(name);
    }
}
