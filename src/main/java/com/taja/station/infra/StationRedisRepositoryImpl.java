package com.taja.station.infra;

import com.taja.station.application.StationRedisRepository;
import com.taja.station.domain.Station;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StationRedisRepositoryImpl implements StationRedisRepository {

    private final ReactiveRedisTemplate<String, Object> reactiveRedisTemplate;

    @Override
    public Mono<Boolean> saveStation(Station station, LocalDateTime savedAt) {
        String key = "station:" + station.getNumber();
        Map<String, Object> values = Map.of(
                "latitude", station.getLatitude(),
                "longitude", station.getLongitude(),
                "bikeCount", 0,
                "requestedAt", savedAt.withSecond(0).withNano(0).toString()
        );
        return reactiveRedisTemplate.opsForHash()
                .putAll(key, values)
                .doOnError(e -> log.error("Redis 저장 실패: {}", key, e));
    }


}
