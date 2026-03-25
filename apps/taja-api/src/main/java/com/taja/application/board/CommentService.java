package com.taja.application.board;

import com.taja.domain.board.Comment;
import com.taja.global.exception.CommentNotFoundException;
import com.taja.global.exception.InvalidContentException;
import com.taja.global.exception.NotCommentWriterException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final MeterRegistry meterRegistry;

    public void createComment(Long memberId, Long postId, String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidContentException("댓글 내용이 비어있습니다.");
        }

        Comment comment = Comment.of(postId, memberId, content);
        commentRepository.saveComment(comment);
        Counter.builder("comment.created.total").register(meterRegistry).increment();
    }

    public void softDeleteComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalse(postId);
        comments.forEach(Comment::markAsDeleted);
    }

    public Comment findCommentByCommentId(Long commentId, Long memberId) {
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException("존재하지 않는 댓글입니다."));

        if (!comment.getWriterId().equals(memberId)) {
            throw new NotCommentWriterException("해당 사용자가 작성한 댓글이 아닙니다.");
        }

        return comment;
    }

    public void softDeleteComment(Comment comment) {
        comment.markAsDeleted();
        commentRepository.saveComment(comment);
    }

    public void deleteByPostIdIn(List<Long> postIds) {
        commentRepository.deleteByPostIdIn(postIds);
    }

    public void deleteByWriterIdIn(List<Long> writerIds) {
        commentRepository.deleteByWriterIdIn(writerIds);
    }
}
