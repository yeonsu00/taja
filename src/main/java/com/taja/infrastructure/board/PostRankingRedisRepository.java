package com.taja.infrastructure.board;

import com.taja.application.board.PostRankingRepository;
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
public class PostRankingRedisRepository implements PostRankingRepository {

    private static final String KEY_PREFIX = "ranking:station:";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final long TTL_SECONDS = 2 * 24 * 60 * 60;
    private static final double CARRY_OVER_WEIGHT = 0.1;

    private final StringRedisTemplate redisTemplate;

    @Override
    public void addScore(long stationId, long postId, double delta, LocalDate today) {
        String todayKey = key(stationId, today);
        redisTemplate.opsForZSet().incrementScore(todayKey, String.valueOf(postId), delta);
        redisTemplate.expire(todayKey, Duration.ofSeconds(TTL_SECONDS));
    }

    @Override
    public List<Long> findRankedPostIds(long stationId, long offset, int limit, LocalDate today) {
        String todayKey = key(stationId, today);
        Set<String> members = redisTemplate.opsForZSet().reverseRange(todayKey, offset, offset + limit - 1);
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
        String pattern = KEY_PREFIX + "*:" + today.format(DATE_FORMAT);
        Set<String> todayKeys = redisTemplate.keys(pattern);
        if (todayKeys.isEmpty()) {
            log.debug("랭킹 carry-over: 오늘 키 없음, pattern={}", pattern);
            return;
        }
        ZSetOperations<String, String> zSetOps = redisTemplate.opsForZSet();
        for (String fromKey : todayKeys) {
            Long stationId = parseStationId(fromKey);
            if (stationId == null) {
                continue;
            }
            String toKey = key(stationId, tomorrow);
            zSetOps.unionAndStore(fromKey, Collections.emptyList(), toKey,
                    Aggregate.SUM, Weights.of(CARRY_OVER_WEIGHT));
            redisTemplate.expire(toKey, Duration.ofSeconds(TTL_SECONDS));
        }
        log.info("랭킹 carry-over 완료: fromDate={}, toDate={}, keys={}", today, tomorrow, todayKeys.size());
    }

    private String key(long stationId, LocalDate date) {
        return KEY_PREFIX + stationId + ":" + date.format(DATE_FORMAT);
    }

    private Long parseStationId(String key) {
        if (key == null || !key.startsWith(KEY_PREFIX)) {
            return null;
        }
        String rest = key.substring(KEY_PREFIX.length());
        int colon = rest.indexOf(':');
        if (colon <= 0) {
            return null;
        }
        try {
            return Long.parseLong(rest.substring(0, colon));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
