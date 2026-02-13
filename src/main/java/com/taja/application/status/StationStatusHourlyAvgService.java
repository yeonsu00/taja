package com.taja.application.status;

import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.application.station.StationRepository;
import com.taja.domain.status.StationStatusHourlyAvg;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
public class StationStatusHourlyAvgService {

    private final StationStatusRepository stationStatusRepository;
    private final StationStatusHourlyAvgRepository stationStatusHourlyAvgRepository;
    private final StationRepository stationRepository;

    @Transactional
    public void updateHourlyAvgByRequestedAt(LocalDateTime requestedAt) {
        List<StationStatus> stationStatuses = stationStatusRepository.findByRequestedAt(requestedAt);
        if (stationStatuses.isEmpty()) {
            log.info("요청 시간에 해당하는 대여소 상태가 없습니다. requestedAt: {}", requestedAt);
            return;
        }

        LocalDate baseDate = requestedAt.toLocalDate();
        int baseHour = requestedAt.getHour();

        List<Integer> stationNumbers = stationStatuses.stream()
                .map(StationStatus::getStationNumber)
                .distinct()
                .toList();

        List<StationStatusHourlyAvg> existing = stationStatusHourlyAvgRepository
                .findAllByBaseDateAndBaseHourAndStationNumbers(baseDate, baseHour, stationNumbers);
        Map<Integer, StationStatusHourlyAvg> existingMap = existing.stream()
                .collect(Collectors.toMap(StationStatusHourlyAvg::getStationNumber, e -> e));

        List<StationStatusHourlyAvg> toSaveHourlyAvgs = new ArrayList<>();

        for (StationStatus status : stationStatuses) {
            Integer stationNumber = status.getStationNumber();
            StationStatusHourlyAvg existingAvg = existingMap.get(stationNumber);

            if (existingAvg != null) {
                existingAvg.updateAvgParkingBikeCount(status.getParkingBikeCount());
                toSaveHourlyAvgs.add(existingAvg);
            } else {
                StationStatusHourlyAvg newAvg = StationStatusHourlyAvg.create(
                        stationNumber,
                        baseDate,
                        baseHour,
                        status.getParkingBikeCount()
                );
                toSaveHourlyAvgs.add(newAvg);
                existingMap.put(stationNumber, newAvg);
            }
        }

        stationStatusHourlyAvgRepository.saveAllHourlyAvgs(toSaveHourlyAvgs);
        log.info("일별시간별 평균 갱신 완료 - requestedAt: {}, 처리 건수: {}", requestedAt, toSaveHourlyAvgs.size());
    }

    public List<StationHourlyAvg> findStationHourlyAvgsByDate(LocalDate baseDate) {
        List<StationStatusHourlyAvg> stationStatusHourlyAvgs =
                stationStatusHourlyAvgRepository.findAllByBaseDate(baseDate);
        return toStationHourlyAvgList(stationStatusHourlyAvgs);
    }

    public List<StationHourlyAvg> findStationHourlyAvgsByDateAndStationNumbers(LocalDate baseDate,
                                                                               List<Integer> stationNumbers) {
        List<StationStatusHourlyAvg> stationStatusHourlyAvgs =
                stationStatusHourlyAvgRepository.findAllByBaseDateAndStationNumbers(baseDate, stationNumbers);
        return toStationHourlyAvgList(stationStatusHourlyAvgs);
    }

    private List<StationHourlyAvg> toStationHourlyAvgList(List<StationStatusHourlyAvg> stationStatusHourlyAvgs) {
        if (stationStatusHourlyAvgs.isEmpty()) {
            return List.of();
        }
        List<Integer> stationNumbers = stationStatusHourlyAvgs.stream()
                .map(StationStatusHourlyAvg::getStationNumber)
                .distinct()
                .toList();
        Map<Integer, Long> numberToStationId = stationRepository.findByNumbers(stationNumbers).stream()
                .collect(Collectors.toMap(Station::getNumber, Station::getStationId));

        Map<Long, Map<Integer, Integer>> groupedByStationId = stationStatusHourlyAvgs.stream()
                .filter(avg -> numberToStationId.containsKey(avg.getStationNumber()))
                .collect(Collectors.groupingBy(
                        avg -> numberToStationId.get(avg.getStationNumber()),
                        Collectors.toMap(StationStatusHourlyAvg::getBaseHour,
                                StationStatusHourlyAvg::getAvgParkingBikeCount, (a, b) -> a)
                ));
        return groupedByStationId.entrySet().stream()
                .map(entry -> new StationHourlyAvg(entry.getKey(), entry.getValue()))
                .toList();
    }
}
