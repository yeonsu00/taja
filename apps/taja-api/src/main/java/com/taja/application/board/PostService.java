package com.taja.application.board;

import com.taja.application.board.BoardInfo.PostItem;
import com.taja.domain.board.Post;
import com.taja.global.exception.PostNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private static final int DAILY_RANK_LIMIT = 10;

    private final PostRepository postRepository;
    private final PostRankingRepository postRankingRepository;
    private final AllPostRankingRepository allPostRankingRepository;
    private final MeterRegistry meterRegistry;

    public Post createPost(Long memberId, Long stationId, String content) {
        Post post = Post.of(stationId, memberId, content);
        postRepository.savePost(post);
        Counter.builder("post.created.total").register(meterRegistry).increment();
        return post;
    }

    public BoardInfo.PostDetailPart findPostDetailPart(Long postId) {
        return postRepository.findPostDetailPartByPostId(postId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
    }

    public BoardInfo.PostDetail enrichWithComments(BoardInfo.PostDetailPart part) {
        List<BoardInfo.CommentItem> comments = postRepository.findCommentItemsByPostId(part.postId());
        return BoardInfo.PostDetail.from(part, comments, false);
    }

    public BoardInfo.PostItems findLatestPosts(Long stationId, String cursor, int size) {
        long cursorPostId = PostCursor.decode(cursor);
        List<BoardInfo.PostItem> fetchedPostItems = postRepository.findLatestPosts(stationId, cursorPostId, size + 1);

        List<BoardInfo.PostItem> pagedPostItems = fetchedPostItems;
        String nextCursor = null;
        if (fetchedPostItems.size() > size) {
            pagedPostItems = fetchedPostItems.subList(0, size);
            nextCursor = PostCursor.encode(pagedPostItems.getLast().postId());
        }
        return BoardInfo.PostItems.from(pagedPostItems, nextCursor);
    }

    public BoardInfo.PostItems findPopularPosts(Long stationId, String cursor, int size, LocalDate today) {
        long offset = PostCursor.decode(cursor);
        List<Long> rankedPostIds = postRankingRepository.findRankedPostIds(stationId, offset, size + 1, today);
        if (rankedPostIds.isEmpty()) {
            return BoardInfo.PostItems.from(List.of(), null);
        }
        List<BoardInfo.PostItem> fetchedPostItems = postRepository.findPostItemsByStationIdAndPostIds(stationId, rankedPostIds);

        List<BoardInfo.PostItem> pagedPostItems = fetchedPostItems;
        String nextCursor = null;
        if (fetchedPostItems.size() > size) {
            pagedPostItems = fetchedPostItems.subList(0, size);
            nextCursor = PostCursor.encode(offset + size);
        }
        return BoardInfo.PostItems.from(pagedPostItems, nextCursor);
    }

    public Post findPostByPostAndMember(Long memberId, Long postId) {
        return postRepository.findPostByPostIdAndMemberId(postId, memberId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
    }

    public void softDeletePost(Post post) {
        post.markAsDeleted();
    }

    public Post findPostByPostId(Long postId) {
        return postRepository.findPostById(postId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
    }

    public void incrementCommentCount(Long postId) {
        int updated = postRepository.increaseCommentCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }

    public void decrementCommentCount(Long postId) {
        int updated = postRepository.decreaseCommentCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }

    public void incrementLikeCount(Long postId) {
        int updated = postRepository.increaseLikeCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }

    public void decrementLikeCount(Long postId) {
        int updated = postRepository.decreaseLikeCount(postId);
        if (updated == 0) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }

    public void addRankingScore(long stationId, long postId, double weightDelta, LocalDate today) {
        postRankingRepository.addScore(stationId, postId, weightDelta, today);
    }

    public void addAllPostRankingScore(long postId, double weightDelta, LocalDate today) {
        allPostRankingRepository.addScore(postId, weightDelta, today);
    }

    public List<Long> findDailyRankedPostIds(LocalDate today) {
        return allPostRankingRepository.findRankedPostIds(0, DAILY_RANK_LIMIT, today);
    }

    public List<PostItem> findPostItemsByPostIds(List<Long> postIds) {
        return postRepository.findPostItemsByStationIdAndPostIds(postIds);
    }

    public void updateTomorrowRankingScores(LocalDate today) {
        postRankingRepository.carryOverTodayToTomorrow(today);
        allPostRankingRepository.carryOverTodayToTomorrow(today);
    }

    public List<PostItem> findRecentPosts(Long stationId, int recentPostsSize) {
        return postRepository.findRecentPosts(stationId, recentPostsSize);
    }

    public Optional<String> findLatestPostContentByStationId(Long stationId) {
        return postRepository.findLatestPostContentByStationId(stationId);
    }
}
