package com.taja.infrastructure.board;

import com.taja.domain.board.PostLike;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostLikeJpaRepository extends JpaRepository<PostLike, Long> {

    List<PostLike> findByPostIdAndIsDeletedFalse(Long postId);

    boolean existsByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);

    Optional<PostLike> findByPostIdAndMemberIdAndIsDeletedFalse(Long postId, Long memberId);

    @Query("SELECT pl.postId FROM PostLike pl WHERE pl.memberId = :memberId AND pl.postId IN :postIds AND pl.isDeleted = false")
    Set<Long> findPostIdsByMemberIdAndPostIdInAndIsDeletedFalse(@Param("memberId") Long memberId, @Param("postIds") List<Long> postIds);
}
