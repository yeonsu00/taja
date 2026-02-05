package com.taja.infrastructure.board;

import com.taja.application.board.AllPostRankingRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.zset.Aggregate;
import org.springframework.data.redis.connection.zset.Weights;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class AllPostRankingRedisRepository implements AllPostRankingRepository {

    private static final String KEY_PREFIX = "ranking:all:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final long TTL_SECONDS = 2 * 24 * 60 * 60;
    private static final double CARRY_OVER_WEIGHT = 0.1;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addScore(long postId, double delta, LocalDate today) {
        String key = key(today);
        redisTemplate.opsForZSet().incrementScore(key, String.valueOf(postId), delta);
        redisTemplate.expire(key, Duration.ofSeconds(TTL_SECONDS));
    }

    @Override
    public List<Long> findRankedPostIds(long offset, int limit, LocalDate today) {
        String key = key(today);
        Set<String> members = redisTemplate.opsForZSet().reverseRange(key, offset, offset + limit - 1);
        if (members == null) {
            return List.of();
        }
        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public void carryOverTodayToTomorrow(LocalDate today) {
        LocalDate tomorrow = today.plusDays(1);
        String fromKey = key(today);
        String toKey = key(tomorrow);
        if (!redisTemplate.hasKey(fromKey)) {
            log.debug("랭킹 carry-over(전체): 오늘 키 없음, key={}", fromKey);
            return;
        }
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        zSetOps.unionAndStore(fromKey, Collections.emptyList(), toKey,
                Aggregate.SUM, Weights.of(CARRY_OVER_WEIGHT));
        redisTemplate.expire(toKey, Duration.ofSeconds(TTL_SECONDS));
        log.info("랭킹 carry-over(전체) 완료: fromDate={}, toDate={}", today, tomorrow);
    }

    private String key(LocalDate date) {
        return KEY_PREFIX + date.format(DATE_FORMAT);
    }
}
