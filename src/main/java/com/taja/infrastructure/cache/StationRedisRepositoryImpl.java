package com.taja.infrastructure.cache;

import com.taja.application.cache.StationInfo;
import com.taja.application.cache.StationInfo.BikeCountInfo;
import com.taja.application.cache.StationRedisRepository;
import com.taja.application.status.StationStatusRepository;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import com.taja.global.exception.StationNotFoundException;
import com.taja.infrastructure.station.StationJpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
@Slf4j
public class StationRedisRepositoryImpl implements StationRedisRepository {

    private final StationHashRepository stationHashRepository;
    private final StationGeoRepository stationGeoRepository;
    private final StationJpaRepository stationJpaRepository;
    private final StationStatusRepository stationStatusRepository;

    @Override
    public void saveStations(List<Station> stations, LocalDateTime requestedAt) {
        stationHashRepository.saveStationInfosWithPipeline(stations, requestedAt);
        stationGeoRepository.saveStationGeosWithPipeline(stations);
    }

    @Override
    public void updateBikeCountAndRequestedAtWithPipeline(List<StationStatus> statuses) {
        stationHashRepository.updateBikeCountAndRequestedAtWithPipeline(statuses);
    }

    @Override
    public List<StationInfo.StationGeoInfo> findStationsWithinBox(double centerLat, double centerLon, double height, double width) {
        return stationGeoRepository.findStationsWithinBox(centerLat, centerLon, height, width);
    }

    @Override
    public List<StationInfo.NearbyAvailableStation> findNearbyAvailableStations(
            double centerLat, double centerLon, double radiusKm, Integer excludeNumber) {
        List<StationInfo.NearbyStationGeoInfo> geoInfos =
                stationGeoRepository.findStationsWithinRadius(centerLat, centerLon, radiusKm);

        return geoInfos.stream()
                .filter(geo -> !geo.number().equals(excludeNumber))
                .map(geo -> {
                    Optional<StationInfo.NearbyStationHashInfo> hashInfo =
                            stationHashRepository.fetchStationIdAndNameAndBikeCount(geo.number());
                    if (hashInfo.isEmpty() || hashInfo.get().bikeCount() < 1) {
                        return null;
                    }
                    StationInfo.NearbyStationHashInfo info = hashInfo.get();
                    return new StationInfo.NearbyAvailableStation(
                            info.stationId(), geo.number(), info.name(),
                            geo.latitude(), geo.longitude(), (int) geo.distanceMeters());
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<StationInfo.StationFullInfo> findStationInfos(List<StationInfo.StationGeoInfo> geoInfos) {
        return geoInfos.stream()
                .map(geo -> getOrRefresh(geo.number(), geo.latitude(), geo.longitude()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<StationInfo.StationFullInfo> findStationStatus(List<Station> stations) {
        return stations.stream()
                .map(s -> getOrRefresh(s.getNumber(), s.getLatitude(), s.getLongitude()))
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    @Override
    public BikeCountInfo getStationStatusByNumber(Integer stationNumber) {
        Station station = stationJpaRepository.findByNumber(stationNumber)
                .orElseThrow(() -> new StationNotFoundException(stationNumber + " 번 대여소를 찾을 수 없습니다."));
        Long stationId = station.getStationId();
        return stationHashRepository.fetchAllFields(stationNumber)
                .map(info -> new BikeCountInfo(info.stationId(), info.bikeCount(), info.requestedAt()))
                .orElseGet(() -> stationStatusRepository.findLatestByStationNumber(stationNumber)
                        .map(status -> new BikeCountInfo(
                                stationId,
                                status.getParkingBikeCount(),
                                LocalDateTime.of(status.getRequestedDate(), status.getRequestedTime())))
                        .orElse(new BikeCountInfo(stationId, 0, LocalDateTime.now())));
    }

    private Optional<StationInfo.StationFullInfo> getOrRefresh(Integer number, double lat, double lon) {
        Optional<StationInfo.StationHashInfo> hashInfoOpt = stationHashRepository.fetchAllFields(number);
        Optional<StationInfo.StationFullInfo> stationFullInfo = StationInfo.StationFullInfo.from(hashInfoOpt.orElse(null), lat, lon);

        if (hashInfoOpt.isEmpty()) {
            Station station = stationJpaRepository.findByNumber(number)
                    .orElseThrow(() -> new StationNotFoundException(number + " 번 대여소를 찾을 수 없습니다."));
            LocalDateTime now = LocalDateTime.now();
            stationHashRepository.saveStationInfosWithPipeline(List.of(station), now);

            StationInfo.StationHashInfo hashInfo = stationHashRepository.fetchAllFields(number).orElseThrow(
                    () -> new StationNotFoundException(number + " 번 대여소 해시 정보를 찾을 수 없습니다.")
            );
            return StationInfo.StationFullInfo.from(hashInfo, lat, lon);
        }

        if (stationHashRepository.isThresholdReached(number)) {
            CompletableFuture.runAsync(() -> refreshCacheWithLock(number));
        }

        return stationFullInfo;
    }

    private void refreshCacheWithLock(Integer number) {
        if (!stationHashRepository.acquireLock(number)) {
            log.debug("다른 스레드가 이미 캐시 갱신 중입니다: number={}", number);
            return;
        }

        try {
            Optional<Station> stationOpt = stationJpaRepository.findByNumber(number);
            if (stationOpt.isEmpty()) {
                log.warn("대여소를 찾을 수 없습니다: number={}", number);
                return;
            }

            Station station = stationOpt.get();
            LocalDateTime now = LocalDateTime.now();
            stationHashRepository.saveStationInfosWithPipeline(List.of(station), now);

            log.debug("캐시 갱신 완료: number={}", number);
        } catch (Exception e) {
            log.error("캐시 갱신 중 오류 발생: number={}, error={}", number, e.getMessage(), e);
        } finally {
            stationHashRepository.releaseLock(number);
        }
    }
}
