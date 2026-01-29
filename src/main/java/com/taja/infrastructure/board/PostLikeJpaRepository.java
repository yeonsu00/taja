package com.taja.infrastructure.board;

import com.taja.domain.board.PostLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeJpaRepository extends JpaRepository<PostLike, Long> {

    List<PostLike> findByPostIdAndIsDeletedFalse(Long postId);

    boolean existsByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);

    Optional<PostLike> findByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);
}
