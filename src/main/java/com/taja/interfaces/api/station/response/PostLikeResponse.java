package com.taja.interfaces.api.station.response;

public record PostLikeResponse(
        Long postId,
        int likeCount
) {
}
