package com.taja.application.board;

import java.time.LocalDate;
import java.util.List;

public interface PostRankingRepository {
    void addScore(long stationId, long postId, double delta, LocalDate today);

    List<Long> findRankedPostIds(long stationId, long offset, int limit, LocalDate today);

    void carryOverTodayToTomorrow(LocalDate today);

    void removePostIdsFromAllRankings(List<Long> postIds);
}
