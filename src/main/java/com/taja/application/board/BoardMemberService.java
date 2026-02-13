package com.taja.application.board;

import com.taja.domain.board.BoardMember;
import com.taja.global.exception.AlreadyJoinedException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardMemberService {

    private final BoardMemberRepository boardMemberRepository;

    public void joinBoard(BoardMember boardMember) {
        if (boardMemberRepository.existsByStationIdAndMemberId(boardMember.getStationId(), boardMember.getMemberId())) {
            throw new AlreadyJoinedException("이미 참여한 게시판입니다.");
        }

        boardMemberRepository.saveBoardMember(boardMember);
    }

    public List<BoardMember> findByMemberId(Long memberId) {
        return boardMemberRepository.findByMemberId(memberId);
    }
}
