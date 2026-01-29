package com.taja.interfaces.listener;

import com.taja.application.board.PostRankingEvent;
import com.taja.application.board.PostRankingWeights;
import com.taja.application.board.PostService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostRankingEventListener {

    private final PostService postService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLiked(PostRankingEvent.Liked event) {
        try {
            postService.addRankingScore(event.stationId(), event.postId(), PostRankingWeights.LIKE, LocalDate.now());
        } catch (Exception e) {
            log.warn("랭킹 점수 갱신 실패 (Liked): stationId={}, postId={}, error={}",
                    event.stationId(), event.postId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUnliked(PostRankingEvent.Unliked event) {
        try {
            postService.addRankingScore(event.stationId(), event.postId(), -PostRankingWeights.LIKE, LocalDate.now());
        } catch (Exception e) {
            log.warn("랭킹 점수 갱신 실패 (Unliked): stationId={}, postId={}, error={}",
                    event.stationId(), event.postId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleViewed(PostRankingEvent.Viewed event) {
        try {
            postService.addRankingScore(event.stationId(), event.postId(), PostRankingWeights.VIEW, LocalDate.now());
        } catch (Exception e) {
            log.warn("랭킹 점수 갱신 실패 (Viewed): stationId={}, postId={}, error={}",
                    event.stationId(), event.postId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(PostRankingEvent.CommentCreated event) {
        try {
            postService.addRankingScore(event.stationId(), event.postId(), PostRankingWeights.COMMENT, LocalDate.now());
        } catch (Exception e) {
            log.warn("랭킹 점수 갱신 실패 (CommentCreated): stationId={}, postId={}, error={}",
                    event.stationId(), event.postId(), e.getMessage());
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(PostRankingEvent.CommentDeleted event) {
        try {
            postService.addRankingScore(event.stationId(), event.postId(), -PostRankingWeights.COMMENT, LocalDate.now());
        } catch (Exception e) {
            log.warn("랭킹 점수 갱신 실패 (CommentDeleted): stationId={}, postId={}, error={}",
                    event.stationId(), event.postId(), e.getMessage());
        }
    }
}
