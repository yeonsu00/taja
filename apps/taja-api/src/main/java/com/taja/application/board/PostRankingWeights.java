package com.taja.application.board;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostRankingWeights {

    public static final double REGISTRATION_TIME = 0.1;
    public static final double VIEW = 0.1;
    public static final double COMMENT = 0.6;
    public static final double LIKE = 0.2;

    private static final double RECENCY_TIE_BREAKER_DIVISOR = 1e20;

    public static double registrationScoreWithRecency(long postId) {
        return REGISTRATION_TIME + (double) postId / RECENCY_TIE_BREAKER_DIVISOR;
    }
}
