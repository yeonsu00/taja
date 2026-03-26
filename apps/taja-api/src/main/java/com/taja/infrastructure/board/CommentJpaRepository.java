package com.taja.infrastructure.board;

import com.taja.domain.board.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndIsDeletedFalse(Long postId);

    Optional<Comment> findByCommentIdAndIsDeletedFalse(Long commentId);

    Optional<Comment> findByCommentIdAndWriterIdAndIsDeletedFalse(Long commentId, Long writerId);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.postId IN :postIds")
    void deleteByPostIdIn(@Param("postIds") List<Long> postIds);

    @Modifying
    @Query("DELETE FROM Comment c WHERE c.writerId IN :writerIds")
    void deleteByWriterIdIn(@Param("writerIds") List<Long> writerIds);
}
