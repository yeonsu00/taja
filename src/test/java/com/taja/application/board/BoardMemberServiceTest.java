package com.taja.application.board;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taja.domain.board.BoardMember;
import com.taja.global.exception.AlreadyJoinedException;
import com.taja.global.exception.NotStationMemberException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("BoardMemberService")
class BoardMemberServiceTest {

    @MockitoBean
    private BoardMemberRepository boardMemberRepository;

    @Autowired
    private BoardMemberService boardMemberService;

    @Nested
    @DisplayName("게시판 참여")
    class JoinBoard {

        @Test
        @DisplayName("게시판 참여에 성공한다")
        void success() {
            BoardMember boardMember = BoardMember.of(1L, 1L);
            when(boardMemberRepository.existsByStationIdAndMemberId(1L, 1L)).thenReturn(false);

            boardMemberService.joinBoard(boardMember);

            verify(boardMemberRepository).saveBoardMember(boardMember);
        }

        @Test
        @DisplayName("이미 참여한 게시판이면 AlreadyJoinedException 발생")
        void alreadyJoined_throwsAlreadyJoinedException() {
            BoardMember boardMember = BoardMember.of(1L, 1L);
            when(boardMemberRepository.existsByStationIdAndMemberId(1L, 1L)).thenReturn(true);

            assertThatThrownBy(() -> boardMemberService.joinBoard(boardMember))
                    .isInstanceOf(AlreadyJoinedException.class)
                    .hasMessageContaining("이미 참여한 게시판");

            verify(boardMemberRepository, never()).saveBoardMember(boardMember);
        }
    }

    @Nested
    @DisplayName("참여한 멤버인지 확인")
    class CheckMemberJoined {

        @Test
        @DisplayName("참여한 멤버면 예외 없이 통과한다")
        void joined_success() {
            when(boardMemberRepository.existsByStationIdAndMemberId(1L, 1L)).thenReturn(true);

            boardMemberService.checkMemberJoined(1L, 1L);
        }

        @Test
        @DisplayName("참여하지 않은 멤버면 NotStationMemberException 발생")
        void notJoined_throwsNotStationMemberException() {
            when(boardMemberRepository.existsByStationIdAndMemberId(1L, 1L)).thenReturn(false);

            assertThatThrownBy(() -> boardMemberService.checkMemberJoined(1L, 1L))
                    .isInstanceOf(NotStationMemberException.class)
                    .hasMessageContaining("참여자가 아닙니다");
        }
    }
}
