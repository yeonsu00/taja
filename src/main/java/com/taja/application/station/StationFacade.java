package com.taja.application.station;

import com.taja.application.cache.StationCacheService;
import com.taja.application.cache.StationInfo;
import com.taja.domain.station.Station;
import com.taja.interfaces.api.station.response.MapStationResponse;
import com.taja.interfaces.api.station.response.detail.StationDetailResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class StationFacade {

    private final StationService stationService;
    private final StationCacheService stationCacheService;

    @Transactional
    public int uploadStationsFromFile(MultipartFile file, LocalDateTime requestedAt) {
        List<Station> savedStations = stationService.uploadStationsFromFile(file);
        stationCacheService.saveStations(savedStations, requestedAt);
        return savedStations.size();
    }

    @Transactional(readOnly = true)
    public List<MapStationResponse> findNearbyStations(double centerLat, double centerLon,
                                                       double latDelta, double lonDelta) {
        double height = latDelta * 2;
        double width = lonDelta * 2;

        List<StationInfo.StationGeoInfo> geoInfos = stationCacheService.findNearbyStations(centerLat, centerLon, height, width);
        List<StationInfo.StationFullInfo> stationInfos = stationCacheService.findStationInfos(geoInfos);

        return StationInfo.StationFullInfo.toMapStationResponses(stationInfos);
    }

    @Transactional(readOnly = true)
    public StationDetailResponse findStationDetail(Long stationId) {
        Station station = stationService.findStationByStationId(stationId);

        List<StationInfo.StationGeoInfo> geoInfos = stationCacheService.findNearbyStations(station.getLatitude(), station.getLongitude(), 1, 1);
        List<StationInfo.StationFullInfo> stationInfos = stationCacheService.findStationInfos(geoInfos);

        List<MapStationResponse> nearbyStationsResponse = StationInfo.StationFullInfo.toMapStationResponses(stationInfos);

        List<Integer> nearbyStationsNumber =
                MapStationResponse.extractAvailableNumbers(
                        nearbyStationsResponse,
                        station.getNumber()
                );

        List<Station> nearbyStations = stationService.findStationByNumbers(nearbyStationsNumber);

        return StationDetailResponse.fromStation(station, nearbyStations);
    }
}
