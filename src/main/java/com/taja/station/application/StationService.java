package com.taja.station.application;

import com.taja.bikeapi.application.BikeApiBatchFetcher;
import com.taja.bikeapi.application.BikeApiClient;
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
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationFileReader stationFileReader;
    private final StationRepository stationRepository;
    private final StationRedisRepository stationRedisRepository;
    private final BikeApiClient bikeApiClient;
    private final BikeApiBatchFetcher bikeApiBatchFetcher;

    private static final int TOTAL_COUNT = 3500;
    private static final int BATCH_SIZE = 500;

    @Transactional
    public int uploadStations(MultipartFile file) {
        List<Station> readStations = stationFileReader.readStationsFromFile(file);
        int savedCount = stationRepository.upsert(readStations);

        saveStationsToRedis(readStations);

        return savedCount;
    }

    @Transactional
    public void loadStations() {
        bikeApiBatchFetcher.fetchAll(TOTAL_COUNT, BATCH_SIZE, bikeApiClient::fetchStations)
                .subscribe(loadedStations -> {
                    log.info("총 {}개의 대여소 정보를 성공적으로 수집했습니다.", loadedStations.size());

                    List<Station> stations = loadedStations.stream()
                            .map(StationDto::toStation)
                            .flatMap(Optional::stream)
                            .toList();

                    int savedStationCount = stationRepository.upsert(stations);
                    log.info("{}개의 대여소 실시간 상태를 저장했습니다. ", savedStationCount);

                    saveStationsToRedis(stations);
                });
    }

    private void saveStationsToRedis(List<Station> readStations) {
        LocalDateTime redisSaveTime = LocalDateTime.now();

        Flux.fromIterable(readStations)
                .flatMap(station -> stationRedisRepository.saveStation(station, redisSaveTime)
                        .doOnError(e -> log.error("Redis 저장 실패: {}", station.getNumber(), e))
                )
                .collectList()
                .doOnSuccess(list -> log.info("Redis 저장 완료! 총 {}개 저장됨.", list.size()))
                .subscribe();
    }
}
