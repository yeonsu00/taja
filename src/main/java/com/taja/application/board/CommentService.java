package com.taja.application.board;

import com.taja.domain.board.Comment;
import com.taja.domain.board.Post;
import com.taja.global.exception.CommentNotFoundException;
import com.taja.global.exception.InvalidContentException;
import com.taja.global.exception.NotCommentWriterException;
import com.taja.global.exception.NotStationMemberException;
import com.taja.global.exception.PostNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final BoardMemberRepository boardMemberRepository;

    public void createComment(Long memberId, Long postId, String content) {
        if (content == null || content.isBlank()) {
            throw new InvalidContentException("댓글 내용이 비어있습니다.");
        }

        Post post = postRepository.findPostById(postId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));

        if (!boardMemberRepository.existsByStationIdAndMemberId(post.getStationId(), memberId)) {
            throw new NotStationMemberException("해당 게시판의 참여자가 아닙니다.");
        }

        Comment comment = Comment.of(postId, memberId, content);
        commentRepository.saveComment(comment);
        post.increaseCommentCount();
    }

    public void softDeleteComment(Long memberId, Long commentId) {
        Comment comment = commentRepository.findByCommentIdAndIsDeletedFalse(commentId)
                .orElseThrow(() -> new CommentNotFoundException("존재하지 않는 댓글입니다."));

        if (!comment.getWriterId().equals(memberId)) {
            throw new NotCommentWriterException("해당 사용자가 작성한 댓글이 아닙니다.");
        }

        Post post = postRepository.findPostById(comment.getPostId())
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));

        if (!boardMemberRepository.existsByStationIdAndMemberId(post.getStationId(), memberId)) {
            throw new NotStationMemberException("해당 게시판의 참여자가 아닙니다.");
        }

        comment.markAsDeleted();
        post.decreaseCommentCount();
    }

    public void softDeleteComments(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalse(postId);
        comments.forEach(Comment::markAsDeleted);
    }
}
