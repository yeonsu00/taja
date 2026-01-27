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
@Table(name = "posts")
@Getter
@RequiredArgsConstructor
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postId;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int likeCount = 0;

    @Column(nullable = false)
    private int commentCount = 0;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Builder
    private Post(Long postId, Long stationId, Long writerId, String content, int likeCount, int commentCount, boolean isDeleted) {
        this.postId = postId;
        this.stationId = stationId;
        this.writerId = writerId;
        this.content = content;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.isDeleted = isDeleted;
    }

    public static Post of(Long stationId, Long writerId, String content) {
        return Post.builder()
                .stationId(stationId)
                .writerId(writerId)
                .content(content)
                .likeCount(0)
                .commentCount(0)
                .isDeleted(false)
                .build();
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
