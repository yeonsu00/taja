package com.taja.infrastructure.board;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.application.board.AllPostRankingRepository;
import com.taja.application.board.PostRankingWeights;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("AllPostRankingRedisRepository")
class AllPostRankingRedisRepositoryTest {

    private static final LocalDate TODAY = LocalDate.now();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    @Autowired
    private AllPostRankingRepository allPostRankingRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @AfterEach
    void tearDown() {
        String todayKey = "ranking:all:" + TODAY.format(DATE_FORMAT);
        String tomorrowKey = "ranking:all:" + TODAY.plusDays(1).format(DATE_FORMAT);
        redisTemplate.delete(todayKey);
        redisTemplate.delete(tomorrowKey);
    }

    @Nested
    @DisplayName("게시글 등록 시 점수 반영")
    class WhenPostCreated {

        @Test
        @DisplayName("등록 시 점수가 추가되고 전체 랭킹에 포함된다")
        void registration_addsScoreAndAppearsInRanking() {
            long postId = 1001L;
            double score = PostRankingWeights.registrationScoreWithRecency(postId);

            allPostRankingRepository.addScore(postId, score, TODAY);

            List<Long> ranked = allPostRankingRepository.findRankedPostIds(0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }

        @Test
        @DisplayName("여러 게시글 등록 시 점수 순으로 정렬되어 조회된다")
        void multipleRegistrations_sortedByScore() {
            long post1 = 1001L;
            long post2 = 1002L;
            long post3 = 1003L;
            allPostRankingRepository.addScore(post1, PostRankingWeights.registrationScoreWithRecency(post1), TODAY);
            allPostRankingRepository.addScore(post2, PostRankingWeights.registrationScoreWithRecency(post2), TODAY);
            allPostRankingRepository.addScore(post3, PostRankingWeights.registrationScoreWithRecency(post3), TODAY);

            List<Long> ranked = allPostRankingRepository.findRankedPostIds(0, 10, TODAY);
            assertThat(ranked).containsExactly(post3, post2, post1);
        }
    }

    @Nested
    @DisplayName("좋아요/댓글/조회 시 점수 반영")
    class WhenInteraction {

        @Test
        @DisplayName("좋아요 시 LIKE 가중치만큼 점수가 추가된다")
        void like_addsLikeScore() {
            long postId = 2001L;
            allPostRankingRepository.addScore(postId, PostRankingWeights.REGISTRATION_TIME, TODAY);
            allPostRankingRepository.addScore(postId, PostRankingWeights.LIKE, TODAY);

            List<Long> ranked = allPostRankingRepository.findRankedPostIds(0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }

        @Test
        @DisplayName("댓글 시 COMMENT 가중치만큼 점수가 추가된다")
        void comment_addsCommentScore() {
            long postId = 2002L;
            allPostRankingRepository.addScore(postId, PostRankingWeights.REGISTRATION_TIME, TODAY);
            allPostRankingRepository.addScore(postId, PostRankingWeights.COMMENT, TODAY);

            List<Long> ranked = allPostRankingRepository.findRankedPostIds(0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }

        @Test
        @DisplayName("조회 시 VIEW 가중치만큼 점수가 추가된다")
        void view_addsViewScore() {
            long postId = 2003L;
            allPostRankingRepository.addScore(postId, PostRankingWeights.REGISTRATION_TIME, TODAY);
            allPostRankingRepository.addScore(postId, PostRankingWeights.VIEW, TODAY);

            List<Long> ranked = allPostRankingRepository.findRankedPostIds(0, 10, TODAY);
            assertThat(ranked).containsExactly(postId);
        }
    }

    @Nested
    @DisplayName("일간 Top 10 조회")
    class FindRankedPostIds {

        @Test
        @DisplayName("offset 0, limit 10으로 상위 10개만 조회된다")
        void returnsTop10() {
            for (long postId = 1L; postId <= 15L; postId++) {
                allPostRankingRepository.addScore(postId, 1.0 + postId * 0.1, TODAY);
            }

            List<Long> top10 = allPostRankingRepository.findRankedPostIds(0, 10, TODAY);

            assertThat(top10).hasSize(10);
            assertThat(top10.get(0)).isEqualTo(15L);
            assertThat(top10.get(9)).isEqualTo(6L);
        }

        @Test
        @DisplayName("랭킹 데이터가 없으면 빈 목록을 반환한다")
        void emptyRanking_returnsEmpty() {
            List<Long> ranked = allPostRankingRepository.findRankedPostIds(0, 10, TODAY);

            assertThat(ranked).isEmpty();
        }
    }

    @Nested
    @DisplayName("carry-over")
    class CarryOver {

        @Test
        @DisplayName("오늘 랭킹이 내일 키로 가중치 적용되어 이월된다")
        void carryOver_copiesToTomorrowWithWeight() {
            long postId = 3001L;
            allPostRankingRepository.addScore(postId, 1.0, TODAY);

            allPostRankingRepository.carryOverTodayToTomorrow(TODAY);

            LocalDate tomorrow = TODAY.plusDays(1);
            List<Long> tomorrowRanked = allPostRankingRepository.findRankedPostIds(0, 10, tomorrow);
            assertThat(tomorrowRanked).containsExactly(postId);
        }
    }
}
