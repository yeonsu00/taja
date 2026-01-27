package com.taja.application.board;

import com.taja.domain.board.Comment;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public void softDeleteComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalse(postId);
        comments.forEach(Comment::markAsDeleted);
    }

}
