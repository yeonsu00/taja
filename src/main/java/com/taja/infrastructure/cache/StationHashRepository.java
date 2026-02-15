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
                    updateValues.put("name", station.getName());
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

    public Optional<StationInfo.StationHashInfo> fetchAllFields(Integer number) {
        String hashKey = STATION_KEY_PREFIX + number;
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(hashKey);

        if (entries.isEmpty()) {
            return Optional.empty();
        }

        try {
            Long stationId = entries.get("stationId") != null ? Long.parseLong(entries.get("stationId").toString()) : null;
            String name = entries.get("name") != null ? entries.get("name").toString() : "";
            int bikeCount = entries.get("bikeCount") != null ? Integer.parseInt(entries.get("bikeCount").toString()) : 0;
            LocalDateTime requestedAt = entries.get("requestedAt") != null ? LocalDateTime.parse(entries.get("requestedAt").toString()) : null;

            return Optional.of(StationInfo.StationHashInfo.from(number, stationId, name, bikeCount, requestedAt));
        } catch (Exception e) {
            log.warn("Redis 데이터 파싱 실패: number={}, error={}", number, e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<StationInfo.NearbyStationHashInfo> fetchStationIdAndNameAndBikeCount(Integer number) {
        String hashKey = STATION_KEY_PREFIX + number;
        List<Object> values = redisTemplate.opsForHash().multiGet(hashKey, List.of("stationId", "name", "bikeCount"));

        if (values.get(0) == null) {
            return Optional.empty();
        }

        try {
            Long stationId = Long.parseLong(values.get(0).toString());
            String name = values.get(1) != null ? values.get(1).toString() : "";
            int bikeCount = values.get(2) != null ? Integer.parseInt(values.get(2).toString()) : 0;
            return Optional.of(StationInfo.NearbyStationHashInfo.from(stationId, name, bikeCount));
        } catch (Exception e) {
            log.warn("Redis 데이터 파싱 실패: number={}, error={}", number, e.getMessage());
            return Optional.empty();
        }
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

}
