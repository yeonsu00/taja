package com.taja.interfaces.api.board.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
        @NotBlank(message = "댓글 내용을 입력해주세요.")
        String content
) {
}
