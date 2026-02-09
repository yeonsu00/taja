package com.taja.infrastructure.cache;

import com.taja.application.cache.StationInfo;
import com.taja.domain.station.Station;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoSearchCommandArgs;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.data.redis.domain.geo.GeoShape;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StationGeoRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    public static final String GEO_KEY = "locations";

    public void saveStationGeosWithPipeline(List<Station> stations) {
        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (Station station : stations) {
                redisTemplate.opsForGeo().add(
                        GEO_KEY,
                        new Point(station.getLongitude(), station.getLatitude()),
                        station.getNumber().toString()
                );
            }
            return null;
        });
    }

    public List<StationInfo.StationGeoInfo> findStationsWithinShape(double centerLat, double centerLon, double height, double width) {
        Point center = new Point(centerLon, centerLat);
        GeoShape shape = GeoShape.byBox(width, height, DistanceUnit.KILOMETERS);

        GeoResults<GeoLocation<Object>> results = redisTemplate.opsForGeo().search(
                GEO_KEY,
                GeoReference.fromCoordinate(center),
                shape,
                GeoSearchCommandArgs.newGeoSearchArgs().includeCoordinates()
        );

        log.info("레디스 GEO 조회: {}개", results != null ? results.getContent().size() : 0);

        if (results == null) return List.of();

        return results.getContent().stream()
                .map(this::toStationGeoInfo)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private Optional<StationInfo.StationGeoInfo> toStationGeoInfo(GeoResult<GeoLocation<Object>> result) {
        try {
            String member = result.getContent().getName().toString();
            Point point = result.getContent().getPoint();
            return Optional.of(new StationInfo.StationGeoInfo(Integer.parseInt(member), point.getY(), point.getX()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

}
