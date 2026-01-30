package com.taja.application.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taja.domain.board.Comment;
import com.taja.global.exception.CommentNotFoundException;
import com.taja.global.exception.InvalidContentException;
import com.taja.global.exception.NotCommentWriterException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("CommentService")
class CommentServiceTest {

    @MockitoBean
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @Nested
    @DisplayName("댓글 작성")
    class CreateComment {

        @Test
        @DisplayName("댓글 작성에 성공한다")
        void success() {
            commentService.createComment(1L, 2L, "댓글 내용");

            verify(commentRepository).saveComment(any(Comment.class));
        }

        @Test
        @DisplayName("댓글 내용이 null이면 InvalidContentException 발생")
        void nullContent_throwsInvalidContentException() {
            assertThatThrownBy(() -> commentService.createComment(1L, 2L, null))
                    .isInstanceOf(InvalidContentException.class)
                    .hasMessageContaining("비어있습니다");

            verify(commentRepository, never()).saveComment(any(Comment.class));
        }

        @Test
        @DisplayName("댓글 내용이 공백이면 InvalidContentException 발생")
        void blankContent_throwsInvalidContentException() {
            assertThatThrownBy(() -> commentService.createComment(1L, 2L, "   "))
                    .isInstanceOf(InvalidContentException.class);

            verify(commentRepository, never()).saveComment(any(Comment.class));
        }
    }

    @Nested
    @DisplayName("댓글 ID로 댓글 조회")
    class FindCommentByCommentId {

        @Test
        @DisplayName("작성자와 일치하면 댓글을 반환한다")
        void success() {
            Comment comment = Comment.of(2L, 1L, "내용");
            when(commentRepository.findByCommentIdAndIsDeletedFalse(3L)).thenReturn(Optional.of(comment));

            Comment result = commentService.findCommentByCommentId(3L, 1L);

            assertThat(result).isEqualTo(comment);
        }

        @Test
        @DisplayName("해당 ID의 댓글이 없으면 CommentNotFoundException 발생")
        void notFound_throwsCommentNotFoundException() {
            when(commentRepository.findByCommentIdAndIsDeletedFalse(3L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> commentService.findCommentByCommentId(3L, 1L))
                    .isInstanceOf(CommentNotFoundException.class)
                    .hasMessageContaining("존재하지 않는 댓글");
        }

        @Test
        @DisplayName("작성자가 아니면 NotCommentWriterException 발생")
        void notWriter_throwsNotCommentWriterException() {
            Comment comment = Comment.of(2L, 99L, "내용"); // writerId=99
            when(commentRepository.findByCommentIdAndIsDeletedFalse(3L)).thenReturn(Optional.of(comment));

            assertThatThrownBy(() -> commentService.findCommentByCommentId(3L, 1L))
                    .isInstanceOf(NotCommentWriterException.class)
                    .hasMessageContaining("작성한 댓글이 아닙니다");
        }
    }
}
