package com.taja.station.presentation.response.detail;

public record ChatRoomRecentMessageResponse(
        String memberName,
        String message,
        Boolean isReply,
        String replyToMemberName,
        String replyToMessage
) {
}
