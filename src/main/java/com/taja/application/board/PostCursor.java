package com.taja.application.board;

public final class PostCursor {

    private PostCursor() {
    }

    public static String encode(Long postId) {
        return postId == null ? null : String.valueOf(postId);
    }

    public static long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            return Math.max(0, Long.parseLong(cursor.trim()));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
