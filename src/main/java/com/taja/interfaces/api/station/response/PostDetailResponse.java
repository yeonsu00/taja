package com.taja.interfaces.api.station.response;

import com.taja.application.board.BoardInfo.PostDetail;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        String writer,
        LocalDateTime createdAt,
        String content,
        int likeCount,
        int commentCount,
        List<CommentItemResponse> comments
) {
    public static PostDetailResponse from(PostDetail detail) {
        return new PostDetailResponse(
                detail.postId(),
                detail.writer(),
                detail.createdAt(),
                detail.content(),
                detail.likeCount(),
                detail.commentCount(),
                CommentItemResponse.from(detail.comments())
        );
    }
}
