package com.taja.interfaces.api.station.response;

import java.util.List;

public record PostListResponse(
        List<PostItemResponse> posts,
        String nextCursor
) {
}
