package com.taja.application.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taja.application.board.BoardInfo.PostItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PostService")
class PostServiceTest {

    @MockitoBean
    private PostRepository postRepository;

    @MockitoBean
    private PostRankingRepository postRankingRepository;

    @MockitoBean
    private AllPostRankingRepository allPostRankingRepository;

    @Autowired
    private PostService postService;

    @Nested
    @DisplayName("최신순 게시물 조회")
    class FindLatestPosts {

        @Test
        @DisplayName("최신순 목록과 nextCursor를 반환한다")
        void success() {
            List<PostItem> fetched = List.of(
                    postItem(1L, 100L),
                    postItem(1L, 99L),
                    postItem(1L, 98L)
            );
            when(postRepository.findLatestPosts(1L, 0L, 3)).thenReturn(fetched);

            BoardInfo.PostItems result = postService.findLatestPosts(1L, null, 2);

            assertThat(result.items()).hasSize(2);
            assertThat(result.items().get(0).postId()).isEqualTo(100L);
            assertThat(result.items().get(1).postId()).isEqualTo(99L);
            assertThat(result.nextCursor()).isEqualTo("99");
        }

        @Test
        @DisplayName("조회 결과가 size 이하면 nextCursor는 null")
        void noMore_nextCursorNull() {
            List<PostItem> fetched = List.of(postItem(1L, 100L));
            when(postRepository.findLatestPosts(1L, 0L, 3)).thenReturn(fetched);

            BoardInfo.PostItems result = postService.findLatestPosts(1L, null, 2);

            assertThat(result.items()).hasSize(1);
            assertThat(result.nextCursor()).isNull();
        }

        private PostItem postItem(Long stationId, Long postId) {
            return new PostItem(stationId, postId, "writer", LocalDateTime.now(), "content", 0, 0, false);
        }
    }

    @Nested
    @DisplayName("인기순 게시물 조회")
    class FindPopularPosts {

        @Test
        @DisplayName("랭킹 순 목록과 nextCursor를 반환한다")
        void success() {
            LocalDate today = LocalDate.now();
            List<Long> rankedIds = List.of(10L, 9L, 8L);
            List<PostItem> items = List.of(
                    postItem(1L, 10L),
                    postItem(1L, 9L),
                    postItem(1L, 8L)
            );
            when(postRankingRepository.findRankedPostIds(1L, 0L, 3, today)).thenReturn(rankedIds);
            when(postRepository.findPostItemsByStationIdAndPostIds(1L, rankedIds)).thenReturn(items);

            BoardInfo.PostItems result = postService.findPopularPosts(1L, null, 2, today);

            assertThat(result.items()).hasSize(2);
            assertThat(result.nextCursor()).isEqualTo("2");
        }

        @Test
        @DisplayName("랭킹 데이터가 없으면 빈 목록 반환")
        void emptyRanking_returnsEmpty() {
            when(postRankingRepository.findRankedPostIds(eq(1L), eq(0L), eq(3), any(LocalDate.class))).thenReturn(List.of());

            BoardInfo.PostItems result = postService.findPopularPosts(1L, null, 2, LocalDate.now());

            assertThat(result.items()).isEmpty();
            assertThat(result.nextCursor()).isNull();
        }

        private PostItem postItem(Long stationId, Long postId) {
            return new PostItem(stationId, postId, "writer", LocalDateTime.now(), "content", 0, 0, false);
        }
    }

    @Nested
    @DisplayName("인기순 랭킹 점수 추가")
    class AddRankingScore {

        @Test
        @DisplayName("PostRankingRepository.addScore가 올바른 인자로 호출된다")
        void delegatesToRepository() {
            LocalDate today = LocalDate.now();
            postService.addRankingScore(1L, 2L, 0.5, today);

            verify(postRankingRepository).addScore(1L, 2L, 0.5, today);
        }
    }
}
