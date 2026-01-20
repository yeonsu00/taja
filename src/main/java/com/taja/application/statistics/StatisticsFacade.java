package com.taja.application.statistics;

import com.taja.application.station.StationService;
import com.taja.domain.station.Station;
import com.taja.application.statistics.dto.StationDailyAvg;
import com.taja.application.statistics.dto.StationDistricts;
import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.application.status.StationStatusService;
import com.taja.domain.status.StationStatus;
import com.taja.application.weather.WeatherService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsFacade {

    private final StationStatusService stationStatusService;
    private final HourlyStatisticsService hourlyStatisticsService;
    private final DayOfWeekStatisticsService dayOfWeekStatisticsService;
    private final TemperatureStatisticsBatchService temperatureStatisticsBatchService;
    private final WeatherService weatherService;
    private final StationService stationService;

    private static final int BATCH_SIZE = 100;

    public int calculateHourlyStatistics(LocalDate requestedAt) {
        LocalDate calculationDate = getCalculationDate(requestedAt);

        List<StationStatus> stationStatuses = stationStatusService.findStationStatusesByDate(calculationDate);
        List<StationHourlyAvg> stationHourlyAvgParkingBikeCounts = stationStatusService.calculateHourlyAvgParkingBikeCount(
                stationStatuses);

        log.info("시간대별 통계 변환 완료 - 대여소 수: {}", stationHourlyAvgParkingBikeCounts.size());

        int totalUpdated = 0;
        for (int i = 0; i < stationHourlyAvgParkingBikeCounts.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, stationHourlyAvgParkingBikeCounts.size());
            List<StationHourlyAvg> batchStationHourlyAvgParkingBikeCounts = stationHourlyAvgParkingBikeCounts.subList(i,
                    endIndex);

            totalUpdated += hourlyStatisticsService.processBatch(batchStationHourlyAvgParkingBikeCounts);
        }

        log.info("시간대별 통계 저장 완료 - 총 {}건 처리", totalUpdated);
        return totalUpdated;
    }

    @Transactional
    public int calculateDayOfWeekStatistics(LocalDate requestedAt) {
        LocalDate calculationDate = getCalculationDate(requestedAt);
        DayOfWeek dayOfWeek = calculationDate.getDayOfWeek();

        List<StationStatus> stationStatuses = stationStatusService.findStationStatusesByDate(calculationDate);
        List<StationDailyAvg> stationDailyAvgParkingBikeCounts = stationStatusService.calculateDailyAvgParkingBikeCount(
                stationStatuses);

        int totalUpdated = 0;
        for (int i = 0; i < stationDailyAvgParkingBikeCounts.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, stationDailyAvgParkingBikeCounts.size());
            List<StationDailyAvg> batchStationDailyAvgParkingBikeCounts = stationDailyAvgParkingBikeCounts.subList(i,
                    endIndex);

            totalUpdated += dayOfWeekStatisticsService.processBatch(dayOfWeek, batchStationDailyAvgParkingBikeCounts);
        }

        log.info("요일별 통계 계산 완료 - 총 {}건 처리", totalUpdated);
        return totalUpdated;
    }

    public int calculateTemperatureStatistics(LocalDate requestedAt) {
        LocalDate calculationDate = getCalculationDate(requestedAt);

        Map<String, Map<Integer, Double>> districtHourlyTempMap = weatherService.findWeathersByBaseDate(
                calculationDate);

        List<Station> stations = stationService.findAllStations();
        StationDistricts stationDistricts = StationDistricts.from(stations);

        int totalProcessed = 0;

        for (int i = 0; i < stations.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, stations.size());
            List<Station> batchStations = stations.subList(i, endIndex);

            temperatureStatisticsBatchService.processBatch(
                    batchStations, calculationDate, districtHourlyTempMap, stationDistricts);
            totalProcessed += batchStations.size();
        }

        log.info("기온별 통계 계산 완료 - 총 {}개 대여소 처리", totalProcessed);
        return totalProcessed;
    }


    private static LocalDate getCalculationDate(LocalDate requestedAt) {
        return requestedAt.minusDays(1);
    }
}
