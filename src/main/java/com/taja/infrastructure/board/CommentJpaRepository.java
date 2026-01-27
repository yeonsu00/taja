package com.taja.infrastructure.board;

import com.taja.domain.board.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentJpaRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndIsDeletedFalse(Long postId);
}
