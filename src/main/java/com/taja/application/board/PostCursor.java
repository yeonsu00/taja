package com.taja.application.board;

public final class PostCursor {

    private PostCursor() {
    }

    public static String encode(Long postId) {
        return postId == null ? null : String.valueOf(postId);
    }

    public static Long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(cursor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
