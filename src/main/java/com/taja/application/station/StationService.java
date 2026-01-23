package com.taja.application.station;

import com.taja.domain.station.Station;
import com.taja.interfaces.api.station.response.MapStationResponse;
import com.taja.interfaces.api.station.response.StationSimpleResponse;
import com.taja.interfaces.api.station.response.detail.StationDetailResponse;
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
    private final StationClient stationClient;
    private final StationRepository stationRepository;
    private final StationRedisRepository stationRedisRepository;

    private static final int TOTAL_COUNT = 3500;
    private static final int ITEMS_PER_REQUEST = 500;

    @Transactional
    public int uploadStationsFromFile(MultipartFile file, LocalDateTime requestedAt) {
        List<Station> readStations = stationFileReader.readStationsFromFile(file);
        List<Station> savedStations = stationRepository.upsert(readStations);

        stationRedisRepository.saveStationsWithPipeline(savedStations, requestedAt);

        return savedStations.size();
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

    public List<Station> findAllStations() {
        return stationRepository.findAll();
    }

    public void loadStations(LocalDateTime requestedAt) {
        int pageCount = (int) Math.ceil((double) TOTAL_COUNT / ITEMS_PER_REQUEST);

        for (int page = 0; page < pageCount; page++) {
            int startIndex = 1 + (page * ITEMS_PER_REQUEST);
            int endIndex = startIndex + ITEMS_PER_REQUEST - 1;

            List<Station> loadedStations = stationClient.fetchStationInfos(startIndex, endIndex);

            //저장 -> 트랜잭션 처리 필요
            List<Station> savedStations = stationRepository.upsert(loadedStations);
            stationRedisRepository.saveStationsWithPipeline(savedStations, requestedAt);
            log.info("배치 저장 완료 ({}-{}): {}개의 대여소 정보 저장", startIndex, endIndex, savedStations.size());
        }
    }
}
