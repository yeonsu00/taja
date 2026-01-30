package com.taja.application.board;

import com.taja.application.member.AuthService;
import com.taja.application.station.StationService;
import com.taja.domain.board.BoardMember;
import com.taja.domain.board.Comment;
import com.taja.domain.board.Post;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class BoardFacade {

    private final BoardMemberService boardMemberService;
    private final AuthService authService;
    private final StationService stationService;
    private final PostService postService;
    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void join(String email, Long stationId) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);

        BoardMember boardMember = BoardMember.of(station.getStationId(), member.getMemberId());
        boardMemberService.joinBoard(boardMember);
    }

    @Transactional
    public void createPost(String email, Long stationId, String content) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);
        boardMemberService.checkMemberJoined(station.getStationId(), member.getMemberId());
        Post post = postService.createPost(member.getMemberId(), station.getStationId(), content);
        eventPublisher.publishEvent(PostRankingEvent.Created.from(post.getStationId(), post.getPostId()));
    }

    @Transactional(readOnly = true)
    public BoardInfo.PostItems findLatestPosts(String email, Long stationId, String cursor, int size) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);

        boardMemberService.checkMemberJoined(station.getStationId(), member.getMemberId());
        BoardInfo.PostItems postItems = postService.findLatestPosts(station.getStationId(), cursor, size);

        List<BoardInfo.PostItem> itemsWithLiked = fillLikedForMember(postItems.items(), member.getMemberId());
        return BoardInfo.PostItems.from(itemsWithLiked, postItems.nextCursor());
    }

    @Transactional(readOnly = true)
    public BoardInfo.PostItems findPopularPosts(String email, Long stationId, String cursor, int size, LocalDate today) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);
        boardMemberService.checkMemberJoined(station.getStationId(), member.getMemberId());

        boolean isFirstPage = cursor == null || cursor.isBlank();
        BoardInfo.PostItems postItems = postService.findPopularPosts(station.getStationId(), cursor, size, today);

        if (isFirstPage && postItems.items().isEmpty()) {
            postItems = postService.findLatestPosts(station.getStationId(), cursor, size);
        }

        List<BoardInfo.PostItem> itemsWithLiked = fillLikedForMember(postItems.items(), member.getMemberId());
        return BoardInfo.PostItems.from(itemsWithLiked, postItems.nextCursor());
    }

    @Transactional(readOnly = true)
    public BoardInfo.PostDetail findPostDetail(String email, Long postId) {
        Member member = authService.findMemberByEmail(email);

        BoardInfo.PostDetailPart postDetailPart = postService.findPostDetailPart(postId);
        boardMemberService.checkMemberJoined(postDetailPart.stationId(), member.getMemberId());
        BoardInfo.PostDetail detail = postService.enrichWithComments(postDetailPart);
        boolean liked = postLikeService.hasLiked(postId, member.getMemberId());
        BoardInfo.PostDetail detailWithLiked = BoardInfo.PostDetail.from(detail, liked);

        eventPublisher.publishEvent(PostRankingEvent.Viewed.from(postDetailPart.stationId(), postDetailPart.postId()));
        return detailWithLiked;
    }

    @Transactional
    public void deletePost(String email, Long postId) {
        Member member = authService.findMemberByEmail(email);

        Post post = postService.findPostByPostAndMember(member.getMemberId(), postId);
        boardMemberService.checkMemberJoined(post.getStationId(), member.getMemberId());

        postService.softDeletePost(post);
        commentService.softDeleteComments(postId);
        postLikeService.softDeletePostLikes(postId);
    }

    @Transactional
    public void createComment(String email, Long postId, String content) {
        Member member = authService.findMemberByEmail(email);

        Post post = postService.findPostByPostId(postId);
        boardMemberService.checkMemberJoined(post.getStationId(), member.getMemberId());

        commentService.createComment(member.getMemberId(), post.getPostId(), content);
        postService.incrementCommentCount(postId);

        eventPublisher.publishEvent(PostRankingEvent.CommentCreated.from(post.getStationId(), post.getPostId()));
    }

    @Transactional
    public void deleteComment(String email, Long commentId) {
        Member member = authService.findMemberByEmail(email);

        Comment comment = commentService.findCommentByCommentId(commentId, member.getMemberId());
        Post post = postService.findPostByPostId(comment.getPostId());
        boardMemberService.checkMemberJoined(post.getStationId(), member.getMemberId());

        commentService.softDeleteComment(comment);
        postService.decrementCommentCount(comment.getPostId());

        eventPublisher.publishEvent(PostRankingEvent.CommentDeleted.from(post.getStationId(), comment.getPostId()));
    }

    @Transactional
    public BoardInfo.LikeResult likePost(String email, Long postId) {
        Member member = authService.findMemberByEmail(email);

        Post post = postService.findPostByPostId(postId);
        boardMemberService.checkMemberJoined(post.getStationId(), member.getMemberId());

        postLikeService.likePost(member.getMemberId(), postId);
        postService.incrementLikeCount(postId);

        Post updated = postService.findPostByPostId(postId);

        eventPublisher.publishEvent(PostRankingEvent.Liked.from(post.getStationId(), postId));
        return BoardInfo.LikeResult.from(postId, updated.getLikeCount());
    }

    @Transactional
    public BoardInfo.LikeResult unlikePost(String email, Long postId) {
        Member member = authService.findMemberByEmail(email);

        Post post = postService.findPostByPostId(postId);
        boardMemberService.checkMemberJoined(post.getStationId(), member.getMemberId());

        postLikeService.unlikePost(member.getMemberId(), postId);
        postService.decrementLikeCount(postId);

        Post updated = postService.findPostByPostId(postId);

        eventPublisher.publishEvent(PostRankingEvent.Unliked.from(post.getStationId(), postId));
        return BoardInfo.LikeResult.from(postId, updated.getLikeCount());
    }

    private List<BoardInfo.PostItem> fillLikedForMember(List<BoardInfo.PostItem> items, Long memberId) {
        if (items.isEmpty()) {
            return items;
        }
        List<Long> postIds = items.stream().map(BoardInfo.PostItem::postId).toList();
        Set<Long> likedPostIds = postLikeService.findLikedPostIdsByMemberIdAndPostIdIn(memberId, postIds);
        return items.stream()
                .map(item -> BoardInfo.PostItem.from(item, likedPostIds.contains(item.postId())))
                .toList();
    }
}
