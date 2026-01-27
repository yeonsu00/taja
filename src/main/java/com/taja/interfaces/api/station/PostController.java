package com.taja.interfaces.api.station;

import com.taja.application.board.BoardFacade;
import com.taja.global.response.CommonApiResponse;
import com.taja.infrastructure.jwt.CustomUserDetails;
import com.taja.interfaces.api.station.response.PostDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
@Tag(name = "Post", description = "Post API")
public class PostController {

    private final BoardFacade boardFacade;

    @Operation(summary = "게시글 상세 조회", description = "게시글 ID로 게시글 상세 정보를 조회합니다.")
    @GetMapping("/{postId}")
    public CommonApiResponse<PostDetailResponse> getPostDetail(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        PostDetailResponse response = PostDetailResponse.from(boardFacade.findPostDetail(email, postId));
        return CommonApiResponse.success(response, "게시글 상세 조회에 성공했습니다.");
    }
}
