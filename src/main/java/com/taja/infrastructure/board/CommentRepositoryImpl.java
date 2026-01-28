package com.taja.infrastructure.board;

import com.taja.application.board.CommentRepository;
import com.taja.domain.board.Comment;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepository {

    private final CommentJpaRepository commentJpaRepository;

    @Override
    public List<Comment> findByPostIdAndIsDeletedFalse(Long postId) {
        return commentJpaRepository.findByPostIdAndIsDeletedFalse(postId);
    }

    @Override
    public void saveComment(Comment comment) {
        commentJpaRepository.save(comment);
    }

    @Override
    public Optional<Comment> findByCommentIdAndIsDeletedFalse(Long commentId) {
        return commentJpaRepository.findByCommentIdAndIsDeletedFalse(commentId);
    }
}
