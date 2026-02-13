package com.taja.infrastructure.board;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.application.board.PostRankingRepository;
import com.taja.application.board.PostRankingWeights;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("PostRankingRedisRepository")
class PostRankingRedisRepositoryTest {

    private static final long TEST_STATION_ID = 99_999L;
    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private PostRankingRepository postRankingRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        String key = "ranking:station:" + TEST_STATION_ID + ":" + TODAY.format(java.time.format.DateTimeFormatter.BASIC_ISO_DATE);
        redisTemplate.delete(key);
    }

    @Nested
    @DisplayName("게시글 등록 시 점수 반영")
    class WhenPostCreated {

        @Test
        @DisplayName("등록 시 REGISTRATION_TIME + recency(tie-breaker) 점수가 추가되고, 랭킹에 포함된다")
        void registration_addsScoreAndAppearsInRanking() {
            long postId = 1001L;
            double score = PostRankingWeights.registrationScoreWithRecency(postId);

            postRankingRepository.addScore(TEST_STATION_ID, postId, score, TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }

        @Test
        @DisplayName("여러 게시글 등록 시 모두 랭킹에 포함되고, recency로 동점이면 최신(postId 큰 순) 순으로 정렬된다")
        void multipleRegistrations_allInRanking_sortedByRecencyWhenTied() {
            long post1 = 1001L;
            long post2 = 1002L;
            long post3 = 1003L;
            postRankingRepository.addScore(TEST_STATION_ID, post1, PostRankingWeights.registrationScoreWithRecency(post1), TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post2, PostRankingWeights.registrationScoreWithRecency(post2), TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post3, PostRankingWeights.registrationScoreWithRecency(post3), TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked).containsExactly(post3, post2, post1);
        }
    }

    @Nested
    @DisplayName("게시글 상세 조회 시 점수 반영")
    class WhenPostViewed {

        @Test
        @DisplayName("상세 조회 시 VIEW 가중치만큼 점수가 추가된다")
        void view_addsViewScore() {
            long postId = 2001L;
            postRankingRepository.addScore(TEST_STATION_ID, postId, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postId, PostRankingWeights.VIEW, TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }

        @Test
        @DisplayName("조회 여러 번 시 VIEW 점수가 누적된다")
        void multipleViews_accumulateScore() {
            long postA = 2002L;
            long postB = 2003L;
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.VIEW, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.VIEW, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postB, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postB, PostRankingWeights.VIEW, TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked.get(0)).isEqualTo(postA);
            assertThat(ranked.get(1)).isEqualTo(postB);
        }
    }

    @Nested
    @DisplayName("좋아요 등록 시 점수 반영")
    class WhenLiked {

        @Test
        @DisplayName("좋아요 시 LIKE 가중치만큼 점수가 추가된다")
        void like_addsLikeScore() {
            long postId = 3001L;
            postRankingRepository.addScore(TEST_STATION_ID, postId, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postId, PostRankingWeights.LIKE, TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }

        @Test
        @DisplayName("좋아요 여러 개 시 LIKE 점수가 누적된다")
        void multipleLikes_accumulateScore() {
            long postA = 3002L;
            long postB = 3003L;
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.LIKE, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.LIKE, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postB, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postB, PostRankingWeights.LIKE, TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked.get(0)).isEqualTo(postA);
            assertThat(ranked.get(1)).isEqualTo(postB);
        }
    }

    @Nested
    @DisplayName("댓글 추가 시 점수 반영")
    class WhenCommentAdded {

        @Test
        @DisplayName("댓글 추가 시 COMMENT 가중치만큼 점수가 추가된다")
        void comment_addsCommentScore() {
            long postId = 4001L;
            postRankingRepository.addScore(TEST_STATION_ID, postId, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postId, PostRankingWeights.COMMENT, TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }

        @Test
        @DisplayName("댓글 여러 개 시 COMMENT 점수가 누적된다")
        void multipleComments_accumulateScore() {
            long postA = 4002L;
            long postB = 4003L;
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.COMMENT, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postA, PostRankingWeights.COMMENT, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postB, PostRankingWeights.REGISTRATION_TIME, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, postB, PostRankingWeights.COMMENT, TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked.get(0)).isEqualTo(postA);
            assertThat(ranked.get(1)).isEqualTo(postB);
        }
    }

    @Nested
    @DisplayName("통합: 등록·조회·좋아요·댓글 반영 후 랭킹 조회")
    class IntegratedRanking {

        @Test
        @DisplayName("여러 게시글에 등록/조회/좋아요/댓글 점수 반영 후, 점수 순으로 랭킹 조회가 된다")
        void mixedActions_rankingOrderByScore() {
            long post1 = 5001L;
            long post2 = 5002L;
            long post3 = 5003L;

            postRankingRepository.addScore(TEST_STATION_ID, post1, PostRankingWeights.registrationScoreWithRecency(post1), TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post1, PostRankingWeights.VIEW, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post1, PostRankingWeights.VIEW, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post1, PostRankingWeights.LIKE, TODAY);

            postRankingRepository.addScore(TEST_STATION_ID, post2, PostRankingWeights.registrationScoreWithRecency(post2), TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post2, PostRankingWeights.VIEW, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post2, PostRankingWeights.COMMENT, TODAY);

            postRankingRepository.addScore(TEST_STATION_ID, post3, PostRankingWeights.registrationScoreWithRecency(post3), TODAY);

            List<Long> ranked = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 10, TODAY);
            assertThat(ranked).hasSize(3);
            assertThat(ranked.get(0)).isEqualTo(post2);
            assertThat(ranked.get(1)).isEqualTo(post1);
            assertThat(ranked.get(2)).isEqualTo(post3);
        }

        @Test
        @DisplayName("랭킹 조회 시 offset/limit 페이징이 적용된다")
        void findRankedPostIds_respectsOffsetAndLimit() {
            long post1 = 6001L;
            long post2 = 6002L;
            long post3 = 6003L;
            postRankingRepository.addScore(TEST_STATION_ID, post1, 0.5, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post2, 0.8, TODAY);
            postRankingRepository.addScore(TEST_STATION_ID, post3, 0.3, TODAY);

            List<Long> firstPage = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 0, 2, TODAY);
            List<Long> secondPage = postRankingRepository.findRankedPostIds(TEST_STATION_ID, 2, 2, TODAY);

            assertThat(firstPage).containsExactly(post2, post1);
            assertThat(secondPage).containsExactly(post3);
        }
    }
}
