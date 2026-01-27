package com.taja.application.board;

import com.taja.application.member.AuthService;
import com.taja.application.station.StationService;
import com.taja.domain.board.BoardMember;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class BoardFacade {

    private final BoardMemberService boardMemberService;
    private final AuthService authService;
    private final StationService stationService;
    private final PostService postService;

    @Transactional
    public void join(String email, Long stationId) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);

        BoardMember boardMember = BoardMember.of(station.getStationId(), member.getMemberId());
        boardMemberService.joinBoard(boardMember);
    }

    @Transactional
    public void createPost(String email, Long stationId, String content) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);

        postService.createPost(member.getMemberId(), station.getStationId(), content);
    }

    public BoardInfo.PostItems findLatestPosts(String email, Long stationId, String cursor, int size) {
        Member member = authService.findMemberByEmail(email);
        Station station = stationService.findStationByStationId(stationId);

        List<BoardInfo.PostItem> fetchedItems = postService.findLatestPosts(
                member.getMemberId(), station.getStationId(), cursor, size);

        List<BoardInfo.PostItem> pagedItems = fetchedItems;
        String nextCursor = null;

        if (postService.hasNext(fetchedItems, size)) {
            pagedItems = fetchedItems.subList(0, size);
            nextCursor = PostCursor.encode(pagedItems.getLast().postId());
        }

        return new BoardInfo.PostItems(pagedItems, nextCursor);
    }

    public BoardInfo.PostItems findPopularPosts(String email, Long stationId, String cursor, int size) {
        return new BoardInfo.PostItems(List.of(), null);
    }

    public BoardInfo.PostDetail findPostDetail(String email, Long postId) {
        Member member = authService.findMemberByEmail(email);
        return postService.findPostDetail(member.getMemberId(), postId);
    }
}
