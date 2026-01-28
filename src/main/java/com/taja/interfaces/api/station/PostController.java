package com.taja.interfaces.api.station;

import com.taja.application.board.BoardFacade;
import com.taja.global.response.CommonApiResponse;
import com.taja.infrastructure.jwt.CustomUserDetails;
import com.taja.interfaces.api.station.request.CreateCommentRequest;
import com.taja.interfaces.api.station.response.PostDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "게시글 삭제", description = "게시글 ID로 게시글을 삭제합니다.")
    @DeleteMapping("/{postId}")
    public CommonApiResponse<String> deletePost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        boardFacade.deletePost(email, postId);
        return CommonApiResponse.success("게시글 삭제에 성공했습니다.");
    }

    @Operation(summary = "댓글 작성", description = "게시글 ID로 댓글을 작성합니다.")
    @PostMapping("/{postId}/comments")
    public CommonApiResponse<String> createComment(
            @PathVariable("postId") Long postId,
            @RequestBody @Valid CreateCommentRequest request,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        boardFacade.createComment(email, postId, request.content());
        return CommonApiResponse.success("댓글을 작성했습니다.");
    }

    @Operation(summary = "댓글 삭제", description = "댓글 ID로 댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public CommonApiResponse<String> deleteComment(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        boardFacade.deleteComment(email, commentId);
        return CommonApiResponse.success("댓글을 삭제했습니다.");
    }
}
