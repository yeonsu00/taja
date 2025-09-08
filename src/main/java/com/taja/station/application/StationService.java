package com.taja.station.application;

import com.taja.bikeapi.application.dto.station.StationDto;
import com.taja.station.domain.Station;
import java.time.LocalDateTime;
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
    public void findNearbyStations(double latitude, double longitude) {
//        stationRedisRepository.findNearbyStations(latitude, longitude);
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
}
