package com.taja.infrastructure.cache;

import com.taja.application.cache.StationInfo;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;

@Repository
@Slf4j
public class StationHashRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisTemplate<String, Object> redisTemplateMaster;

    public StationHashRepository(
            @Qualifier("createTemplate") RedisTemplate<String, Object> redisTemplate,
            @Qualifier("redisTemplateMaster") RedisTemplate<String, Object> redisTemplateMaster) {
        this.redisTemplate = redisTemplate;
        this.redisTemplateMaster = redisTemplateMaster;
    }

    public static final String STATION_KEY_PREFIX = "stations:";
    public static final String LOCK_PREFIX = "lock:station:";

    @Value("${cache.station.ttl-sec:3600}")
    private long cacheTtlSec;

    @Value("${cache.station.refresh-threshold-sec:10}")
    private long refreshThresholdSec;

    public void saveStationInfosWithPipeline(List<Station> stations, LocalDateTime requestedAt) {
        String formattedTime = requestedAt.withSecond(0).withNano(0).toString();

        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                for (Station station : stations) {
                    String hashKey = STATION_KEY_PREFIX + station.getNumber();

                    Map<String, Object> updateValues = new HashMap<>();
                    updateValues.put("stationId", station.getStationId());
                    updateValues.put("requestedAt", formattedTime);
                    updateValues.put("district", station.getDistrict());

                    operations.opsForHash().putAll((K) hashKey, updateValues);
                    operations.opsForHash().putIfAbsent((K) hashKey, "bikeCount", "0");

                    operations.expire((K) hashKey, Duration.ofSeconds(cacheTtlSec));
                }
                return null;
            }
        });
    }

    public void updateBikeCountAndRequestedAtWithPipeline(List<StationStatus> statuses) {
        redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            @SuppressWarnings("unchecked")
            public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                for (StationStatus status : statuses) {
                    String key = STATION_KEY_PREFIX + status.getStationNumber();
                    LocalDateTime reqTime = LocalDateTime.of(status.getRequestedDate(), status.getRequestedTime());

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("bikeCount", status.getParkingBikeCount());
                    updates.put("requestedAt", reqTime.toString());

                    operations.opsForHash().putAll((K) key, updates);
                    operations.expire((K) key, Duration.ofSeconds(cacheTtlSec));
                }
                return null;
            }
        });
    }

    public Optional<StationInfo.StationFullInfo> fetchFullInfo(Integer number, double lat, double lon) {
        String hashKey = STATION_KEY_PREFIX + number;
        List<Object> values = redisTemplate.opsForHash().multiGet(hashKey, List.of("bikeCount", "requestedAt", "stationId"));

        if (values.get(2) == null) {
            return Optional.empty();
        }

        log.info("Redis 데이터 조회: number={}, values={}", number, values);
        return parseFullInfo(number, lat, lon, values);
    }

    public boolean isThresholdReached(Integer number) {
        long remainTtl = redisTemplate.getExpire(STATION_KEY_PREFIX + number);
        return remainTtl > 0 && remainTtl < refreshThresholdSec;
    }

    public boolean acquireLock(Integer number) {
        String lockKey = LOCK_PREFIX + number;
        return Boolean.TRUE.equals(redisTemplateMaster.opsForValue().setIfAbsent(lockKey, "locked", Duration.ofSeconds(5)));
    }

    public void releaseLock(Integer number) {
        redisTemplateMaster.delete(LOCK_PREFIX + number);
    }

    private Optional<StationInfo.StationFullInfo> parseFullInfo(Integer number, double lat, double lon, List<Object> values) {
        try {
            int bikeCount = values.get(0) != null ? Integer.parseInt(values.get(0).toString()) : 0;
            LocalDateTime requestedAt = values.get(1) != null ? LocalDateTime.parse(values.get(1).toString()) : null;
            Long stationId = values.get(2) != null ? Long.parseLong(values.get(2).toString()) : null;

            return Optional.of(new StationInfo.StationFullInfo(stationId, number, lat, lon, bikeCount, requestedAt));
        } catch (Exception e) {
            log.warn("Redis 데이터 파싱 실패: number={}, error={}", number, e.getMessage());
            return Optional.empty();
        }
    }
}
