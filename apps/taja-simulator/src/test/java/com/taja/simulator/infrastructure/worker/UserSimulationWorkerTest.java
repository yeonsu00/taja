package com.taja.simulator.infrastructure.worker;

import com.taja.simulator.domain.ActionType;
import com.taja.simulator.domain.UserContext;
import com.taja.simulator.infrastructure.ai.AiContentAgent;
import com.taja.simulator.infrastructure.client.TajaApiClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSimulationWorker")
class UserSimulationWorkerTest {

    @Mock
    private TajaApiClient apiClient;

    @Mock
    private AiContentAgent aiContentAgent;

    private UserContext context;

    @BeforeEach
    void setUp() {
        context = new UserContext("출퇴근러", "매일 따릉이로 출근하는 직장인");
    }

    private UserSimulationWorker workerWith(
            List<ActionType> actions,
            AtomicBoolean running,
            long deadlineOffsetMs,
            Runnable onSuccess,
            Runnable onFailure,
            Runnable onComplete
    ) {
        return new UserSimulationWorker(
                context, actions, apiClient, aiContentAgent,
                false, 0, 0,
                System.currentTimeMillis() + deadlineOffsetMs,
                running,
                msg -> {},
                onSuccess, onFailure, onComplete
        );
    }

    @Nested
    @DisplayName("루프 종료 조건")
    class LoopTermination {

        @Test
        @DisplayName("running=false이면 행동을 실행하지 않고 onComplete만 호출된다")
        void running_false_no_action_executed() {
            AtomicBoolean running = new AtomicBoolean(false);
            AtomicInteger completeCount = new AtomicInteger(0);

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP), running, 5000,
                    () -> {}, () -> {}, completeCount::incrementAndGet
            );
            worker.run();

            assertThat(completeCount.get()).isEqualTo(1);
            verifyNoInteractions(apiClient);
        }

        @Test
        @DisplayName("deadline이 지났으면 행동을 실행하지 않고 onComplete만 호출된다")
        void deadline_passed_no_action_executed() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger completeCount = new AtomicInteger(0);

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP), running, -1000,
                    () -> {}, () -> {}, completeCount::incrementAndGet
            );
            worker.run();

            assertThat(completeCount.get()).isEqualTo(1);
            verifyNoInteractions(apiClient);
        }

        @Test
        @DisplayName("onComplete는 루프가 어떻게 종료되든 항상 호출된다")
        void onComplete_always_called() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger completeCount = new AtomicInteger(0);

            when(apiClient.signup(any(), any())).thenReturn(true);
            when(apiClient.login(any())).thenReturn(Optional.of("token"));

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP), running, 200,
                    () -> running.set(false), () -> {}, completeCount::incrementAndGet
            );
            worker.run();

            assertThat(completeCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("콜백 호출")
    class Callbacks {

        @Test
        @DisplayName("행동 성공 시 onSuccess가 호출된다")
        void success_action_calls_onSuccess() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger successCount = new AtomicInteger(0);

            when(apiClient.signup(any(), any())).thenReturn(true);
            when(apiClient.login(any())).thenReturn(Optional.of("token"));

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP), running, 5000,
                    () -> { successCount.incrementAndGet(); running.set(false); },
                    () -> {},
                    () -> {}
            );
            worker.run();

            assertThat(successCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("행동 실패 시 onFailure가 호출된다")
        void failed_action_calls_onFailure() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger failureCount = new AtomicInteger(0);

            when(apiClient.signup(any(), any())).thenReturn(false);

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP), running, 5000,
                    () -> {},
                    () -> { failureCount.incrementAndGet(); running.set(false); },
                    () -> {}
            );
            worker.run();

            assertThat(failureCount.get()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("선행 조건 스킵")
    class SkipConditions {

        @Test
        @DisplayName("로그인 없이 JOIN_BOARD 실행 시 false를 반환한다")
        void joinBoard_without_login_returns_false() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger failureCount = new AtomicInteger(0);

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.JOIN_BOARD), running, 5000,
                    () -> {},
                    () -> { failureCount.incrementAndGet(); running.set(false); },
                    () -> {}
            );
            worker.run();

            assertThat(failureCount.get()).isEqualTo(1);
            verifyNoInteractions(apiClient);
        }

        @Test
        @DisplayName("lastJoinedStationId 없이 CREATE_POST 실행 시 false를 반환한다")
        void createPost_without_joinedStation_returns_false() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger failureCount = new AtomicInteger(0);

            context.setAccessToken("token");  // 로그인 상태

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.CREATE_POST), running, 5000,
                    () -> {},
                    () -> { failureCount.incrementAndGet(); running.set(false); },
                    () -> {}
            );
            worker.run();

            assertThat(failureCount.get()).isEqualTo(1);
            verifyNoInteractions(apiClient);
        }

        @Test
        @DisplayName("로그인 없이 CREATE_COMMENT 실행 시 false를 반환한다")
        void createComment_without_login_returns_false() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger failureCount = new AtomicInteger(0);

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.CREATE_COMMENT), running, 5000,
                    () -> {},
                    () -> { failureCount.incrementAndGet(); running.set(false); },
                    () -> {}
            );
            worker.run();

            assertThat(failureCount.get()).isEqualTo(1);
            verifyNoInteractions(apiClient);
        }
    }

    @Nested
    @DisplayName("행동 순환")
    class ActionCycle {

        @Test
        @DisplayName("actions 목록을 순서대로 순환하며 실행한다")
        void actions_executed_in_order() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger actionCount = new AtomicInteger(0);

            when(apiClient.signup(any(), any())).thenReturn(false);
            when(apiClient.searchStations()).thenReturn(List.of());

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP, ActionType.SEARCH_STATION), running, 5000,
                    () -> {},
                    () -> { if (actionCount.incrementAndGet() >= 2) running.set(false); },
                    () -> {}
            );
            worker.run();

            // 두 action이 각각 1번씩 호출됨
            verify(apiClient, times(1)).signup(any(), any());
            verify(apiClient, times(1)).searchStations();
        }
    }
}
