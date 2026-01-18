package com.taja.member.domain;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "members")
@RequiredArgsConstructor
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    private Member(Long memberId, String name, String email, String password, Role role) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static Member of(String name, String email, String password) {
        return Member.builder()
                .name(name)
                .email(email)
                .password(password)
                .role(Role.MEMBER)
                .build();
    }
}
