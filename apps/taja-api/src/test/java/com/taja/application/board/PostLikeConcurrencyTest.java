package com.taja.application.board;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.application.member.AuthService;
import com.taja.application.station.StationRepository;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.global.exception.AlreadyLikedException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("좋아요 동시성 테스트")
class PostLikeConcurrencyTest {

    private static final String EMAIL = "concurrency@test.com";
    private static final String PASSWORD = "password12!!";
    private static final String NAME = "concurrencyuser";

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
        Station station = Station.builder()
                .name("동시성 테스트 대여소")
                .number(88888)
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

    @Test
    @DisplayName("동일 사용자가 동일 게시글에 동시에 좋아요 시 1건만 성공한다 - likeCount 1")
    void concurrentLike_singleSuccess() throws InterruptedException {
        boardFacade.createPost(EMAIL, stationId, "글");
        BoardInfo.PostItems items = boardFacade.findLatestPosts(EMAIL, stationId, null, 10);
        Long postId = items.items().getFirst().postId();

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger alreadyLikedCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    start.await();
                    boardFacade.likePost(EMAIL, postId);
                    successCount.incrementAndGet();
                } catch (AlreadyLikedException e) {
                    alreadyLikedCount.incrementAndGet();
                } catch (Exception e) {
                    // other errors
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await();
        executor.shutdown();

        // 동시에 여러 스레드가 좋아요를 시도해도 최종적으로 1건만 반영되어야 함
        BoardInfo.PostDetail detail = boardFacade.findPostDetail(EMAIL, postId);
        assertThat(detail.likeCount()).isEqualTo(1);
        assertThat(detail.liked()).isTrue();
    }
}
