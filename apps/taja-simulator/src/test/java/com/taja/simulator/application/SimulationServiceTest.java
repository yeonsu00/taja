package com.taja.simulator.application;

import com.taja.simulator.infrastructure.ai.AiContentAgent;
import com.taja.simulator.infrastructure.client.TajaApiClient;
import com.taja.simulator.interfaces.api.dto.SimulationRequest;
import com.taja.simulator.interfaces.api.dto.SimulationStatusResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("SimulationService")
class SimulationServiceTest {

    @Mock
    private TajaApiClient apiClient;

    @Mock
    private AiContentAgent aiContentAgent;

    private SimulationService service;

    // signup()은 mock 기본값(false)을 사용 — 스텁 불필요
    // 2개 액션 + 1000ms 딜레이 → 두 번째 start() 호출 시 워커가 딜레이 중으로 running=true 유지
    private static final SimulationRequest ONE_USER_REQUEST =
            new SimulationRequest(1000, 1000, false,
                    List.of(new SimulationRequest.UserConfig("테스트", "설명", List.of("SIGNUP", "SEARCH_STATION"), 1)));

    @BeforeEach
    void setUp() {
        service = new SimulationService(apiClient, aiContentAgent, Executors.newCachedThreadPool());
    }

    @AfterEach
    void tearDown() {
        service.stop();
    }

    @Nested
    @DisplayName("start()")
    class Start {

        @Test
        @DisplayName("시뮬레이션이 실행 중일 때 start()를 호출하면 예외가 발생한다")
        void start_while_running_throws() {
            service.start(ONE_USER_REQUEST);

            assertThatThrownBy(() -> service.start(ONE_USER_REQUEST))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("시뮬레이션이 이미 실행 중입니다.");
        }

        @Test
        @DisplayName("동시에 두 스레드가 start()를 호출하면 하나만 성공한다")
        void concurrent_start_only_one_succeeds() throws InterruptedException {
            CountDownLatch ready = new CountDownLatch(2);
            CountDownLatch go = new CountDownLatch(1);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger exceptionCount = new AtomicInteger(0);

            Runnable task = () -> {
                ready.countDown();
                try {
                    go.await();
                    service.start(ONE_USER_REQUEST);
                    successCount.incrementAndGet();
                } catch (IllegalStateException e) {
                    exceptionCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            };

            Thread t1 = new Thread(task);
            Thread t2 = new Thread(task);
            t1.start();
            t2.start();

            ready.await();
            go.countDown();

            t1.join();
            t2.join();

            assertThat(successCount.get()).isEqualTo(1);
            assertThat(exceptionCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("start() 후 running 상태가 true다")
        void start_sets_running_true() {
            service.start(ONE_USER_REQUEST);

            assertThat(service.getStatus().running()).isTrue();
        }
    }

    @Nested
    @DisplayName("stop()")
    class Stop {

        @Test
        @DisplayName("stop() 호출 후 running 상태가 false다")
        void stop_sets_running_false() {
            service.start(ONE_USER_REQUEST);
            service.stop();

            assertThat(service.getStatus().running()).isFalse();
        }

        @Test
        @DisplayName("stop() 후 start()를 다시 호출할 수 있다")
        void stop_then_start_again() {
            service.start(ONE_USER_REQUEST);
            service.stop();

            service.start(ONE_USER_REQUEST);
            assertThat(service.getStatus().running()).isTrue();
        }
    }

    @Nested
    @DisplayName("getStatus()")
    class GetStatus {

        @Test
        @DisplayName("초기 상태에서 running=false, 모든 카운터는 0이다")
        void initial_status() {
            SimulationStatusResponse status = service.getStatus();

            assertThat(status.running()).isFalse();
            assertThat(status.totalUsers()).isZero();
            assertThat(status.activeUsers()).isZero();
            assertThat(status.completedActions()).isZero();
            assertThat(status.successCount()).isZero();
            assertThat(status.failureCount()).isZero();
        }

        @Test
        @DisplayName("start() 후 totalUsers가 요청한 사용자 수와 같다")
        void start_sets_totalUsers() {
            service.start(ONE_USER_REQUEST);

            assertThat(service.getStatus().totalUsers()).isEqualTo(1);
        }
    }
}
