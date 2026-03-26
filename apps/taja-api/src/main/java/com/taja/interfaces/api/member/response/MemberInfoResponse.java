package com.taja.interfaces.api.member.response;

import com.taja.domain.member.Member;

public record MemberInfoResponse(Long memberId, String name, String email) {

    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(member.getMemberId(), member.getName(), member.getEmail());
    }
}
