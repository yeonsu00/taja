package com.taja.infrastructure.board;

import com.taja.application.board.PostLikeRepository;
import com.taja.domain.board.PostLike;
import com.taja.global.exception.AlreadyLikedException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PostLikeRepositoryImpl implements PostLikeRepository {

    private final PostLikeJpaRepository postLikeJpaRepository;

    @Override
    public List<PostLike> findByPostIdAndIsDeletedFalse(Long postId) {
        return postLikeJpaRepository.findByPostIdAndIsDeletedFalse(postId);
    }

    @Override
    public boolean existsByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId) {
        return postLikeJpaRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId);
    }

    @Override
    public Optional<PostLike> findByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId) {
        return postLikeJpaRepository.findByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId);
    }

    @Override
    public Set<Long> findLikedPostIdsByMemberIdAndPostIdIn(Long memberId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Set.of();
        }
        return postLikeJpaRepository.findPostIdsByMemberIdAndPostIdInAndIsDeletedFalse(memberId, postIds);
    }

    @Override
    public void savePostLike(PostLike postLike) {
        try {
            postLikeJpaRepository.save(postLike);
        } catch (DataIntegrityViolationException e) {
            throw new AlreadyLikedException("이미 좋아요를 누른 게시글입니다.");
        }
    }
}
