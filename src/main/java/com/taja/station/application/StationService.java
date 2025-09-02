package com.taja.station.application;

import com.taja.station.domain.Station;
import java.time.LocalDateTime;
import java.util.List;
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

    @Transactional
    public int uploadStationData(MultipartFile file) {
        List<Station> readStations = stationFileReader.readStationsFromFile(file);
        int savedCount = stationRepository.upsert(readStations);

        saveStationsToRedis(readStations);

        return savedCount;
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
