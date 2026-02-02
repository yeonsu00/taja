package com.taja.application.statistics;

import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.domain.statistics.HourlyStatistics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HourlyStatisticsService {

    private final HourlyStatisticsRepository hourlyStatisticsRepository;

    public List<HourlyStatistics> findByStationIds(List<Long> stationIds) {
        return hourlyStatisticsRepository.findAllByStationIds(stationIds);
    }

    @Transactional
    public int processBatch(List<StationHourlyAvg> stationHourlyAvgParkingBikeCounts) {
        if (stationHourlyAvgParkingBikeCounts == null || stationHourlyAvgParkingBikeCounts.isEmpty()) {
            log.info("배치 처리할 데이터가 없습니다.");
            return 0;
        }

        List<Long> stationIds = stationHourlyAvgParkingBikeCounts.stream()
                .map(StationHourlyAvg::stationId)
                .toList();

        List<HourlyStatistics> existingStatistics = hourlyStatisticsRepository.findAllByStationIds(stationIds);

        Map<String, HourlyStatistics> existingMap = new HashMap<>();
        for (HourlyStatistics stats : existingStatistics) {
            String key = createKey(stats.getStationId(), stats.getBaseHour());
            existingMap.put(key, stats);
        }

        List<HourlyStatistics> toSaveHourlyStatistics = new ArrayList<>();
        int updatedCount = 0;

        for (StationHourlyAvg stationHourlyAvg : stationHourlyAvgParkingBikeCounts) {
            Long stationId = stationHourlyAvg.stationId();
            Map<Integer, Integer> hourlyAverages = stationHourlyAvg.hourlyAvgParkingBikeCounts();

            for (Map.Entry<Integer, Integer> hourEntry : hourlyAverages.entrySet()) {
                Integer hour = hourEntry.getKey();
                Integer avgCount = hourEntry.getValue();

                String key = createKey(stationId, hour);
                HourlyStatistics existingHourlyStatistics = existingMap.get(key);

                if (existingHourlyStatistics == null) {
                    HourlyStatistics newHourlyStatistics = HourlyStatistics.create(stationId, hour, avgCount);
                    toSaveHourlyStatistics.add(newHourlyStatistics);
                } else {
                    existingHourlyStatistics.updateAvgParkingBikeCount(avgCount);
                    updatedCount++;
                }
            }
        }

        if (!toSaveHourlyStatistics.isEmpty()) {
            hourlyStatisticsRepository.saveAllHourlyStatistics(toSaveHourlyStatistics);
        }

        log.info("시간대별 통계 배치 처리 완료 - 신규: {}개, 업데이트: {}개", toSaveHourlyStatistics.size(), updatedCount);
        return toSaveHourlyStatistics.size() + updatedCount;
    }

    private String createKey(Long stationId, Integer hour) {
        return stationId + ":" + hour;
    }

    public List<HourlyStatistics> findHourlyStatisticsByStationId(Long stationId) {
        return hourlyStatisticsRepository.findHourlyStatisticsByStationId(stationId);
    }
}

