package com.taja.infrastructure.board;

import com.taja.application.board.BoardMemberRepository;
import com.taja.domain.board.BoardMember;
import com.taja.global.exception.AlreadyJoinedException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class BoardMemberRepositoryImpl implements BoardMemberRepository {

    private final BoardMemberJpaRepository boardMemberJpaRepository;

    @Override
    public boolean existsByStationIdAndMemberId(Long stationId, Long memberId) {
        return boardMemberJpaRepository.existsByStationIdAndMemberId(stationId, memberId);
    }

    @Override
    public void saveBoardMember(BoardMember boardMember) {
        try {
            boardMemberJpaRepository.save(boardMember);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyJoinedException("이미 참여한 게시판입니다.");
        }
    }
}
