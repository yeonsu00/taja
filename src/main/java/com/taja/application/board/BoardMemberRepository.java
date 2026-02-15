package com.taja.application.board;

import com.taja.domain.board.BoardMember;
import java.util.List;

public interface BoardMemberRepository {

    boolean existsByStationIdAndMemberId(Long stationId, Long memberId);

    void saveBoardMember(BoardMember boardMember);

    List<BoardMember> findByMemberId(Long memberId);
}
