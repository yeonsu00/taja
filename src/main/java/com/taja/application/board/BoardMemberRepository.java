package com.taja.application.board;

import com.taja.domain.board.BoardMember;

public interface BoardMemberRepository {

    boolean existsByStationIdAndMemberId(Long stationId, Long memberId);

    void saveBoardMember(BoardMember boardMember);
}
