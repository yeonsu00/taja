package com.taja.infrastructure.board;

import com.taja.application.board.BoardInfo;
import com.taja.application.board.PostRepository;
import com.taja.domain.board.Post;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostRepositoryImpl implements PostRepository {

    private final PostJpaRepository postJpaRepository;
    private final PostQueryRepository postQueryRepository;

    @Override
    public void savePost(Post post) {
        postJpaRepository.save(post);
    }

    @Override
    public List<BoardInfo.PostItem> findLatestPosts(Long stationId, long cursor, int size) {
        return postQueryRepository.findLatestPosts(stationId, cursor, size);
    }

    @Override
    public List<BoardInfo.PostItem> findRecentPosts(Long stationId, int recentPostsSize) {
        return postQueryRepository.findRecentPosts(stationId, recentPostsSize);
    }

    @Override
    public Optional<String> findLatestPostContentByStationId(Long stationId) {
        return postQueryRepository.findLatestPostContentByStationId(stationId);
    }

    @Override
    public List<BoardInfo.PostItem> findPostItemsByStationIdAndPostIds(Long stationId, List<Long> postIds) {
        return postQueryRepository.findPostItemsByStationIdAndPostIds(stationId, postIds);
    }

    @Override
    public List<BoardInfo.PostItem> findPostItemsByStationIdAndPostIds(List<Long> postIds) {
        return postQueryRepository.findPostItemsByStationIdAndPostIds(postIds);
    }

    @Override
    public Optional<BoardInfo.PostDetailPart> findPostDetailPartByPostId(Long postId) {
        return postQueryRepository.findPostDetailPartByPostId(postId);
    }

    @Override
    public List<BoardInfo.CommentItem> findCommentItemsByPostId(Long postId) {
        return postQueryRepository.findCommentItemsByPostId(postId);
    }

    @Override
    public Optional<Post> findPostByPostIdAndMemberId(Long postId, Long memberId) {
        return postJpaRepository.findByPostIdAndWriterIdAndIsDeletedFalse(postId, memberId);
    }

    @Override
    public Optional<Post> findPostById(Long postId) {
        return postJpaRepository.findByPostIdAndIsDeletedFalse(postId);
    }

    @Override
    public int increaseLikeCount(Long postId) {
        return postJpaRepository.increaseLikeCount(postId);
    }

    @Override
    public int decreaseLikeCount(Long postId) {
        return postJpaRepository.decreaseLikeCount(postId);
    }

    @Override
    public int increaseCommentCount(Long postId) {
        return postJpaRepository.increaseCommentCount(postId);
    }

    @Override
    public int decreaseCommentCount(Long postId) {
        return postJpaRepository.decreaseCommentCount(postId);
    }

    @Override
    public List<Long> findPostIdsByWriterIdIn(List<Long> writerIds) {
        return postJpaRepository.findPostIdsByWriterIdIn(writerIds);
    }

    @Override
    public void deleteByWriterIdIn(List<Long> writerIds) {
        postJpaRepository.deleteByWriterIdIn(writerIds);
    }
}
