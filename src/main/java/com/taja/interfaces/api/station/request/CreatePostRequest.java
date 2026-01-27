package com.taja.interfaces.api.station.request;

import jakarta.validation.constraints.NotBlank;

public record CreatePostRequest(
        @NotBlank(message = "게시글 내용이 비어있습니다.")
        String content
) {
}
