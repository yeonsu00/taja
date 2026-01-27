package com.taja.application.board;

import java.time.LocalDateTime;
import java.util.List;

public class BoardInfo {

    public record PostItem(
            Long stationId,
            Long postId,
            String writer,
            LocalDateTime createdAt,
            String content,
            int commentCount,
            int likeCount
    ) {
    }

    public record PostItems(
            List<PostItem> items,
            String nextCursor
    ) {
    }

    public record CommentItem(
            Long commentId,
            String writer,
            String content,
            LocalDateTime createdAt
    ) {
    }

    public record PostDetailPart(
            Long postId,
            Long stationId,
            String writer,
            LocalDateTime createdAt,
            String content,
            int likeCount,
            int commentCount
    ) {
    }

    public record PostDetail(
            Long postId,
            String writer,
            LocalDateTime createdAt,
            String content,
            int likeCount,
            int commentCount,
            List<CommentItem> comments
    ) {
    }
}
