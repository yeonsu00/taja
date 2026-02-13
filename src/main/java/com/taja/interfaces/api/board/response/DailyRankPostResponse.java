package com.taja.interfaces.api.board.response;

import com.taja.application.board.BoardInfo.DailyRankPostItem;
import java.time.LocalDateTime;
import java.util.List;

public final class DailyRankPostResponse {

    public record Item(
            Long stationId,
            String stationName,
            int rank,
            Long postId,
            String writer,
            LocalDateTime createdAt,
            String content,
            int commentCount,
            int likeCount,
            boolean liked
    ) {
        public static Item from(DailyRankPostItem item) {
            return new Item(
                    item.stationId(),
                    item.stationName(),
                    item.rank(),
                    item.postId(),
                    item.writer(),
                    item.createdAt(),
                    item.content(),
                    item.commentCount(),
                    item.likeCount(),
                    item.liked()
            );
        }

        public static List<Item> from(List<DailyRankPostItem> items) {
            return items.stream().map(Item::from).toList();
        }
    }

    public record ListResponse(
            List<Item> posts
    ) {
        public static ListResponse from(List<Item> posts) {
            return new ListResponse(posts);
        }
    }
}
