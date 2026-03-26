package com.taja.application.status;

import com.taja.application.station.StationRepository;
import com.taja.application.station.event.StationEvent;
import com.taja.application.statistics.dto.StationDailyAvg;
import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import com.taja.infrastructure.client.bike.dto.status.StationStatusDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStatusService {

    private final StationStatusRepository stationStatusRepository;
    private final StationRepository stationRepository;
    private final TransactionTemplate transactionTemplate;
    private final StatusClient statusClient;
    private final ApplicationEventPublisher eventPublisher;

    private static final int TOTAL_COUNT = 3000;
    private static final int ITEMS_PER_REQUEST = 500;

    public List<StationStatus> findStationStatusesByDate(LocalDate calculationDate) {
        return stationStatusRepository.findByDate(calculationDate);
    }

    public List<StationHourlyAvg> calculateHourlyAvgParkingBikeCount(List<StationStatus> stationStatuses) {
        Map<Integer, Map<Integer, Integer>> groupedByNumber = stationStatuses.stream()
                .collect(Collectors.groupingBy(
                        StationStatus::getStationNumber,
                        Collectors.groupingBy(
                                status -> status.getRequestedTime().getHour(),
                                Collectors.collectingAndThen(
                                        Collectors.averagingInt(StationStatus::getParkingBikeCount),
                                        avg -> (int) Math.round(avg)
                                )
                        )
                ));

        List<Integer> stationNumbers = groupedByNumber.keySet().stream().toList();
        Map<Integer, Long> numberToStationId = stationRepository.findByNumbers(stationNumbers).stream()
                .collect(Collectors.toMap(Station::getNumber, Station::getStationId));

        return groupedByNumber.entrySet().stream()
                .filter(entry -> numberToStationId.containsKey(entry.getKey()))
                .map(entry -> new StationHourlyAvg(numberToStationId.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    public List<StationDailyAvg> calculateDailyAvgParkingBikeCount(List<StationStatus> stationStatuses) {
        Map<Integer, Integer> groupedByNumber = stationStatuses.stream()
                .collect(Collectors.groupingBy(
                        StationStatus::getStationNumber,
                        Collectors.collectingAndThen(
                                Collectors.averagingInt(StationStatus::getParkingBikeCount),
                                avg -> (int) Math.round(avg)
                        )
                ));

        List<Integer> stationNumbers = groupedByNumber.keySet().stream().toList();
        Map<Integer, Long> numberToStationId = stationRepository.findByNumbers(stationNumbers).stream()
                .collect(Collectors.toMap(Station::getNumber, Station::getStationId));

        return groupedByNumber.entrySet().stream()
                .filter(entry -> numberToStationId.containsKey(entry.getKey()))
                .map(entry -> new StationDailyAvg(numberToStationId.get(entry.getKey()), entry.getValue()))
                .toList();
    }

    public void loadStationStatuses(LocalDateTime requestedAt) {
        int totalPages = (int) Math.ceil((double) TOTAL_COUNT / ITEMS_PER_REQUEST);

        Flux.range(0, totalPages)
                .flatMap(page -> {
                    int startIndex = (page * ITEMS_PER_REQUEST) + 1;
                    int endIndex = Math.min((page + 1) * ITEMS_PER_REQUEST, TOTAL_COUNT);
                    return statusClient.fetchStationStatuses(startIndex, endIndex)
                            .map(dtos -> new PageResult(dtos, startIndex, endIndex));
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(result -> {
                    if (!result.dtos().isEmpty()) {
                        saveInTransaction(result.dtos(), requestedAt, result.startIndex(), result.endIndex());
                    }
                })
                .doOnError(e -> log.error("대여소 상태 업데이트 프로세스 진행 중 오류 발생: {}", e.getMessage()))
                .blockLast();

        eventPublisher.publishEvent(StationEvent.StationStatusesCollected.from(requestedAt));
    }

    private void saveInTransaction(List<StationStatusDto> loadedStationStatuses, LocalDateTime requestedAt,
                                   int startIndex, int endIndex) {
        transactionTemplate.execute(status -> {
            List<StationStatus> stationStatuses = loadedStationStatuses.stream()
                    .map(dto -> dto.toStationStatus(requestedAt))
                    .toList();

            int savedStationStatusCount = stationStatusRepository.saveAllStationStatus(stationStatuses);
            log.info("{}개의 대여소 실시간 상태를 DB에 저장했습니다. ({}-{}) 요청 시간: {}",
                    savedStationStatusCount, startIndex, endIndex, requestedAt);
            eventPublisher.publishEvent(StationEvent.StationStatusesUpdated.from(stationStatuses, startIndex, endIndex));
            return null;
        });
    }

    private record PageResult(List<StationStatusDto> dtos, int startIndex, int endIndex) {
    }
}
