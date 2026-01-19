package com.taja.statistics.application;

import com.taja.statistics.application.dto.StationDistricts;
import com.taja.statistics.application.dto.StationHourlyAvg;
import com.taja.statistics.application.dto.StationTempGroup;
import com.taja.statistics.domain.TemperatureStatistics;
import com.taja.station.domain.Station;
import com.taja.status.application.StationStatusService;
import com.taja.status.domain.StationStatus;
import com.taja.weather.application.WeatherService;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemperatureStatisticsBatchService {

    private final StationStatusService stationStatusService;
    private final TemperatureStatisticsService temperatureStatisticsService;
    private final WeatherService weatherService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processBatch(List<Station> batchStations, LocalDate calculationDate,
                             Map<String, Map<Integer, Double>> districtHourlyTempMap,
                             StationDistricts stationDistricts) {
        List<Long> stationIds = Station.toStationIds(batchStations);

        log.info("배치 처리 시작 - 대여소 수: {}", batchStations.size());

        List<StationStatus> stationStatuses = stationStatusService.findStationStatusesByDateAndStationIds(
                calculationDate, stationIds);

        List<StationHourlyAvg> stationHourlyAvgParkingBikeCounts = stationStatusService.calculateHourlyAvgParkingBikeCount(
                stationStatuses);
        List<TemperatureStatistics> temperatureStatistics = temperatureStatisticsService.findStatisticsByStationIds(
                stationIds);
        Map<StationTempGroup, TemperatureStatistics> existingStatsMap = temperatureStatistics.stream()
                .collect(Collectors.toMap(StationTempGroup::from, s -> s));

        List<TemperatureStatistics> toSaveTemperatureStatistics = new ArrayList<>();

        stationHourlyAvgParkingBikeCounts.forEach(stationHourlyAvg -> {
            Long stationId = stationHourlyAvg.stationId();
            String district = stationDistricts.getDistrict(stationId);

            stationHourlyAvg.hourlyAvgParkingBikeCounts().forEach((hour, avgBikeCount) -> {
                Double temperature = weatherService.getTemperature(districtHourlyTempMap, district, hour);
                if (temperature == null) {
                    log.warn("기온 데이터 누락으로 통계 제외 - 자치구: {}, 시간: {}", district, hour);
                    return;
                }

                Double tempRange = TemperatureStatistics.getTemperatureRangeStart(temperature);
                StationTempGroup group = new StationTempGroup(stationId, tempRange);

                if (existingStatsMap.containsKey(group)) {
                    TemperatureStatistics stats = existingStatsMap.get(group);
                    stats.updateAvgParkingBikeCount(avgBikeCount);
                    toSaveTemperatureStatistics.add(stats);
                } else {
                    TemperatureStatistics newStats = TemperatureStatistics.create(
                            stationId,
                            tempRange,
                            avgBikeCount
                    );
                    toSaveTemperatureStatistics.add(newStats);
                    existingStatsMap.put(group, newStats);
                }
            });
        });

        temperatureStatisticsService.saveStatistics(toSaveTemperatureStatistics);
        log.info("배치 처리 완료 - 신규: {}개, 업데이트: {}개",
                toSaveTemperatureStatistics.size() - temperatureStatistics.size(), temperatureStatistics.size());
    }
}
