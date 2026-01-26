package com.taja.application.cache;

import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StationCacheService {

    private final StationRedisRepository stationRedisRepository;

    public void saveStations(List<Station> stations, LocalDateTime requestedAt) {
        stationRedisRepository.saveStations(stations, requestedAt);
    }

    public void updateBikeCountAndRequestedAt(List<StationStatus> statuses) {
        stationRedisRepository.updateBikeCountAndRequestedAtWithPipeline(statuses);
    }

    public List<StationInfo.StationFullInfo> findStationStatus(List<Station> favoriteStations) {
        return stationRedisRepository.findStationStatus(favoriteStations);
    }

    public List<StationInfo.StationGeoInfo> findNearbyStations(double centerLat, double centerLon, double height, double width) {
        return stationRedisRepository.findNearbyStations(centerLat, centerLon, height, width);
    }

    public List<StationInfo.StationFullInfo> findStationInfos(List<StationInfo.StationGeoInfo> geoInfos) {
        return stationRedisRepository.findStationInfos(geoInfos);
    }
}
