package com.taja.application.status;

import com.taja.infrastructure.client.bike.dto.status.StationStatusDto;
import com.taja.application.statistics.dto.StationDailyAvg;
import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.application.station.StationRedisRepository;
import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStatusService {

    private final StationStatusRepository stationStatusRepository;
    private final StationRedisRepository stationRedisRepository;
    private final TransactionTemplate transactionTemplate;
    private final StatusClient statusClient;

//    private static final int ITEMS_PER_REQUEST = 500;
//    private static final int TOTAL_PAGES = 6;
    private static final int TOTAL_COUNT = 3500;
    private static final int ITEMS_PER_REQUEST = 500;

    public List<StationStatus> findStationStatusesByDate(LocalDate calculationDate) {
        return stationStatusRepository.findByDate(calculationDate);
    }

    public List<StationStatus> findStationStatusesByDateAndStationIds(LocalDate calculationDate, List<Long> stationIds) {
        return stationStatusRepository.findAllByDateAndStationIds(calculationDate, stationIds);
    }

    public List<StationHourlyAvg> calculateHourlyAvgParkingBikeCount(List<StationStatus> stationStatuses) {
        Map<Long, Map<Integer, Integer>> groupedMap = stationStatuses.stream()
                .collect(Collectors.groupingBy(
                        StationStatus::getStationId,
                        Collectors.groupingBy(
                                status -> status.getRequestedTime().getHour(),
                                Collectors.collectingAndThen(
                                        Collectors.averagingInt(StationStatus::getParkingBikeCount),
                                        avg -> (int) Math.round(avg)
                                )
                        )
                ));

        return groupedMap.entrySet().stream()
                .map(entry -> new StationHourlyAvg(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<StationDailyAvg> calculateDailyAvgParkingBikeCount(List<StationStatus> stationStatuses) {
        Map<Long, Integer> groupedMap = stationStatuses.stream()
                .collect(Collectors.groupingBy(
                        StationStatus::getStationId,
                        Collectors.collectingAndThen(
                                Collectors.averagingInt(StationStatus::getParkingBikeCount),
                                avg -> (int) Math.round(avg)
                        )
                ));

        return groupedMap.entrySet().stream()
                .map(entry -> new StationDailyAvg(entry.getKey(), entry.getValue()))
                .toList();
    }

    public void loadStationStatuses(LocalDateTime requestedAt) {
        int totalPages = (int) Math.ceil((double) TOTAL_COUNT / ITEMS_PER_REQUEST);

        Flux.range(0, totalPages)
                .flatMap(page -> {
                    int start = (page * ITEMS_PER_REQUEST) + 1;
                    int end = (page + 1) * ITEMS_PER_REQUEST;

                    if (end > TOTAL_COUNT) {
                        end = TOTAL_COUNT;
                    }

                    return statusClient.fetchStationStatuses(start, end);
                })
                .publishOn(Schedulers.boundedElastic())
                .doOnNext(stationStatusDtos -> {
                    if (!stationStatusDtos.isEmpty()) {
                        saveInTransaction(stationStatusDtos, requestedAt);
                    }
                })
                .doOnError(e -> log.error("대여소 상태 업데이트 프로세스 진행 중 오류 발생: {}", e.getMessage()))
                .subscribe();
    }

    private void saveInTransaction(List<StationStatusDto> loadedStationStatuses, LocalDateTime requestedAt) {
        transactionTemplate.execute(status -> {
            List<StationStatus> stationStatuses = loadedStationStatuses.stream()
                    .map(dto -> dto.toStationStatus(requestedAt))
                    .toList();

            int savedStationStatusCount = stationStatusRepository.saveAll(stationStatuses);
            log.info("{}개의 대여소 실시간 상태를 DB에 저장했습니다. 요청 시간: {}", savedStationStatusCount, requestedAt);
            stationRedisRepository.updateBikeCountAndRequestedAtWithPipeline(stationStatuses);
            return null;
        });
    }
}
