package com.taja.simulator.infrastructure.worker;

import com.taja.simulator.domain.ActionType;
import com.taja.simulator.domain.UserContext;
import com.taja.simulator.infrastructure.ai.AiContentAgent;
import com.taja.simulator.infrastructure.client.TajaApiClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

@Slf4j
public class UserSimulationWorker implements Runnable {

    private final UserContext context;
    private final List<ActionType> actions;
    private final TajaApiClient apiClient;
    private final AiContentAgent aiContentAgent;
    private final boolean useAiContent;
    private final long delayMinMs;
    private final long delayMaxMs;
    private final long deadline;
    private final AtomicBoolean running;
    private final Consumer<String> logger;
    private final Runnable onSuccess;
    private final Runnable onFailure;
    private final Runnable onComplete;

    public UserSimulationWorker(
            UserContext context,
            List<ActionType> actions,
            TajaApiClient apiClient,
            AiContentAgent aiContentAgent,
            boolean useAiContent,
            long delayMinMs,
            long delayMaxMs,
            long deadline,
            AtomicBoolean running,
            Consumer<String> logger,
            Runnable onSuccess,
            Runnable onFailure,
            Runnable onComplete
    ) {
        this.context = context;
        this.actions = actions;
        this.apiClient = apiClient;
        this.aiContentAgent = aiContentAgent;
        this.useAiContent = useAiContent;
        this.delayMinMs = delayMinMs;
        this.delayMaxMs = delayMaxMs;
        this.deadline = deadline;
        this.running = running;
        this.logger = logger;
        this.onSuccess = onSuccess;
        this.onFailure = onFailure;
        this.onComplete = onComplete;
    }

    @Override
    public void run() {
        int actionIndex = 0;
        try {
            while (running.get() && System.currentTimeMillis() < deadline) {
                ActionType action = actions.get(actionIndex % actions.size());
                actionIndex++;

                boolean success = executeAction(action);
                if (success) onSuccess.run();
                else onFailure.run();

                if (!running.get() || System.currentTimeMillis() >= deadline) break;

                long delay = delayMinMs + (long) (Math.random() * (delayMaxMs - delayMinMs));
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            onComplete.run();
        }
    }

    private boolean executeAction(ActionType action) {
        return switch (action) {
            case SIGNUP -> handleSignup();
            case SEARCH_STATION -> handleSearchStation();
            case JOIN_BOARD -> handleJoinBoard();
            case CREATE_POST -> handleCreatePost();
            case CREATE_COMMENT -> handleCreateComment();
            case LIKE_POST -> handleLikePost();
            case ADD_FAVORITE -> handleAddFavorite();
            case VIEW_MAP -> handleViewMap();
        };
    }

    private boolean handleSignup() {
        boolean ok = apiClient.signup(context.getName(), context.getEmail());
        if (ok) {
            apiClient.login(context.getEmail()).ifPresent(context::setAccessToken);
            log("[{}] 회원가입 성공 ({})", context.getPersonaName(), context.getName());
        } else {
            log("[{}] 회원가입 실패", context.getPersonaName());
        }
        return ok && context.isLoggedIn();
    }

    private boolean handleSearchStation() {
        List<Long> stationIds = apiClient.searchStations();
        if (!stationIds.isEmpty()) {
            context.setKnownStationIds(stationIds);
            log("[{}] 역 검색 성공 → {}개 역 발견", context.getPersonaName(), stationIds.size());
            return true;
        }
        log("[{}] 역 검색 실패", context.getPersonaName());
        return false;
    }

    private boolean handleJoinBoard() {
        if (!context.isLoggedIn() || context.getKnownStationIds().isEmpty()) {
            log("[{}] 게시판 참여 스킵 (로그인 또는 역 정보 없음)", context.getPersonaName());
            return false;
        }
        Long stationId = context.getKnownStationIds().get(0);
        boolean ok = apiClient.joinBoard(stationId, context.getAccessToken());
        if (ok) {
            context.setLastJoinedStationId(stationId);
            log("[{}] 게시판 참여 성공 (stationId: {})", context.getPersonaName(), stationId);
        } else {
            log("[{}] 게시판 참여 실패 (stationId: {})", context.getPersonaName(), stationId);
        }
        return ok;
    }

    private boolean handleCreatePost() {
        if (!context.isLoggedIn() || context.getLastJoinedStationId() == null) {
            log("[{}] 게시글 작성 스킵 (로그인 또는 참여 게시판 없음)", context.getPersonaName());
            return false;
        }
        Long stationId = context.getLastJoinedStationId();
        String content = useAiContent
                ? aiContentAgent.generatePostContent(context.getPersonaName(), context.getPersonaDescription(), "역" + stationId)
                : context.getPersonaName() + "입니다. 따릉이 이용 좋았어요!";

        boolean ok = apiClient.createPost(stationId, content, context.getAccessToken());
        if (ok) {
            apiClient.getLatestPostId(stationId).ifPresent(context::setLastCreatedPostId);
            log("[{}] 게시글 작성 성공 (stationId: {})", context.getPersonaName(), stationId);
        } else {
            log("[{}] 게시글 작성 실패", context.getPersonaName());
        }
        return ok;
    }

    private boolean handleCreateComment() {
        if (!context.isLoggedIn()) {
            log("[{}] 댓글 작성 스킵 (로그인 없음)", context.getPersonaName());
            return false;
        }
        Long postId = resolvePostId();
        if (postId == null) {
            log("[{}] 댓글 작성 스킵 (게시글 없음)", context.getPersonaName());
            return false;
        }
        String content = useAiContent
                ? aiContentAgent.generateCommentContent(context.getPersonaName(), context.getPersonaDescription())
                : "유용한 정보 감사합니다!";

        boolean ok = apiClient.createComment(postId, content, context.getAccessToken());
        log("[{}] 댓글 작성 {} (postId: {})", context.getPersonaName(), ok ? "성공" : "실패", postId);
        return ok;
    }

    private boolean handleLikePost() {
        if (!context.isLoggedIn()) {
            log("[{}] 좋아요 스킵 (로그인 없음)", context.getPersonaName());
            return false;
        }
        Long postId = resolvePostId();
        if (postId == null) {
            log("[{}] 좋아요 스킵 (게시글 없음)", context.getPersonaName());
            return false;
        }
        boolean ok = apiClient.likePost(postId, context.getAccessToken());
        log("[{}] 좋아요 {} (postId: {})", context.getPersonaName(), ok ? "성공" : "실패", postId);
        return ok;
    }

    private boolean handleAddFavorite() {
        if (!context.isLoggedIn() || context.getKnownStationIds().isEmpty()) {
            log("[{}] 즐겨찾기 스킵 (로그인 또는 역 정보 없음)", context.getPersonaName());
            return false;
        }
        Long stationId = context.getKnownStationIds().get(0);
        boolean ok = apiClient.addFavorite(stationId, context.getAccessToken());
        log("[{}] 즐겨찾기 {} (stationId: {})", context.getPersonaName(), ok ? "성공" : "실패", stationId);
        return ok;
    }

    private boolean handleViewMap() {
        List<Long> stationIds = apiClient.searchStations();
        if (!stationIds.isEmpty() && context.getKnownStationIds().isEmpty()) {
            context.setKnownStationIds(stationIds);
        }
        log("[{}] 지도 조회 {} ({}개 역)", context.getPersonaName(), stationIds.isEmpty() ? "실패" : "성공", stationIds.size());
        return !stationIds.isEmpty();
    }

    private Long resolvePostId() {
        if (context.getLastCreatedPostId() != null) return context.getLastCreatedPostId();
        if (context.getLastJoinedStationId() != null) {
            return apiClient.getLatestPostId(context.getLastJoinedStationId()).orElse(null);
        }
        return null;
    }

    private void log(String format, Object... args) {
        String message = format.formatted(args);
        logger.accept(message);
    }
}
