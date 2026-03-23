package com.taja.application.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taja.domain.board.PostLike;
import com.taja.global.exception.AlreadyLikedException;
import com.taja.global.exception.LikeNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PostLikeService")
class PostLikeServiceTest {

    @MockitoBean
    private PostLikeRepository postLikeRepository;

    @Autowired
    private PostLikeService postLikeService;

    @Nested
    @DisplayName("게시글에 좋아요 등록")
    class LikePost {

        @Test
        @DisplayName("좋아요 등록에 성공한다")
        void success() {
            Long memberId = 1L;
            Long postId = 2L;
            when(postLikeRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId)).thenReturn(false);

            postLikeService.likePost(memberId, postId);

            ArgumentCaptor<PostLike> captor = ArgumentCaptor.forClass(PostLike.class);
            verify(postLikeRepository).savePostLike(captor.capture());
            PostLike saved = captor.getValue();
            assertThat(saved.getPostId()).isEqualTo(postId);
            assertThat(saved.getMemberId()).isEqualTo(memberId);
            assertThat(saved.isDeleted()).isFalse();
        }

        @Test
        @DisplayName("이미 좋아요한 게시글이면 AlreadyLikedException 발생")
        void alreadyLiked_throwsAlreadyLikedException() {
            Long memberId = 1L;
            Long postId = 2L;
            when(postLikeRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId)).thenReturn(true);

            assertThatThrownBy(() -> postLikeService.likePost(memberId, postId))
                    .isInstanceOf(AlreadyLikedException.class)
                    .hasMessageContaining("이미 좋아요를 누른");

            verify(postLikeRepository, never()).savePostLike(any(PostLike.class));
        }
    }

    @Nested
    @DisplayName("게시글 좋아요 취소")
    class UnlikePost {

        @Test
        @DisplayName("좋아요 취소에 성공한다")
        void success() {
            Long memberId = 1L;
            Long postId = 2L;
            PostLike postLike = PostLike.of(postId, memberId);
            when(postLikeRepository.findByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId))
                    .thenReturn(Optional.of(postLike));

            postLikeService.unlikePost(memberId, postId);

            assertThat(postLike.isDeleted()).isTrue();
            verify(postLikeRepository).savePostLike(postLike);
        }

        @Test
        @DisplayName("좋아요하지 않은 게시글이면 LikeNotFoundException 발생")
        void notLiked_throwsLikeNotFoundException() {
            Long memberId = 1L;
            Long postId = 2L;
            when(postLikeRepository.findByPostIdAndMemberIdAndIsDeletedFalse(postId, memberId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> postLikeService.unlikePost(memberId, postId))
                    .isInstanceOf(LikeNotFoundException.class)
                    .hasMessageContaining("좋아요를 누른 사용자가 아닙니다");
        }
    }

    @Nested
    @DisplayName("좋아요 한 게시글인지 확인")
    class HasLiked {

        @Test
        @DisplayName("좋아요한 경우 true")
        void liked_returnsTrue() {
            when(postLikeRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(2L, 1L)).thenReturn(true);
            assertThat(postLikeService.hasLiked(2L, 1L)).isTrue();
        }

        @Test
        @DisplayName("좋아요하지 않은 경우 false")
        void notLiked_returnsFalse() {
            when(postLikeRepository.existsByPostIdAndMemberIdAndIsDeletedFalse(2L, 1L)).thenReturn(false);
            assertThat(postLikeService.hasLiked(2L, 1L)).isFalse();
        }
    }

    @Test
    @DisplayName("회원이 좋아요한 postId 집합을 반환한다")
    void success() {
        when(postLikeRepository.findLikedPostIdsByMemberIdAndPostIdIn(1L, List.of(2L, 3L)))
                .thenReturn(Set.of(2L));

        Set<Long> result = postLikeService.findLikedPostIdsByMemberIdAndPostIdIn(1L, List.of(2L, 3L));

        assertThat(result).containsExactlyInAnyOrder(2L);
    }
}
