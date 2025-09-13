package com.taja.station.application;

import com.taja.bikeapi.application.dto.station.StationDto;
import com.taja.station.domain.Station;
import com.taja.station.presentation.response.NearbyStationResponse;
import com.taja.station.presentation.response.SearchStationResponse;
import com.taja.station.presentation.response.detail.StationDetailResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
        int savedStationCount = stationRepository.upsert(readStations);

        saveStationsToRedis(readStations, requestedAt);

        return savedStationCount;
    }

    @Transactional
    public void saveStations(List<StationDto> loadedStations, LocalDateTime requestedAt) {
        log.info("총 {}개의 대여소 정보를 성공적으로 수집했습니다.", loadedStations.size());

        List<Station> stations = loadedStations.stream()
                .map(StationDto::toStation)
                .flatMap(Optional::stream)
                .toList();

        int savedStationCount = stationRepository.upsert(stations);
        log.info("{}개의 대여소 정보를 DB에 저장했습니다. ", savedStationCount);

        saveStationsToRedis(stations, requestedAt);
    }

    @Transactional(readOnly = true)
    public List<NearbyStationResponse> findNearbyStations(double centerLat, double centerLon,
                                                          double latDelta, double lonDelta) {
        double height = latDelta * 2;
        double width = lonDelta * 2;

        return stationRedisRepository.findNearbyStations(centerLat, centerLon, height, width);
    }

    private void saveStationsToRedis(List<Station> readStations, LocalDateTime requestedAt) {
        int redisUpdatedCount = 0;

        for (Station station : readStations) {
            if (stationRedisRepository.saveStation(station, requestedAt)) {
                redisUpdatedCount++;
            }
        }

        log.info("Redis 대여소 정보 총 {}개 업데이트됨.", redisUpdatedCount);
    }

    @Transactional(readOnly = true)
    public List<SearchStationResponse> searchStationsByName(String keyword, double centerLat, double centerLon) {
        List<Station> searchedStations = stationRepository.findByNameContaining(keyword);

        return searchedStations.stream()
                .map(station -> new SearchStationResponse(
                        station.getStationId(),
                        station.getNumber(),
                        station.getName(),
                        station.getLatitude(),
                        station.getLongitude(),
                        station.getAddress(),
                        station.calculateDistanceTo(centerLat, centerLon)
                ))
                .sorted(Comparator.comparingDouble(SearchStationResponse::distance))
                .toList();
    }

    @Transactional(readOnly = true)
    public StationDetailResponse findStationDetail(int stationNumber) {
        Station station = stationRepository.findStationByNumber(stationNumber);

        return StationDetailResponse.fromStation(station);
    }
}
