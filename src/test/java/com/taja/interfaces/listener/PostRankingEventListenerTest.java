package com.taja.interfaces.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import com.taja.application.board.PostRankingEvent;
import com.taja.application.board.PostRankingWeights;
import com.taja.application.board.PostService;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostRankingEventListener")
class PostRankingEventListenerTest {

    @Mock
    private PostService postService;

    @InjectMocks
    private PostRankingEventListener listener;

    @Test
    @DisplayName("handleCreated: addRankingScore가 등록 가중치(REGISTRATION_TIME + recency)로 호출된다")
    void handleCreated_addsRegistrationScore() {
        PostRankingEvent.Created event = new PostRankingEvent.Created(1L, 100L);
        LocalDate today = LocalDate.now();

        listener.handleCreated(event);

        ArgumentCaptor<Double> scoreCaptor = ArgumentCaptor.forClass(Double.class);
        verify(postService).addRankingScore(eq(1L), eq(100L), scoreCaptor.capture(), eq(today));
        double expected = PostRankingWeights.registrationScoreWithRecency(100L);
        assertThat(scoreCaptor.getValue()).isCloseTo(expected, org.assertj.core.api.Assertions.within(1e-15));
    }

    @Test
    @DisplayName("handleLiked: addRankingScore가 LIKE 가중치로 호출된다")
    void handleLiked_addsLikeScore() {
        PostRankingEvent.Liked event = new PostRankingEvent.Liked(1L, 2L);
        LocalDate today = LocalDate.now();

        listener.handleLiked(event);

        verify(postService).addRankingScore(1L, 2L, PostRankingWeights.LIKE, today);
    }

    @Test
    @DisplayName("handleUnliked: addRankingScore가 -LIKE로 호출된다")
    void handleUnliked_subtractsLikeScore() {
        PostRankingEvent.Unliked event = new PostRankingEvent.Unliked(1L, 2L);
        LocalDate today = LocalDate.now();

        listener.handleUnliked(event);

        verify(postService).addRankingScore(1L, 2L, -PostRankingWeights.LIKE, today);
    }

    @Test
    @DisplayName("handleViewed: addRankingScore가 VIEW 가중치로 호출된다")
    void handleViewed_addsViewScore() {
        PostRankingEvent.Viewed event = new PostRankingEvent.Viewed(1L, 2L);
        LocalDate today = LocalDate.now();

        listener.handleViewed(event);

        verify(postService).addRankingScore(1L, 2L, PostRankingWeights.VIEW, today);
    }

    @Test
    @DisplayName("handleCommentCreated: addRankingScore가 COMMENT 가중치로 호출된다")
    void handleCommentCreated_addsCommentScore() {
        PostRankingEvent.CommentCreated event = new PostRankingEvent.CommentCreated(1L, 2L);
        LocalDate today = LocalDate.now();

        listener.handleCommentCreated(event);

        verify(postService).addRankingScore(1L, 2L, PostRankingWeights.COMMENT, today);
    }

    @Test
    @DisplayName("handleCommentDeleted: addRankingScore가 -COMMENT로 호출된다")
    void handleCommentDeleted_subtractsCommentScore() {
        PostRankingEvent.CommentDeleted event = new PostRankingEvent.CommentDeleted(1L, 2L);
        LocalDate today = LocalDate.now();

        listener.handleCommentDeleted(event);

        verify(postService).addRankingScore(1L, 2L, -PostRankingWeights.COMMENT, today);
    }
}
