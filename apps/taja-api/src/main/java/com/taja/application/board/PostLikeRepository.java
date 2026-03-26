package com.taja.application.board;

import com.taja.domain.board.PostLike;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostLikeRepository {

    List<PostLike> findByPostIdAndIsDeletedFalse(Long postId);

    boolean existsByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);

    Set<Long> findLikedPostIdsByMemberIdAndPostIdIn(Long memberId, List<Long> postIds);

    Optional<PostLike> findByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);

    void savePostLike(PostLike postLike);

    void deleteByPostIdIn(List<Long> postIds);

    void deleteByMemberIdIn(List<Long> memberIds);
}
