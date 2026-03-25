package com.taja.infrastructure.board;

import com.taja.domain.board.BoardMember;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardMemberJpaRepository extends JpaRepository<BoardMember, Long> {

    boolean existsByStationIdAndMemberId(Long stationId, Long memberId);

    List<BoardMember> findByMemberIdOrderByBoardMemberIdDesc(Long memberId);

    @Modifying
    @Query("DELETE FROM BoardMember bm WHERE bm.memberId IN :memberIds")
    void deleteByMemberIdIn(@Param("memberIds") List<Long> memberIds);
}
