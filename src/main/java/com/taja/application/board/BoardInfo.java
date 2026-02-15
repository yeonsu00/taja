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
            int likeCount,
            boolean liked
    ) {
        public static PostItem from(PostItem item, boolean liked) {
            return new PostItem(
                    item.stationId(),
                    item.postId(),
                    item.writer(),
                    item.createdAt(),
                    item.content(),
                    item.commentCount(),
                    item.likeCount(),
                    liked
            );
        }
    }

    public record PostItems(
            List<PostItem> items,
            String nextCursor
    ) {
        public static PostItems from(List<PostItem> items, String nextCursor) {
            return new PostItems(items, nextCursor);
        }
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
            List<CommentItem> comments,
            boolean liked
    ) {
        public static PostDetail from(PostDetail postDetail, boolean liked) {
            return new PostDetail(
                    postDetail.postId(),
                    postDetail.writer(),
                    postDetail.createdAt(),
                    postDetail.content(),
                    postDetail.likeCount(),
                    postDetail.commentCount(),
                    postDetail.comments(),
                    liked
            );
        }

        public static PostDetail from(PostDetailPart part, List<CommentItem> comments, boolean liked) {
            return new PostDetail(
                    part.postId(),
                    part.writer(),
                    part.createdAt(),
                    part.content(),
                    part.likeCount(),
                    part.commentCount(),
                    comments,
                    liked
            );
        }
    }

    public record LikeResult(
            Long postId,
            int likeCount
    ) {
        public static LikeResult from(Long postId, int likeCount) {
            return new LikeResult(postId, likeCount);
        }
    }

    public record JoinedBoardItem(
            Long stationId,
            String name,
            String lastContent
    ) {
    }

    public record JoinedBoards(
            List<JoinedBoardItem> items
    ) {
        public static JoinedBoards from(List<JoinedBoardItem> items) {
            return new JoinedBoards(items);
        }
    }

    public record DailyRankPostItem(
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
        public static DailyRankPostItem from(PostItem item, String stationName, int rank) {
            return new DailyRankPostItem(
                    item.stationId(),
                    stationName,
                    rank,
                    item.postId(),
                    item.writer(),
                    item.createdAt(),
                    item.content(),
                    item.commentCount(),
                    item.likeCount(),
                    item.liked()
            );
        }
    }
}
