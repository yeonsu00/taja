package com.taja.simulator.application;

import com.taja.simulator.domain.ActionType;
import com.taja.simulator.domain.UserContext;
import com.taja.simulator.infrastructure.ai.AiContentAgent;
import com.taja.simulator.infrastructure.client.TajaApiClient;
import com.taja.simulator.infrastructure.worker.UserSimulationWorker;
import com.taja.simulator.interfaces.api.dto.SimulationRequest;
import com.taja.simulator.interfaces.api.dto.SimulationStatusResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class SimulationService {

    private final TajaApiClient apiClient;
    private final AiContentAgent aiContentAgent;
    private final Executor simulatorExecutor;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger totalUsers = new AtomicInteger(0);
    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger completedActions = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failureCount = new AtomicInteger(0);

    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();
    private final List<CompletableFuture<Void>> futures = new CopyOnWriteArrayList<>();

    public SimulationService(
            TajaApiClient apiClient,
            AiContentAgent aiContentAgent,
            @Qualifier("simulatorExecutor") Executor simulatorExecutor
    ) {
        this.apiClient = apiClient;
        this.aiContentAgent = aiContentAgent;
        this.simulatorExecutor = simulatorExecutor;
    }

    public void start(SimulationRequest request) {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("시뮬레이션이 이미 실행 중입니다.");
        }

        resetCounters(request.users().size());

        long deadline = System.currentTimeMillis() + (long) request.durationSeconds() * 1000;

        for (SimulationRequest.UserConfig userConfig : request.users()) {
            UserContext context = new UserContext(userConfig.personaName(), userConfig.personaDescription());
            List<ActionType> actions = userConfig.actions().stream()
                    .map(ActionType::valueOf)
                    .toList();

            UserSimulationWorker worker = new UserSimulationWorker(
                    context,
                    actions,
                    apiClient,
                    aiContentAgent,
                    request.useAiContent(),
                    request.delayMinMs(),
                    request.delayMaxMs(),
                    deadline,
                    running,
                    this::broadcastLog,
                    () -> { successCount.incrementAndGet(); completedActions.incrementAndGet(); },
                    () -> { failureCount.incrementAndGet(); completedActions.incrementAndGet(); },
                    activeUsers::decrementAndGet
            );

            CompletableFuture<Void> future = CompletableFuture.runAsync(worker, simulatorExecutor);
            futures.add(future);
            activeUsers.incrementAndGet();
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenRun(() -> {
                    running.set(false);
                    broadcastLog("[시스템] 시뮬레이션 완료");
                    log.info("Simulation completed. success={}, failure={}", successCount.get(), failureCount.get());
                });
    }

    public void stop() {
        running.set(false);
        futures.forEach(f -> f.cancel(true));
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        } catch (Exception ignored) {
        }
        futures.clear();
        broadcastLog("[시스템] 시뮬레이션 중지됨");
    }

    public SimulationStatusResponse getStatus() {
        return new SimulationStatusResponse(
                running.get(),
                totalUsers.get(),
                activeUsers.get(),
                completedActions.get(),
                successCount.get(),
                failureCount.get()
        );
    }

    public SseEmitter registerLogEmitter() {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitter.onCompletion(() -> emitters.remove(emitter));
        emitter.onTimeout(() -> emitters.remove(emitter));
        emitter.onError(e -> emitters.remove(emitter));
        emitters.add(emitter);
        return emitter;
    }

    private void broadcastLog(String message) {
        log.info(message);
        List<SseEmitter> dead = new ArrayList<>();
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event().data(message));
            } catch (IOException e) {
                dead.add(emitter);
            }
        }
        emitters.removeAll(dead);
    }

    private void resetCounters(int userCount) {
        totalUsers.set(userCount);
        activeUsers.set(0);
        completedActions.set(0);
        successCount.set(0);
        failureCount.set(0);
        futures.clear();
    }
}
