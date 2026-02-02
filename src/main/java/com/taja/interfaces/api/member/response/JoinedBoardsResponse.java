package com.taja.interfaces.api.member.response;

import com.taja.application.board.BoardInfo.JoinedBoardItem;
import com.taja.application.board.BoardInfo.JoinedBoards;
import java.util.List;

public record JoinedBoardsResponse(
        List<JoinedBoardItemResponse> boards
) {
    public record JoinedBoardItemResponse(
            Long stationId,
            String name,
            String lastContent
    ) {
        public static JoinedBoardItemResponse from(JoinedBoardItem item) {
            return new JoinedBoardItemResponse(
                    item.stationId(),
                    item.name(),
                    item.lastContent()
            );
        }
    }

    public static JoinedBoardsResponse from(JoinedBoards joinedBoards) {
        List<JoinedBoardItemResponse> boards = joinedBoards.items().stream()
                .map(JoinedBoardItemResponse::from)
                .toList();
        return new JoinedBoardsResponse(boards);
    }
}
