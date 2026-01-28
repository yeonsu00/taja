package com.taja.application.board;

import com.taja.domain.board.Post;
import java.util.List;
import java.util.Optional;

public interface PostRepository {

    void savePost(Post post);

    List<BoardInfo.PostItem> findLatestPosts(Long stationId, Long cursor, int size);

    Optional<BoardInfo.PostDetailPart> findPostDetailPartByPostId(Long postId);

    List<BoardInfo.CommentItem> findCommentItemsByPostId(Long postId);

    Optional<Post> findPostByPostIdAndMemberId(Long postId, Long memberId);

    Optional<Post> findPostById(Long postId);
}
