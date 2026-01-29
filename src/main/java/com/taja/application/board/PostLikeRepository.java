package com.taja.application.board;

import com.taja.domain.board.PostLike;
import java.util.List;
import java.util.Optional;

public interface PostLikeRepository {

    List<PostLike> findByPostIdAndIsDeletedFalse(Long postId);

    void saveAll(Iterable<PostLike> postLikes);

    boolean existsByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);

    Optional<PostLike> findByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);

    void savePostLike(PostLike postLike);
}
