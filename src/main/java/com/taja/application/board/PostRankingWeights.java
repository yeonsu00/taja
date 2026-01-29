package com.taja.application.board;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostRankingWeights {

    public static final double LIKE = 0.3;
    public static final double VIEW = 0.1;
    public static final double COMMENT = 0.6;
}
