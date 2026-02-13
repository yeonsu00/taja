package com.taja.interfaces.api.board;

import com.taja.application.board.BoardFacade;
import com.taja.application.board.BoardInfo;
import com.taja.global.response.CommonApiResponse;
import com.taja.infrastructure.jwt.CustomUserDetails;
import com.taja.interfaces.api.board.request.CreateCommentRequest;
import com.taja.interfaces.api.board.response.DailyRankPostResponse;
import com.taja.interfaces.api.station.response.PostDetailResponse;
import com.taja.interfaces.api.station.response.PostLikeResponse;
import java.time.LocalDate;
import java.util.List;
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
public class BoardController {

    private final BoardFacade boardFacade;

    @Operation(summary = "전체 게시글 일간 랭킹 조회", description = "전체 게시글 중 일간 인기순 Top 10 랭킹을 조회합니다. 액세스 토큰은 선택입니다.")
    @GetMapping("/rank/daily")
    public CommonApiResponse<DailyRankPostResponse.ListResponse> getDailyRankedPosts(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails != null ? customUserDetails.getUsername() : null;
        List<DailyRankPostResponse.Item> posts = DailyRankPostResponse.Item.from(boardFacade.findDailyRankedPosts(email, LocalDate.now()));
        return CommonApiResponse.success(DailyRankPostResponse.ListResponse.from(posts), "일간 랭킹 조회에 성공했습니다.");
    }

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
    @DeleteMapping("/comments/{commentId}")
    public CommonApiResponse<String> deleteComment(
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        boardFacade.deleteComment(email, commentId);
        return CommonApiResponse.success("댓글을 삭제했습니다.");
    }

    @Operation(summary = "게시글 좋아요 등록", description = "게시글에 좋아요를 등록합니다.")
    @PostMapping("/{postId}/like")
    public CommonApiResponse<PostLikeResponse> likePost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        BoardInfo.LikeResult result = boardFacade.likePost(email, postId);
        PostLikeResponse response = new PostLikeResponse(result.postId(), result.likeCount());
        return CommonApiResponse.success(response, "해당 게시글에 좋아요를 등록했습니다.");
    }

    @Operation(summary = "게시글 좋아요 취소", description = "게시글 좋아요를 취소합니다.")
    @DeleteMapping("/{postId}/like")
    public CommonApiResponse<PostLikeResponse> unlikePost(
            @PathVariable("postId") Long postId,
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        BoardInfo.LikeResult result = boardFacade.unlikePost(email, postId);
        PostLikeResponse response = new PostLikeResponse(result.postId(), result.likeCount());
        return CommonApiResponse.success(response, "해당 게시글에 좋아요를 취소했습니다.");
    }
}
