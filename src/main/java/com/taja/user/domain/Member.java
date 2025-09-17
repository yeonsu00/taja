package com.taja.user.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Member {

    private Long memberId;

    private String name;

    private String email;

    private String password;

    private Role role;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Builder
    private Member(Long memberId, String name, String email, String password, Role role, LocalDateTime createdAt,
                   LocalDateTime updatedAt) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Member createMember(String name, String email, String password) {
        return Member.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(Role.USER)
                .build();
    }
}
