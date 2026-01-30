package com.taja.application.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.taja.application.member.AuthService;
import com.taja.application.station.StationRepository;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.global.exception.AlreadyLikedException;
import com.taja.global.exception.LikeNotFoundException;
import com.taja.global.exception.NotStationMemberException;
import com.taja.global.exception.PostNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("Board 통합 테스트")
class BoardIntegrationTest {

    private static final String EMAIL = "testboard@test.com";
    private static final String PASSWORD = "password12!!";
    private static final String NAME = "boarduser!";

    @Autowired
    private BoardFacade boardFacade;

    @Autowired
    private AuthService authService;

    @Autowired
    private StationRepository stationRepository;

    @MockitoBean
    private PostRankingRepository postRankingRepository;

    private Long stationId;

    @BeforeEach
    void setUp() {
        authService.signup(NAME, EMAIL, PASSWORD);
        authService.signup("nomember", "nomember@test.com", PASSWORD);
        Station station = Station.builder()
                .name("테스트 대여소")
                .number(99999)
                .district("강남")
                .address("주소")
                .latitude(37.5)
                .longitude(127.0)
                .operationMode(OperationMode.LCD)
                .build();
        List<Station> saved = stationRepository.upsert(List.of(station));
        stationId = saved.getFirst().getStationId();
        boardFacade.join(EMAIL, stationId);
    }

    @Nested
    @DisplayName("게시판 참여 / 게시글 작성 / 목록·상세")
    class JoinAndPost {

        @Test
        @DisplayName("게시판 참여 후 게시글 작성, 목록·상세 조회")
        void success() {
            boardFacade.createPost(EMAIL, stationId, "첫 글");

            BoardInfo.PostItems latest = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
            assertThat(latest.items()).hasSize(1);
            assertThat(latest.items().getFirst().content()).isEqualTo("첫 글");
            assertThat(latest.items().getFirst().liked()).isFalse();

            Long postId = latest.items().getFirst().postId();
            BoardInfo.PostDetail detail = boardFacade.findPostDetail(EMAIL, postId);
            assertThat(detail.postId()).isEqualTo(postId);
            assertThat(detail.content()).isEqualTo("첫 글");
            assertThat(detail.liked()).isFalse();
        }

        @Test
        @DisplayName("참여하지 않은 게시판에서 게시글 작성 시 NotStationMemberException 예외를 던진다")
        void createPost_withoutJoin_throws() {
            assertThatThrownBy(() -> boardFacade.createPost("nomember@test.com", stationId, "글"))
                    .isInstanceOf(NotStationMemberException.class);
        }

        @Test
        @DisplayName("존재하지 않는 게시글 상세 조회 시 PostNotFoundException 예외를 던진다")
        void findPostDetail_notFound_throws() {
            assertThatThrownBy(() -> boardFacade.findPostDetail(EMAIL, 99999L))
                    .isInstanceOf(PostNotFoundException.class);
        }

        @Test
        @DisplayName("최신순 게시물 목록 조회 시 최신순 목록과 사용자 좋아요 여부를 반환한다")
        void findLatestPosts_returnsLatestOrderAndLikedStatus() {
            boardFacade.createPost(EMAIL, stationId, "첫 번째 글");
            boardFacade.createPost(EMAIL, stationId, "두 번째 글");

            BoardInfo.PostItems latest = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
            assertThat(latest.items()).hasSize(2);
            // 최신순: 두 번째 글이 먼저
            assertThat(latest.items().get(0).content()).isEqualTo("두 번째 글");
            assertThat(latest.items().get(1).content()).isEqualTo("첫 번째 글");
            // 아직 좋아요 안 함
            assertThat(latest.items().get(0).liked()).isFalse();
            assertThat(latest.items().get(1).liked()).isFalse();

            // 첫 번째 글만 좋아요
            Long firstPostId = latest.items().get(1).postId();
            boardFacade.likePost(EMAIL, firstPostId);

            BoardInfo.PostItems afterLike = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
            assertThat(afterLike.items()).hasSize(2);
            assertThat(afterLike.items().get(0).content()).isEqualTo("두 번째 글");
            assertThat(afterLike.items().get(0).liked()).isFalse();
            assertThat(afterLike.items().get(1).content()).isEqualTo("첫 번째 글");
            assertThat(afterLike.items().get(1).liked()).isTrue();
        }
    }

    @Nested
    @DisplayName("댓글")
    class Comment {

        @Test
        @DisplayName("댓글 작성 후 상세에서 댓글 수를 반영한다")
        void success() {
            boardFacade.createPost(EMAIL, stationId, "글");
            BoardInfo.PostItems items = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
            Long postId = items.items().getFirst().postId();

            boardFacade.createComment(EMAIL, postId, "댓글 내용");

            BoardInfo.PostDetail detail = boardFacade.findPostDetail(EMAIL, postId);
            assertThat(detail.commentCount()).isEqualTo(1);
            assertThat(detail.comments()).hasSize(1);
            assertThat(detail.comments().getFirst().content()).isEqualTo("댓글 내용");
        }
    }

    @Nested
    @DisplayName("좋아요")
    class Like {

        @Test
        @DisplayName("좋아요 후 취소, likeCount 및 liked 반영")
        void success() {
            boardFacade.createPost(EMAIL, stationId, "글");
            BoardInfo.PostItems items = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
            Long postId = items.items().getFirst().postId();

            BoardInfo.LikeResult likeResult = boardFacade.likePost(EMAIL, postId);
            assertThat(likeResult.likeCount()).isEqualTo(1);

            BoardInfo.PostDetail afterLike = boardFacade.findPostDetail(EMAIL, postId);
            assertThat(afterLike.likeCount()).isEqualTo(1);
            assertThat(afterLike.liked()).isTrue();

            boardFacade.unlikePost(EMAIL, postId);
            BoardInfo.PostDetail afterUnlike = boardFacade.findPostDetail(EMAIL, postId);
            assertThat(afterUnlike.likeCount()).isEqualTo(0);
        }

        @Test
        @DisplayName("이미 좋아요한 글에 다시 좋아요 시 AlreadyLikedException 예외를 던진다")
        void likeTwice_throws() {
            boardFacade.createPost(EMAIL, stationId, "글");
            BoardInfo.PostItems items = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
            Long postId = items.items().getFirst().postId();

            boardFacade.likePost(EMAIL, postId);
            assertThatThrownBy(() -> boardFacade.likePost(EMAIL, postId))
                    .isInstanceOf(AlreadyLikedException.class);
        }

        @Test
        @DisplayName("게시글 좋아요 하지 않은 상태에서 좋아요 취소를 할 경우 LikeNotFoundException 예외를 던진다")
        void unlikePost_withoutLike_throws() {
            boardFacade.createPost(EMAIL, stationId, "글");
            BoardInfo.PostItems items = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
            Long postId = items.items().getFirst().postId();

            assertThatThrownBy(() -> boardFacade.unlikePost(EMAIL, postId))
                    .isInstanceOf(LikeNotFoundException.class);
        }
    }
}
