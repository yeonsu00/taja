package com.taja.station.application;

import com.taja.station.domain.Station;
import com.taja.station.presentation.response.MapStationResponse;
import com.taja.station.presentation.response.StationSimpleResponse;
import com.taja.station.presentation.response.detail.StationDetailResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationFileReader stationFileReader;
    private final StationRepository stationRepository;
    private final StationRedisRepository stationRedisRepository;

    @Transactional
    public int uploadStationsFromFile(MultipartFile file, LocalDateTime requestedAt) {
        List<Station> readStations = stationFileReader.readStationsFromFile(file);
        List<Station> savedStations = stationRepository.upsert(readStations);

        stationRedisRepository.saveStationsWithPipeline(savedStations, requestedAt);

        return savedStations.size();
    }

    @Transactional
    public void saveStations(List<Station> loadedStations, LocalDateTime requestedAt) {
        log.info("총 {}개의 대여소 정보를 성공적으로 수집했습니다.", loadedStations.size());

        List<Station> savedStations = stationRepository.upsert(loadedStations);
        log.info("{}개의 대여소 정보를 DB에 저장했습니다. ", savedStations.size());

        stationRedisRepository.saveStationsWithPipeline(savedStations, requestedAt);
    }

    @Transactional(readOnly = true)
    public List<MapStationResponse> findNearbyStations(double centerLat, double centerLon,
                                                       double latDelta, double lonDelta) {
        double height = latDelta * 2;
        double width = lonDelta * 2;

        return stationRedisRepository.findNearbyStations(centerLat, centerLon, height, width);
    }

    @Transactional(readOnly = true)
    public List<StationSimpleResponse> searchStationsByName(String keyword, double centerLat, double centerLon) {
        List<Station> searchedStations = stationRepository.findByNameContaining(keyword);

        return searchedStations.stream()
                .map(station -> new StationSimpleResponse(
                        station.getStationId(),
                        station.getNumber(),
                        station.getName(),
                        station.getLatitude(),
                        station.getLongitude(),
                        station.getAddress(),
                        station.calculateDistanceTo(centerLat, centerLon)
                ))
                .sorted(Comparator.comparingDouble(StationSimpleResponse::distance))
                .toList();
    }

    @Transactional(readOnly = true)
    public StationDetailResponse findStationDetail(Long stationId) {
        Station station = stationRepository.findStationById(stationId);

        List<Integer> nearbyStationsNumber =
                MapStationResponse.extractAvailableNumbers(
                        stationRedisRepository.findNearbyStations(station.getLatitude(), station.getLongitude(), 1, 1),
                        station.getNumber()
                );

        List<Station> nearbyStations = stationRepository.findByNumbers(nearbyStationsNumber);

        return StationDetailResponse.fromStation(station, nearbyStations);
    }
}
