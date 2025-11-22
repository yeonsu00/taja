package com.taja.statistics.application;

import com.taja.status.application.StationStatusService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final StationStatusService stationStatusService;
    private final HourlyStatisticsService hourlyStatisticsService;
    private final DayOfWeekStatisticsService dayOfWeekStatisticsService;
    private final TemperatureStatisticsService temperatureStatisticsService;

    @Transactional
    public int calculateHourlyStatistics(LocalDate requestedAt) {
        LocalDate calculationDate = getCalculationDate(requestedAt);

        Map<Long, Map<Integer, Integer>> stationHourlyAverages = stationStatusService.findStationHourlyAverage(calculationDate);
        log.info("시간대별 통계 변환 완료 - 대여소 수: {}", stationHourlyAverages.size());

        int totalUpdated = 0;
        for (Long stationId : stationHourlyAverages.keySet()) {
            Map<Integer, Integer> hourlyAverages = stationHourlyAverages.get(stationId);

            for (Map.Entry<Integer, Integer> hourEntry : hourlyAverages.entrySet()) {
                Integer hour = hourEntry.getKey();
                Integer avgCount = hourEntry.getValue();

                hourlyStatisticsService.upsertHourlyStatistics(stationId, hour, avgCount);
                totalUpdated++;
            }
        }

        return totalUpdated;
    }

    @Transactional
    public int calculateDayOfWeekStatistics(LocalDate requestedAt) {
        LocalDate calculationDate = getCalculationDate(requestedAt);
        DayOfWeek dayOfWeek = calculationDate.getDayOfWeek();

        log.info("요일별 통계 계산 시작 - 대상 날짜: {}, 요일: {}", calculationDate, dayOfWeek);

        Map<Long, Integer> stationDailyAverages = stationStatusService.findStationDailyAverage(calculationDate);
        log.info("요일별 통계 조회 완료 - 대여소 수: {}", stationDailyAverages.size());

        int totalUpdated = 0;
        for (Map.Entry<Long, Integer> entry : stationDailyAverages.entrySet()) {
            Long stationId = entry.getKey();
            Integer avgCount = entry.getValue();

            dayOfWeekStatisticsService.upsertDayOfWeekStatistics(stationId, dayOfWeek, avgCount);
            totalUpdated++;
        }

        log.info("요일별 통계 계산 완료 - 총 {}건 처리", totalUpdated);
        return totalUpdated;
    }

    @Transactional
    public int calculateTemperatureStatistics(LocalDate requestedAt) {
        LocalDate calculationDate = getCalculationDate(requestedAt);

        return 0;
    }

    private static LocalDate getCalculationDate(LocalDate requestedAt) {
        LocalDate calculationDate = requestedAt.minusDays(1);

        log.info("시간대별 통계 계산 시작 - 대상 날짜: {}", calculationDate);
        return calculationDate;
    }

//    @Transactional
//    public int calculateStatistics(
//            LocalDate yesterday,
//            List<StationStatus> yesterdayStatuses,
//            List<WeatherHistory> yesterdayWeathers,
//            Map<Integer, Long> stationNumberToIdMap,
//            Map<Long, String> stationIdToDistrictMap
//    ) {
//        Map<Long, List<StationStatus>> statusesByStation = yesterdayStatuses.stream()
//                .filter(status -> stationNumberToIdMap.containsKey(status.getStationNumber()))
//                .collect(Collectors.groupingBy(status -> stationNumberToIdMap.get(status.getStationNumber())));
//
//        log.info("대여소별 그룹화 완료 - {} 개 대여소", statusesByStation.size());
//
//        int updatedStationCount = 0;
//        for (Map.Entry<Long, List<StationStatus>> entry : statusesByStation.entrySet()) {
//            Long stationId = entry.getKey();
//            List<StationStatus> statuses = entry.getValue();
//            String district = stationIdToDistrictMap.get(stationId);
//
//            hourlyStatisticsService.calculateAndSave(stationId, statuses);
//            dayOfWeekStatisticsService.calculateAndSave(stationId, yesterday.getDayOfWeek(), statuses);
//            temperatureStatisticsService.calculateAndSave(stationId, district, statuses, yesterdayWeathers);
//
//            updatedStationCount++;
//        }
//
//        return updatedStationCount;
//    }
}
