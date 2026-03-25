package com.taja.application.board;

import com.taja.application.board.BoardInfo.PostItem;
import com.taja.application.board.BoardInfo.PostItems;
import com.taja.application.member.AuthService;
import com.taja.application.station.StationService;
import com.taja.domain.board.BoardMember;
import com.taja.domain.board.Comment;
import com.taja.domain.board.Post;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import com.taja.interfaces.api.station.response.PostCreatedResponse;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Transactional(readOnly = true)
    public BoardInfo.JoinedBoards findJoinedBoards(String email) {
        Member member = authService.findMemberByEmail(email);
        List<BoardMember> boardMembers = boardMemberService.findByMemberId(member.getMemberId());
        if (boardMembers.isEmpty()) {
            return BoardInfo.JoinedBoards.from(List.of());
        }
        List<Long> stationIds = boardMembers.stream().map(BoardMember::getStationId).toList();
        Map<Long, Station> stationMap = stationService.findStationMapByIds(stationIds);
        List<BoardInfo.JoinedBoardItem> items = boardMembers.stream()
                .map(bm -> {
                    Station station = stationMap.get(bm.getStationId());
                    String name = station != null ? station.getName() : "";
                    String lastContent = postService.findLatestPostContentByStationId(bm.getStationId()).orElse(null);
                    return new BoardInfo.JoinedBoardItem(bm.getStationId(), name, lastContent);
                })
                .toList();
        return BoardInfo.JoinedBoards.from(items);
    }

    @Transactional
    public PostCreatedResponse createPost(String email, Long stationId, String content) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);
        Post post = postService.createPost(member.getMemberId(), station.getStationId(), content);
        eventPublisher.publishEvent(PostRankingEvent.Created.from(post.getStationId(), post.getPostId()));
        return new PostCreatedResponse(post.getPostId());
    }

    @Transactional(readOnly = true)
    public BoardInfo.PostItems findLatestPosts(String email, Long stationId, String cursor, int size) {
        Station station = stationService.findStationByStationId(stationId);
        BoardInfo.PostItems postItems = postService.findLatestPosts(station.getStationId(), cursor, size);

        return getPostItems(email, postItems);
    }

    @Transactional(readOnly = true)
    public BoardInfo.PostItems findPopularPosts(String email, Long stationId, String cursor, int size, LocalDate today) {
        Station station = stationService.findStationByStationId(stationId);
        boolean isFirstPage = cursor == null || cursor.isBlank();
        BoardInfo.PostItems postItems = postService.findPopularPosts(station.getStationId(), cursor, size, today);

        if (isFirstPage && postItems.items().isEmpty()) {
            postItems = postService.findLatestPosts(station.getStationId(), cursor, size);
        }

        return getPostItems(email, postItems);
    }

    @NotNull
    private BoardInfo.PostItems getPostItems(String email, PostItems postItems) {
        List<PostItem> itemsWithLiked;
        if (isLoggedIn(email)) {
            Long memberId = authService.findMemberByEmail(email).getMemberId();
            itemsWithLiked = fillLikedForMember(postItems.items(), memberId);
        } else {
            itemsWithLiked = postItems.items();
        }

        return PostItems.from(itemsWithLiked, postItems.nextCursor());
    }

    @Transactional(readOnly = true)
    public BoardInfo.PostDetail findPostDetail(String email, Long postId) {
        BoardInfo.PostDetailPart postDetailPart = postService.findPostDetailPart(postId);
        BoardInfo.PostDetail detail = postService.enrichWithComments(postDetailPart);

        boolean liked = false;
        if (isLoggedIn(email)) {
            Member member = authService.findMemberByEmail(email);
            liked = postLikeService.hasLiked(postId, member.getMemberId());
        }

        BoardInfo.PostDetail detailWithLiked = BoardInfo.PostDetail.from(detail, liked);
        eventPublisher.publishEvent(PostRankingEvent.Viewed.from(postDetailPart.stationId(), postDetailPart.postId()));
        return detailWithLiked;
    }

    @Transactional
    public void deletePost(String email, Long postId) {
        Member member = authService.findMemberByEmail(email);

        Post post = postService.findPostByPostAndMember(member.getMemberId(), postId);

        postService.softDeletePost(post);
        commentService.softDeleteComments(postId);
        postLikeService.softDeletePostLikes(postId);
    }

    @Transactional
    public void createComment(String email, Long postId, String content) {
        Member member = authService.findMemberByEmail(email);

        Post post = postService.findPostByPostId(postId);

        commentService.createComment(member.getMemberId(), post.getPostId(), content);
        postService.incrementCommentCount(postId);

        eventPublisher.publishEvent(PostRankingEvent.CommentCreated.from(post.getStationId(), post.getPostId()));
    }

    @Transactional
    public void deleteComment(String email, Long commentId) {
        Member member = authService.findMemberByEmail(email);

        Comment comment = commentService.findCommentByCommentId(commentId, member.getMemberId());
        Post post = postService.findPostByPostId(comment.getPostId());

        commentService.softDeleteComment(comment);
        postService.decrementCommentCount(comment.getPostId());

        eventPublisher.publishEvent(PostRankingEvent.CommentDeleted.from(post.getStationId(), comment.getPostId()));
    }

    @Transactional
    public BoardInfo.LikeResult likePost(String email, Long postId) {
        Member member = authService.findMemberByEmail(email);

        Post post = postService.findPostByPostId(postId);

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

        postLikeService.unlikePost(member.getMemberId(), postId);
        postService.decrementLikeCount(postId);

        Post updated = postService.findPostByPostId(postId);

        eventPublisher.publishEvent(PostRankingEvent.Unliked.from(post.getStationId(), postId));
        return BoardInfo.LikeResult.from(postId, updated.getLikeCount());
    }

    @Transactional(readOnly = true)
    public List<BoardInfo.DailyRankPostItem> findDailyRankedPosts(String email, LocalDate today) {
        List<Long> rankedPostIds = postService.findDailyRankedPostIds(today);
        if (rankedPostIds.isEmpty()) {
            return List.of();
        }

        List<BoardInfo.PostItem> postItems = postService.findPostItemsByPostIds(rankedPostIds);
        List<Long> stationIds = postItems.stream().map(BoardInfo.PostItem::stationId).distinct().toList();
        Map<Long, Station> stationMap = stationService.findStationMapByIds(stationIds);

        if (isLoggedIn(email)) {
            Member member = authService.findMemberByEmail(email);
            postItems = fillLikedForMember(postItems, member.getMemberId());
        }

        List<BoardInfo.DailyRankPostItem> dailyRankPostItems = new ArrayList<>(postItems.size());
        for (int i = 0; i < postItems.size(); i++) {
            BoardInfo.PostItem item = postItems.get(i);
            Station station = stationMap.get(item.stationId());
            String stationName = station != null ? station.getName() : "";

            dailyRankPostItems.add(BoardInfo.DailyRankPostItem.from(item, stationName, i + 1));
        }
        return dailyRankPostItems;
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

    private static boolean isLoggedIn(String email) {
        return email != null && !email.isBlank();
    }
}
