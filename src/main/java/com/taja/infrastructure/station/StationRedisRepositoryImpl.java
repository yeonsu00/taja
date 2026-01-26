package com.taja.infrastructure.station;

import com.taja.application.station.StationInfo;
import com.taja.application.station.StationRedisRepository;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    public void saveStationsWithPipeline(List<Station> stations, LocalDateTime requestedAt) {
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                @SuppressWarnings({"unchecked"})
                public <K, V> Object execute(@NonNull RedisOperations<K, V> operations) throws DataAccessException {
                    for (Station station : stations) {
                        String hashKey = STATION_KEY_PREFIX + station.getNumber();

                        Map<String, Object> updateValues = new HashMap<>();
                        updateValues.put("stationId", station.getStationId());
                        updateValues.put("requestedAt", requestedAt.withSecond(0).withNano(0).toString());
                        updateValues.put("district", station.getDistrict());

                        operations.opsForHash().putAll((K) hashKey, updateValues);
                        operations.opsForHash().putIfAbsent((K) hashKey, "bikeCount", "0");

                        operations.opsForGeo().add(
                                (K) GEO_KEY,
                                new Point(station.getLongitude(), station.getLatitude()),
                                (V) station.getNumber().toString()
                        );
                    }
                    return null;
                }
            });

            log.info("{}개의 대여소 정보 갱신 및 신규 등록을 완료했습니다.", stations.size());
        } catch (Exception e) {
            log.error("Redis 파이프라인 작업 중 오류 발생", e);
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

                        LocalDateTime requestedAt = getLocalDateTime(status);
                        updates.put("requestedAt", requestedAt.toString());

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
    public List<StationInfo.StationGeoInfo> findNearbyStations(double centerLat, double centerLon,
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
                .map(this::toStationGeoInfo)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<StationInfo.StationFullInfo> findStationInfos(List<StationInfo.StationGeoInfo> geoInfos) {
        return geoInfos.stream()
                .map(this::toStationFullInfo)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<StationInfo.StationFullInfo> findStationStatus(List<Station> stations) {
        return stations.stream()
                .map(this::toStationFullInfoFromStation)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<StationInfo.StationGeoInfo> toStationGeoInfo(GeoResult<GeoLocation<Object>> result) {
        try {
            String member = result.getContent().getName().toString();
            Point point = result.getContent().getPoint();
            Integer number = Integer.parseInt(member);

            return Optional.of(
                    new StationInfo.StationGeoInfo(number, point.getY(), point.getX()));

        } catch (NumberFormatException e) {
            log.warn("대여소 GEO 정보 파싱 실패: number={}, error={}", result.getContent().getName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<StationInfo.StationFullInfo> toStationFullInfo(StationInfo.StationGeoInfo geoInfo) {
        try {
            String hashKey = STATION_KEY_PREFIX + geoInfo.number();
            Object stationIdObj = redisTemplate.opsForHash().get(hashKey, "stationId");
            Object bikeCountObj = redisTemplate.opsForHash().get(hashKey, "bikeCount");
            Object requestedAtObj = redisTemplate.opsForHash().get(hashKey, "requestedAt");

            Long stationId = stationIdObj != null ? Long.parseLong(stationIdObj.toString()) : null;
            int bikeCount = bikeCountObj != null ? Integer.parseInt(bikeCountObj.toString()) : 0;
            LocalDateTime requestedAt = requestedAtObj != null ? LocalDateTime.parse(requestedAtObj.toString()) : null;

            return Optional.of(
                    new StationInfo.StationFullInfo(stationId, geoInfo.number(), geoInfo.latitude(), geoInfo.longitude(), bikeCount, requestedAt));

        } catch (NumberFormatException | DateTimeParseException e) {
            log.warn("대여소 정보 파싱 실패: number={}, error={}", geoInfo.number(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<StationInfo.StationFullInfo> toStationFullInfoFromStation(Station station) {
        Object bikeCountObj = null;
        Object requestedAtObj = null;
        String hashKey = STATION_KEY_PREFIX + station.getNumber();

        try {
            bikeCountObj = redisTemplate.opsForHash().get(hashKey, "bikeCount");
            requestedAtObj = redisTemplate.opsForHash().get(hashKey, "requestedAt");

            int bikeCount = bikeCountObj != null ? Integer.parseInt(bikeCountObj.toString()) : 0;
            LocalDateTime requestedAt = (requestedAtObj != null) ?
                    LocalDateTime.parse(requestedAtObj.toString()) : null;

            return Optional.of(
                    new StationInfo.StationFullInfo(
                            station.getStationId(),
                            station.getNumber(),
                            station.getLatitude(),
                            station.getLongitude(),
                            bikeCount,
                            requestedAt
                    ));

        } catch (NumberFormatException | DateTimeParseException e) {
            log.warn("Redis 데이터 파싱 실패: station={}, bikeCount={}, requestedAt={}",
                    station.getNumber(), bikeCountObj, requestedAtObj);
            return Optional.empty();

        } catch (Exception e) {
            log.error("대여소 상태 조회 중 예상치 못한 오류 발생 (대여소 번호: {}): {}", station.getNumber(), e.getMessage());
            return Optional.empty();
        }
    }

    private static LocalDateTime getLocalDateTime(StationStatus status) {
        LocalDate date = status.getRequestedDate();
        LocalTime time = status.getRequestedTime();
        return LocalDateTime.of(date, time);
    }
}
