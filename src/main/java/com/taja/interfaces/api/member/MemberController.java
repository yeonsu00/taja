package com.taja.interfaces.api.member;

import com.taja.application.board.BoardFacade;
import com.taja.global.response.CommonApiResponse;
import com.taja.infrastructure.jwt.CustomUserDetails;
import com.taja.interfaces.api.member.response.JoinedBoardsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
@Tag(name = "Board", description = "Board API")
public class MemberController {

    private final BoardFacade boardFacade;

    @Operation(summary = "참여한 게시판 목록 조회", description = "로그인한 사용자가 참여한 게시판(대여소) 목록을 조회합니다.")
    @GetMapping("/boards")
    public CommonApiResponse<JoinedBoardsResponse> getJoinedBoards(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        JoinedBoardsResponse response = JoinedBoardsResponse.from(boardFacade.findJoinedBoards(email));
        return CommonApiResponse.success(response, "참여한 게시판 목록 조회에 성공했습니다.");
    }
}
