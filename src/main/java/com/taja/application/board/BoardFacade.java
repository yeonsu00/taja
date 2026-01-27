package com.taja.application.board;

import com.taja.application.member.AuthService;
import com.taja.application.station.StationService;
import com.taja.domain.board.BoardMember;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class BoardFacade {

    private final BoardMemberService boardMemberService;
    private final AuthService authService;
    private final StationService stationService;

    @Transactional
    public void join(String email, Long stationId) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);

        BoardMember boardMember = BoardMember.of(station.getStationId(), member.getMemberId());
        boardMemberService.joinBoard(boardMember);
    }

}
