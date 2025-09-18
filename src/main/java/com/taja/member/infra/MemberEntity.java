package com.taja.member.infra;

import com.taja.global.BaseEntity;
import com.taja.member.domain.Role;
import com.taja.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "members")
@RequiredArgsConstructor
public class MemberEntity extends BaseEntity {

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
    private MemberEntity(Long memberId, String name, String email, String password, Role role) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public static MemberEntity fromMember(Member member) {
        return MemberEntity.builder()
                .name(member.getName())
                .email(member.getEmail())
                .password(member.getPassword())
                .role(member.getRole())
                .build();
    }

    public Member toMember() {
        return Member.builder()
                .memberId(this.memberId)
                .name(this.name)
                .email(this.email)
                .password(this.password)
                .role(this.role)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
