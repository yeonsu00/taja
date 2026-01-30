package com.taja.application.board;

public final class PostRankingEvent {

    public record Created(
            long stationId,
            long postId
    ) {
        public static Created from(Long stationId, Long postId) {
            return new Created(stationId, postId);
        }
    }

    public record Liked(
            long stationId,
            long postId
    ) {
        public static Liked from(Long stationId, Long postId) {
            return new Liked(stationId, postId);
        }
    }

    public record Unliked(
            long stationId,
            long postId
    ) {
        public static Unliked from(Long stationId, Long postId) {
            return new Unliked(stationId, postId);
        }
    }

    public record Viewed(
            long stationId,
            long postId
    ) {
        public static Viewed from(Long stationId, Long postId) {
            return new Viewed(stationId, postId);
        }
    }

    public record CommentCreated(
            long stationId,
            long postId
    ) {
        public static CommentCreated from(Long stationId, Long postId) {
            return new CommentCreated(stationId, postId);
        }
    }

    public record CommentDeleted(
            long stationId,
            long postId
    ) {
        public static CommentDeleted from(Long stationId, Long postId) {
            return new CommentDeleted(stationId, postId);
        }
    }
}
