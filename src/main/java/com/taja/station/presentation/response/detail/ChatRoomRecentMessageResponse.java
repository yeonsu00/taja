package com.taja.station.presentation.response.detail;

public record ChatRoomRecentMessageResponse(
        String nickname,
        String message,
        Boolean isReply,
        String replyToUserNickname,
        String replyToMessage
) {
}
