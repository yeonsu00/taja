package com.taja.application.station;

import com.taja.application.station.event.EventPublisherHelper;
import com.taja.application.station.event.StationEvent;
import com.taja.domain.station.Station;
import com.taja.interfaces.api.station.response.StationSimpleResponse;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationService {

    private final StationFileReader stationFileReader;
    private final StationClient stationClient;
    private final StationRepository stationRepository;
    private final TransactionTemplate transactionTemplate;
    private final EventPublisherHelper eventPublisherHelper;

    private static final int TOTAL_COUNT = 3500;
    private static final int ITEMS_PER_REQUEST = 500;

    public List<Station> uploadStationsFromFile(MultipartFile file) {
        List<Station> readStations = stationFileReader.readStationsFromFile(file);
        return stationRepository.upsert(readStations);
    }

    @Transactional(readOnly = true)
    public List<StationSimpleResponse> searchStationsByName(String keyword, double centerLat, double centerLng) {
        List<Station> searchedStations = stationRepository.findByNameContaining(keyword);

        return searchedStations.stream()
                .map(station -> new StationSimpleResponse(
                        station.getStationId(),
                        station.getNumber(),
                        station.getName(),
                        station.getLatitude(),
                        station.getLongitude(),
                        station.getAddress(),
                        station.calculateDistanceTo(centerLat, centerLng)
                ))
                .sorted(Comparator.comparingDouble(StationSimpleResponse::distance))
                .toList();
    }

    public Station findStationByStationId(Long stationId) {
        return stationRepository.findStationById(stationId);
    }

    public Map<Long, Station> findStationMapByIds(List<Long> stationIds) {
        return stationRepository.findStationsByIds(stationIds).stream()
                .collect(Collectors.toMap(Station::getStationId, s -> s));
    }

    public List<Station> findStationByNumbers(List<Integer> stationNumbers) {
        return stationRepository.findByNumbers(stationNumbers);
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
            transactionTemplate.execute(status -> {
                List<Station> savedStations = stationRepository.upsert(loadedStations);
                log.info("배치 저장 완료 ({}-{}): {}개의 대여소 정보 저장", startIndex, endIndex, savedStations.size());
                eventPublisherHelper.publishEventAfterCommit(new StationEvent.StationsSaved(savedStations, requestedAt));
                return null;
            });
        }
    }
}
