package com.taja.domain.board;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        name = "post_likes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_post_like_member", columnNames = {"post_id", "member_id"})
        },
        indexes = {
                @Index(name = "idx_post_like_member_post_deleted", columnList = "member_id, post_id, is_deleted")
        }
)
@Getter
@RequiredArgsConstructor
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postLikeId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder
    private PostLike(Long postLikeId, Long postId, Long memberId, boolean isDeleted) {
        this.postLikeId = postLikeId;
        this.postId = postId;
        this.memberId = memberId;
        this.isDeleted = isDeleted;
    }

    public static PostLike of(Long postId, Long memberId) {
        return PostLike.builder()
                .postId(postId)
                .memberId(memberId)
                .isDeleted(false)
                .build();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
