package com.taja.application.board;

import com.taja.domain.board.Comment;
import java.util.List;

public interface CommentRepository {

    List<Comment> findByPostIdAndIsDeletedFalse(Long postId);

}
