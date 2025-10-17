package com.taja.station.infra;

import com.taja.station.application.StationRedisRepository;
import com.taja.station.domain.Station;
import com.taja.station.presentation.response.MapStationResponse;
import com.taja.status.domain.StationStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
            values.put("stationId", station.getStationId());
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
    public void saveStationsWithPipeline(List<Station> stations, LocalDateTime requestedAt) {
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                @SuppressWarnings({"unchecked"})
                public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                    for (Station station : stations) {
                        String hashKey = STATION_KEY_PREFIX + station.getNumber();
                        Map<String, Object> values = new HashMap<>();
                        values.put("stationId", station.getStationId());
                        values.put("bikeCount", 0);
                        values.put("requestedAt", requestedAt.withSecond(0).withNano(0).toString());
                        operations.opsForHash().putAll((K) hashKey, values);

                        operations.opsForGeo().add(
                                (K) GEO_KEY,
                                new Point(station.getLongitude(), station.getLatitude()),
                                (V) station.getNumber().toString()
                        );
                    }
                    return null;
                }
            });

            log.info("{}개의 대여소 정보를 Redis에 저장했습니다. ", stations.size());
        } catch (Exception e) {
            log.error("Redis 파이프라인 저장 실패: 총 {}개 중 작업 실패", stations.size(), e);
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
        updates.put("bikeCount", status.getParkingBikeCount());
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
    public void updateBikeCountAndRequestedAtWithPipeline(List<StationStatus> statuses) {
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                @SuppressWarnings({"unchecked"})
                public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                    for (StationStatus status : statuses) {
                        String key = STATION_KEY_PREFIX + status.getStationNumber();

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("bikeCount", status.getParkingBikeCount());
                        updates.put("requestedAt", status.getRequestedAt().toString());

                        operations.opsForHash().putAll((K) key, updates);
                    }
                    return null;
                }
            });

            log.info("Redis 파이프라인을 통해 {}개의 대여소 상태 업데이트 완료.", statuses.size());
        } catch (Exception e) {
            log.error("Redis 파이프라인 대여소 상태 업데이트 실패: 총 {}개 중 작업 실패", statuses.size(), e);
        }
    }

    @Override
    public List<MapStationResponse> findNearbyStations(double centerLat, double centerLon,
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
                .map(this::toNearbyStationResponse)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<MapStationResponse> findStationStatus(List<Station> favoriteStations) {
        return favoriteStations.stream()
                .map(station -> {
                    Object bikeCountObj = null;
                    Object requestedAtObj = null;
                    String hashKey = "stations:" + station.getNumber();

                    try {
                        bikeCountObj = redisTemplate.opsForHash().get(hashKey, "bikeCount");
                        requestedAtObj = redisTemplate.opsForHash().get(hashKey, "requestedAt");

                        int bikeCount = bikeCountObj != null ? Integer.parseInt(bikeCountObj.toString()) : 0;
                        LocalDateTime requestedAt = (requestedAtObj != null) ?
                                LocalDateTime.parse(requestedAtObj.toString()) : null;

                        return new MapStationResponse(
                                station.getStationId(),
                                station.getNumber(),
                                station.getLatitude(),
                                station.getLongitude(),
                                bikeCount,
                                requestedAt
                        );

                    } catch (NumberFormatException | DateTimeParseException e) {
                        log.warn("Redis 데이터 파싱 실패: station={}, bikeCount={}, requestedAt={}",
                                station.getNumber(), bikeCountObj, requestedAtObj);
                        throw new RuntimeException("데이터 파싱 중 오류가 발생했습니다.", e);

                    } catch (Exception e) {
                        log.error("대여소 상태 조회 중 예상치 못한 오류 발생 (대여소 번호: {}): {}", station.getNumber(), e.getMessage());
                        throw new RuntimeException("알 수 없는 오류가 발생했습니다.", e);
                    }
                })
                .collect(Collectors.toList());
    }

    private Optional<MapStationResponse> toNearbyStationResponse(GeoResult<GeoLocation<Object>> result) {
        try {
            String member = result.getContent().getName().toString();
            Point point = result.getContent().getPoint();
            Integer number = Integer.parseInt(member);

            String hashKey = STATION_KEY_PREFIX + number;
            Object stationIdObj = redisTemplate.opsForHash().get(hashKey, "stationId");
            Object bikeCountObj = redisTemplate.opsForHash().get(hashKey, "bikeCount");
            Object requestedAtObj = redisTemplate.opsForHash().get(hashKey, "requestedAt");

            Long stationId = stationIdObj != null ? Long.parseLong(stationIdObj.toString()) : null;
            int bikeCount = bikeCountObj != null ? Integer.parseInt(bikeCountObj.toString()) : 0;
            LocalDateTime requestedAt = requestedAtObj != null ? LocalDateTime.parse(requestedAtObj.toString()) : null;

            return Optional.of(
                    new MapStationResponse(stationId, number, point.getY(), point.getX(), bikeCount, requestedAt));

        } catch (NumberFormatException | DateTimeParseException e) {
            log.warn("대여소 정보 파싱 실패: number={}, error={}", result.getContent().getName(), e.getMessage());
            return Optional.empty();
        }
    }
}
