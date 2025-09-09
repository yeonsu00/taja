package com.taja.station.infra;

import com.taja.station.application.StationRedisRepository;
import com.taja.station.domain.Station;
import com.taja.station.presentation.response.NearbyStationResponse;
import com.taja.status.domain.StationStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StationRedisRepositoryImpl implements StationRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String STATION_KEY_PREFIX = "stations:";
    private static final String GEO_KEY = "locations";

    @Override
    public boolean saveStation(Station station, LocalDateTime requestedAt) {
        String hashKey = STATION_KEY_PREFIX + station.getNumber();

        try {
            redisTemplate.opsForGeo().add(
                    GEO_KEY,
                    new Point(station.getLongitude(), station.getLatitude()),
                    String.valueOf(station.getNumber())
            );

            Map<String, Object> values = new HashMap<>();
            values.put("bikeCount", 0);
            values.put("requestedAt", requestedAt.withSecond(0).withNano(0).toString());

            redisTemplate.opsForHash().putAll(hashKey, values);

            return true;
        } catch (Exception e) {
            log.error("Redis 저장 실패: station={}", station.getNumber(), e);
            return false;
        }
    }

    @Override
    public boolean updateBikeCountAndRequestedAt(StationStatus status) {
        String key = STATION_KEY_PREFIX + status.getStationNumber();

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

    @Override
    public List<NearbyStationResponse> findNearbyStations(double centerLat, double centerLon,
                                                          double height, double width) {
        Point center = new Point(centerLon, centerLat);

        GeoShape shape = GeoShape.byBox(
                width, height, DistanceUnit.KILOMETERS
        );

        GeoResults<GeoLocation<Object>> results =
                redisTemplate.opsForGeo().search(
                        GEO_KEY,
                        GeoReference.fromCoordinate(center),
                        shape,
                        GeoSearchCommandArgs.newGeoSearchArgs().includeCoordinates()
                );

        if (results == null) {
            return List.of();
        }

        return results.getContent().stream()
                .map(r -> {
                    String member = r.getContent().getName().toString();
                    Point point = r.getContent().getPoint();
                    Integer number = Integer.parseInt(member);

                    String hashKey = STATION_KEY_PREFIX + number;
                    Object bikeCount = redisTemplate.opsForHash().get(hashKey, "bikeCount");
                    Object requestedAt = redisTemplate.opsForHash().get(hashKey, "requestedAt");

                    return new NearbyStationResponse(
                            number,
                            point.getY(), // latitude
                            point.getX(), // longitude
                            bikeCount != null ? Integer.parseInt(bikeCount.toString()) : 0,
                            requestedAt != null ? LocalDateTime.parse(requestedAt.toString()) : null
                    );
                })
                .collect(Collectors.toList());
    }
}
