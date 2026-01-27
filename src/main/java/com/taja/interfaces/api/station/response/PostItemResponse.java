package com.taja.interfaces.api.station.response;

import com.taja.application.board.BoardInfo.PostItem;
import java.time.LocalDateTime;
import java.util.List;

public record PostItemResponse(
        Long stationId,
        Long postId,
        String writer,
        LocalDateTime createdAt,
        String content,
        int commentCount,
        int likeCount
) {
    public static List<PostItemResponse> from(List<PostItem> postItems) {
        return postItems.stream()
                .map(postItem -> new PostItemResponse(
                        postItem.stationId(),
                        postItem.postId(),
                        postItem.writer(),
                        postItem.createdAt(),
                        postItem.content(),
                        postItem.commentCount(),
                        postItem.likeCount()
                ))
                .toList();
    }
}
