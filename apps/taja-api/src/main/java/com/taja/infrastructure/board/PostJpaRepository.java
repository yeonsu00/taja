package com.taja.infrastructure.board;

import com.taja.domain.board.Post;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostJpaRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByPostIdAndIsDeletedFalse(Long postId);

    Optional<Post> findByPostIdAndWriterIdAndIsDeletedFalse(Long postId, Long writerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount + 1 WHERE p.postId = :postId AND p.isDeleted = false")
    int increaseLikeCount(@Param("postId") Long postId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Post p SET p.likeCount = p.likeCount - 1 WHERE p.postId = :postId AND p.likeCount > 0 AND p.isDeleted = false")
    int decreaseLikeCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount + 1 WHERE p.postId = :postId AND p.isDeleted = false")
    int increaseCommentCount(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.commentCount = p.commentCount - 1 WHERE p.postId = :postId AND p.commentCount > 0 AND p.isDeleted = false")
    int decreaseCommentCount(@Param("postId") Long postId);

    @Query("SELECT p.postId FROM Post p WHERE p.writerId IN :writerIds")
    List<Long> findPostIdsByWriterIdIn(@Param("writerIds") List<Long> writerIds);

    @Modifying
    @Query("DELETE FROM Post p WHERE p.writerId IN :writerIds")
    void deleteByWriterIdIn(@Param("writerIds") List<Long> writerIds);
}
