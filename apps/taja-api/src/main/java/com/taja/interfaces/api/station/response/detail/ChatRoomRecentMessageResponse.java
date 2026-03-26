package com.taja.interfaces.api.station.response.detail;

public record ChatRoomRecentMessageResponse(
        String memberName,
        String message,
        Boolean isReply,
        String replyToMemberName,
        String replyToMessage
) {
}
