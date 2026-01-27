package com.taja.infrastructure.board;

import com.taja.domain.board.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardMemberJpaRepository extends JpaRepository<BoardMember, Long> {

    boolean existsByStationIdAndMemberId(Long stationId, Long memberId);
}
