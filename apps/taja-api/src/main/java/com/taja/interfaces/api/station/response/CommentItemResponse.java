package com.taja.interfaces.api.station.response;

import com.taja.application.board.BoardInfo.CommentItem;
import java.time.LocalDateTime;
import java.util.List;

public record CommentItemResponse(
        Long commentId,
        String writer,
        String content,
        LocalDateTime createdAt
) {
    public static List<CommentItemResponse> from(List<CommentItem> items) {
        return items.stream()
                .map(item -> new CommentItemResponse(
                        item.commentId(),
                        item.writer(),
                        item.content(),
                        item.createdAt()
                ))
                .toList();
    }
}
