package com.taja.application.status;

import com.taja.infrastructure.api.bike.dto.status.StationStatusDto;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStatusService {

    private final StationStatusRepository stationStatusRepository;
    private final StationRedisRepository stationRedisRepository;

    @Transactional
    public void saveStationStatuses(LocalDateTime requestedAt, List<StationStatusDto> loadedStationStatuses) {
        log.info("총 {}개의 대여소 실시간 상태를 성공적으로 수집했습니다.", loadedStationStatuses.size());

        List<StationStatus> stationStatuses = loadedStationStatuses.stream()
                .map(dto -> dto.toStationStatus(requestedAt))
                .toList();

        int savedStationStatusCount = stationStatusRepository.saveAll(stationStatuses);
        log.info("{}개의 대여소 실시간 상태를 DB에 저장했습니다.", savedStationStatusCount);
        stationRedisRepository.updateBikeCountAndRequestedAtWithPipeline(stationStatuses);
    }

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
}
