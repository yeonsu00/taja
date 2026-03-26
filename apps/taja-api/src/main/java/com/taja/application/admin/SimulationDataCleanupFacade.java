package com.taja.application.admin;

import com.taja.application.board.BoardMemberService;
import com.taja.application.board.CommentService;
import com.taja.application.board.PostLikeService;
import com.taja.application.board.PostService;
import com.taja.application.favorite.FavoriteStationService;
import com.taja.application.member.AuthService;
import com.taja.domain.member.Member;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimulationDataCleanupFacade {

    private static final String SIM_EMAIL_PREFIX = "sim_";

    private final AuthService authService;
    private final PostService postService;
    private final CommentService commentService;
    private final PostLikeService postLikeService;
    private final BoardMemberService boardMemberService;
    private final FavoriteStationService favoriteStationService;

    @Transactional
    public int cleanupSimulationData() {
        List<Member> simMembers = authService.findMembersByEmailPrefix(SIM_EMAIL_PREFIX);
        if (simMembers.isEmpty()) {
            log.info("삭제할 시뮬레이션 사용자 없음");
            return 0;
        }

        List<Long> memberIds = simMembers.stream().map(Member::getMemberId).toList();
        List<String> tokenKeys = simMembers.stream().map(Member::getEmail).toList();
        List<Long> simPostIds = postService.findPostIdsByWriterIds(memberIds);

        log.info("시뮬레이션 데이터 삭제 시작: members={}, posts={}", memberIds.size(), simPostIds.size());

        if (!simPostIds.isEmpty()) {
            postLikeService.deleteByPostIdIn(simPostIds);
        }
        postLikeService.deleteByMemberIdIn(memberIds);

        if (!simPostIds.isEmpty()) {
            commentService.deleteByPostIdIn(simPostIds);
        }
        commentService.deleteByWriterIdIn(memberIds);

        postService.deletePostsByWriterIds(memberIds);

        if (!simPostIds.isEmpty()) {
            postService.removePostIdsFromRankings(simPostIds);
        }

        boardMemberService.deleteByMemberIdIn(memberIds);

        favoriteStationService.deleteByMemberIdIn(memberIds);

        authService.deleteRefreshTokensByKeys(tokenKeys);

        authService.deleteAllMembersById(memberIds);

        log.info("시뮬레이션 데이터 삭제 완료: {}명", memberIds.size());
        return memberIds.size();
    }
}
