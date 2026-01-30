package com.taja.domain.board;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "comments")
@Getter
@RequiredArgsConstructor
public class Comment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(name = "post_id", nullable = false)
    private Long postId;

    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder
    private Comment(Long commentId, Long postId, Long writerId, String content, boolean isDeleted) {
        this.commentId = commentId;
        this.postId = postId;
        this.writerId = writerId;
        this.content = content;
        this.isDeleted = isDeleted;
    }

    public static Comment of(Long postId, Long writerId, String content) {
        return Comment.builder()
                .postId(postId)
                .writerId(writerId)
                .content(content)
                .isDeleted(false)
                .build();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }
}
