package com.taja.application.board;

import com.taja.domain.board.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

    void savePost(Post post);

    List<BoardInfo.PostItem> findLatestPosts(Long stationId, long cursor, int size);

    List<BoardInfo.PostItem> findRecentPosts(Long stationId, int recentPostsSize);

    Optional<String> findLatestPostContentByStationId(Long stationId);

    List<BoardInfo.PostItem> findPostItemsByStationIdAndPostIds(Long stationId, List<Long> postIds);

    List<BoardInfo.PostItem> findPostItemsByStationIdAndPostIds(List<Long> postIds);

    Optional<BoardInfo.PostDetailPart> findPostDetailPartByPostId(Long postId);

    List<BoardInfo.CommentItem> findCommentItemsByPostId(Long postId);

    Optional<Post> findPostByPostIdAndMemberId(Long postId, Long memberId);

    Optional<Post> findPostById(Long postId);

    int increaseLikeCount(Long postId);

    int decreaseLikeCount(Long postId);

    int increaseCommentCount(Long postId);

    int decreaseCommentCount(Long postId);
}
