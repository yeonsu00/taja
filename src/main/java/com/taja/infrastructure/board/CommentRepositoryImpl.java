package com.taja.infrastructure.board;

import com.taja.application.board.CommentRepository;
import com.taja.domain.board.Comment;
import java.util.List;
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
}
