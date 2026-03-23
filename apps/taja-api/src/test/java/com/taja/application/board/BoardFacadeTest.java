package com.taja.application.board;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taja.application.member.AuthService;
import com.taja.application.station.StationService;
import com.taja.domain.board.BoardMember;
import com.taja.domain.board.Post;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("BoardFacade")
class BoardFacadeTest {

    private static final String EMAIL = "user@test.com";
    private static final Long MEMBER_ID = 1L;
    private static final Long STATION_ID = 100L;
    private static final Long POST_ID = 2L;

    @Mock
    private BoardMemberService boardMemberService;

    @Mock
    private AuthService authService;

    @Mock
    private StationService stationService;

    @Mock
    private PostService postService;

    @Mock
    private PostLikeService postLikeService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private BoardFacade boardFacade;

    private Member member;
    private Station station;

    @BeforeEach
    void setUpMocks() {
        member = Mockito.mock(Member.class);
        lenient().when(member.getMemberId()).thenReturn(MEMBER_ID);
        station = Mockito.mock(Station.class);
        lenient().when(station.getStationId()).thenReturn(STATION_ID);
    }

    @Test
    @DisplayName("게시판 참여에 성공한다")
    void success_join() {
        when(authService.findMemberByEmail(EMAIL)).thenReturn(member);
        when(stationService.findStationByStationId(STATION_ID)).thenReturn(station);

        boardFacade.join(EMAIL, STATION_ID);

        verify(boardMemberService).joinBoard(any(BoardMember.class));
    }

    @Test
    @DisplayName("게시글 작성 후 Created 이벤트를 발행한다")
    void success_publishesCreatedEvent() {
        when(authService.findMemberByEmail(EMAIL)).thenReturn(member);
        when(stationService.findStationByStationId(STATION_ID)).thenReturn(station);
        Post saved = org.mockito.Mockito.mock(Post.class);
        when(saved.getPostId()).thenReturn(POST_ID);
        when(saved.getStationId()).thenReturn(STATION_ID);
        when(postService.createPost(MEMBER_ID, STATION_ID, "content")).thenReturn(saved);

        boardFacade.createPost(EMAIL, STATION_ID, "content");

        ArgumentCaptor<PostRankingEvent.Created> captor = ArgumentCaptor.forClass(PostRankingEvent.Created.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().stationId()).isEqualTo(STATION_ID);
        assertThat(captor.getValue().postId()).isEqualTo(POST_ID);
    }

    @Test
    @DisplayName("상세 게시글 조회 시 게시글 상세 정보와 사용자의 좋아요 여부를 반환하고, Viewed 이벤트를 발행한다")
    void find_post_detail() {
        when(authService.findMemberByEmail(EMAIL)).thenReturn(member);
        BoardInfo.PostDetailPart part = new BoardInfo.PostDetailPart(POST_ID, STATION_ID, "w", LocalDateTime.now(), "c", 0, 0);
        BoardInfo.PostDetail detail = BoardInfo.PostDetail.from(part, List.of(), false);
        when(postService.findPostDetailPart(POST_ID)).thenReturn(part);
        when(postService.enrichWithComments(part)).thenReturn(detail);
        when(postLikeService.hasLiked(POST_ID, MEMBER_ID)).thenReturn(true);

        BoardInfo.PostDetail result = boardFacade.findPostDetail(EMAIL, POST_ID);

        assertThat(result.postId()).isEqualTo(POST_ID);
        assertThat(result.liked()).isTrue();
        ArgumentCaptor<PostRankingEvent.Viewed> captor = ArgumentCaptor.forClass(PostRankingEvent.Viewed.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().stationId()).isEqualTo(STATION_ID);
        assertThat(captor.getValue().postId()).isEqualTo(POST_ID);
    }

    @Test
    @DisplayName("게시글 좋아요 등록 시 좋아요 후 LikeResult와 Liked 이벤트를 발행한다")
    void like_post() {
        when(authService.findMemberByEmail(EMAIL)).thenReturn(member);
        Post p = Mockito.mock(Post.class);
        lenient().when(p.getPostId()).thenReturn(POST_ID);
        lenient().when(p.getStationId()).thenReturn(STATION_ID);
        lenient().when(p.getLikeCount()).thenReturn(1);
        when(postService.findPostByPostId(POST_ID)).thenReturn(p);

        BoardInfo.LikeResult result = boardFacade.likePost(EMAIL, POST_ID);

        verify(postLikeService).likePost(MEMBER_ID, POST_ID);
        verify(postService).incrementLikeCount(POST_ID);
        verify(eventPublisher).publishEvent(any(PostRankingEvent.Liked.class));
        assertThat(result.postId()).isEqualTo(POST_ID);
    }
}
