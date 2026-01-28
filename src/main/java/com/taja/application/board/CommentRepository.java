package com.taja.application.board;

import com.taja.domain.board.Comment;
import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    List<Comment> findByPostIdAndIsDeletedFalse(Long postId);

    void saveComment(Comment comment);

    Optional<Comment> findByCommentIdAndIsDeletedFalse(Long commentId);
}
