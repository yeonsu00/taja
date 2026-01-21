package com.taja.application.statistics;

import com.taja.application.statistics.dto.StationDistricts;
import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.application.statistics.dto.StationTempGroup;
import com.taja.domain.statistics.TemperatureStatistics;
import com.taja.domain.station.Station;
import com.taja.application.status.StationStatusService;
import com.taja.domain.status.StationStatus;
import com.taja.application.weather.WeatherService;
import java.time.LocalDate;
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
public class TemperatureStatisticsBatchService {

    private final StationStatusService stationStatusService;
    private final TemperatureStatisticsService temperatureStatisticsService;
    private final WeatherService weatherService;

    @Transactional
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
