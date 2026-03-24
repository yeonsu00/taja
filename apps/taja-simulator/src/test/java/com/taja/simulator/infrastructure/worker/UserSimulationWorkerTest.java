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
            Runnable onSuccess,
            Runnable onFailure,
            Runnable onComplete
    ) {
        return new UserSimulationWorker(
                context, actions, apiClient, aiContentAgent,
                false, 0, 0,
                running,
                msg -> {},
                onSuccess, onFailure, onComplete
        );
    }

    @Nested
    @DisplayName("실행 종료 조건")
    class Termination {

        @Test
        @DisplayName("running=false이면 행동을 실행하지 않고 onComplete만 호출된다")
        void running_false_no_action_executed() {
            AtomicBoolean running = new AtomicBoolean(false);
            AtomicInteger completeCount = new AtomicInteger(0);

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP), running,
                    () -> {}, () -> {}, completeCount::incrementAndGet
            );
            worker.run();

            assertThat(completeCount.get()).isEqualTo(1);
            verifyNoInteractions(apiClient);
        }

        @Test
        @DisplayName("행동 시퀀스를 모두 실행하면 자동으로 종료된다")
        void completes_after_all_actions() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger completeCount = new AtomicInteger(0);

            when(apiClient.signup(any(), any())).thenReturn(false);
            when(apiClient.searchStations()).thenReturn(List.of());

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP, ActionType.SEARCH_STATION), running,
                    () -> {}, successCount::incrementAndGet, completeCount::incrementAndGet
            );
            worker.run();

            assertThat(completeCount.get()).isEqualTo(1);
            assertThat(running.get()).isTrue(); // 외부 running 플래그는 변경하지 않음
        }

        @Test
        @DisplayName("onComplete는 항상 호출된다")
        void onComplete_always_called() {
            AtomicBoolean running = new AtomicBoolean(true);
            AtomicInteger completeCount = new AtomicInteger(0);

            when(apiClient.signup(any(), any())).thenReturn(true);
            when(apiClient.login(any())).thenReturn(Optional.of("token"));

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP), running,
                    () -> {}, () -> {}, completeCount::incrementAndGet
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
                    List.of(ActionType.SIGNUP), running,
                    successCount::incrementAndGet, () -> {}, () -> {}
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
                    List.of(ActionType.SIGNUP), running,
                    () -> {}, failureCount::incrementAndGet, () -> {}
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
                    List.of(ActionType.JOIN_BOARD), running,
                    () -> {}, failureCount::incrementAndGet, () -> {}
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

            context.setAccessToken("token");

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.CREATE_POST), running,
                    () -> {}, failureCount::incrementAndGet, () -> {}
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
                    List.of(ActionType.CREATE_COMMENT), running,
                    () -> {}, failureCount::incrementAndGet, () -> {}
            );
            worker.run();

            assertThat(failureCount.get()).isEqualTo(1);
            verifyNoInteractions(apiClient);
        }
    }

    @Nested
    @DisplayName("행동 시퀀스")
    class ActionSequence {

        @Test
        @DisplayName("actions 목록을 순서대로 1번씩 실행한다")
        void actions_executed_in_order_once() {
            AtomicBoolean running = new AtomicBoolean(true);

            when(apiClient.signup(any(), any())).thenReturn(false);
            when(apiClient.searchStations()).thenReturn(List.of());

            UserSimulationWorker worker = workerWith(
                    List.of(ActionType.SIGNUP, ActionType.SEARCH_STATION), running,
                    () -> {}, () -> {}, () -> {}
            );
            worker.run();

            verify(apiClient, times(1)).signup(any(), any());
            verify(apiClient, times(1)).searchStations();
        }
    }
}
