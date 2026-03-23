package com.taja.application.board;

import java.time.LocalDate;
import java.util.List;

public interface AllPostRankingRepository {

    void addScore(long postId, double delta, LocalDate today);

    List<Long> findRankedPostIds(long offset, int limit, LocalDate today);

    void carryOverTodayToTomorrow(LocalDate today);
}
