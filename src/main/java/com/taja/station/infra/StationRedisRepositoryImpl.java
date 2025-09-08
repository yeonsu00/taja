package com.taja.station.infra;

import com.taja.status.domain.StationStatus;
import com.taja.station.application.StationRedisRepository;
import com.taja.station.domain.Station;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StationRedisRepositoryImpl implements StationRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean saveStation(Station station, LocalDateTime requestedAt) {
        String key = "station:" + station.getNumber();
        Map<String, Object> values = new HashMap<>();
        values.put("latitude", station.getLatitude());
        values.put("longitude", station.getLongitude());
        values.put("bikeCount", 0);
        values.put("requestedAt", requestedAt.withSecond(0).withNano(0).toString());

        try {
            redisTemplate.opsForHash().putAll(key, values);
            return true;
        } catch (Exception e) {
            log.error("Redis 저장 실패: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean updateBikeCountAndRequestedAt(StationStatus status) {
        String key = "station:" + status.getStationNumber();
        if (!redisTemplate.hasKey(key)) {
            log.warn("해당 key가 존재하지 않음: {}", key);
            return false;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("bikeCount", status.getParkingBikeTotalCount());
        updates.put("requestedAt", status.getRequestedAt().toString());

        try {
            redisTemplate.opsForHash().putAll(key, updates);
            return true;
        } catch (Exception e) {
            log.error("Redis 업데이트 실패: {}", key, e);
            return false;
        }
    }
}