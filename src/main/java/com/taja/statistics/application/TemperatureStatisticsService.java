package com.taja.statistics.application;

import com.taja.statistics.domain.TemperatureStatistics;
import com.taja.statistics.infra.TemperatureStatisticsEntity;
import com.taja.status.domain.StationStatus;
import com.taja.weather.domain.WeatherHistory;
import java.util.ArrayList;
import java.util.HashMap;
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
public class TemperatureStatisticsService {

    private final TemperatureStatisticsRepository temperatureStatisticsRepository;

    @Transactional
    public void calculateAndSave(Long stationId, String district, List<StationStatus> statuses,
                                 List<WeatherHistory> allWeatherHistories) {
        log.debug("기온별 통계 계산 시작 - stationId: {}, district: {}, 데이터 수: {}", 
                stationId, district, statuses.size());

        // 해당 자치구의 날씨 정보만 필터링
        Map<Integer, Double> temperatureByHour = allWeatherHistories.stream()
                .filter(weather -> weather.getDistrict().equals(district))
                .collect(Collectors.toMap(
                        weather -> weather.getBaseTime().getHour(),
                        WeatherHistory::getTemperature,
                        (existing, replacement) -> existing // 중복 시 기존 값 유지
                ));

        if (temperatureByHour.isEmpty()) {
            log.warn("날씨 정보가 없습니다 - stationId: {}, district: {}", stationId, district);
            return;
        }

        // 시간대별로 그룹화
        Map<Integer, List<StationStatus>> statusesByHour = statuses.stream()
                .collect(Collectors.groupingBy(status -> status.getRequestedAt().getHour()));

        // 온도 구간별로 평균 계산 (온도 구간 -> parkingBikeCount 리스트)
        Map<Double, List<Integer>> parkingCountsByTempRange = new HashMap<>();

        for (Map.Entry<Integer, List<StationStatus>> entry : statusesByHour.entrySet()) {
            Integer hour = entry.getKey();
            List<StationStatus> hourlyStatuses = entry.getValue();
            
            Double temperature = temperatureByHour.get(hour);
            if (temperature == null) {
                continue;
            }

            // 온도 구간 시작값 계산 (예: 3도 -> 0.0, 7도 -> 5.0)
            Double tempRangeStart = TemperatureStatistics.getTemperatureRangeStart(temperature);

            // 해당 시간대의 평균 parkingBikeCount
            int avgCount = (int) Math.round(
                    hourlyStatuses.stream()
                            .mapToInt(StationStatus::getParkingBikeCount)
                            .average()
                            .orElse(0.0)
            );

            parkingCountsByTempRange.computeIfAbsent(tempRangeStart, k -> new ArrayList<>()).add(avgCount);
        }

        // 온도 구간별로 통계 저장
        List<TemperatureStatisticsEntity> toSave = new ArrayList<>();
        int updatedCount = 0;

        for (Map.Entry<Double, List<Integer>> entry : parkingCountsByTempRange.entrySet()) {
            Double tempRangeStart = entry.getKey();
            List<Integer> parkingCounts = entry.getValue();

            // 온도 구간의 평균 계산
            int avgParkingBikeCount = (int) Math.round(
                    parkingCounts.stream()
                            .mapToInt(Integer::intValue)
                            .average()
                            .orElse(0.0)
            );

            // 기존 통계 조회
            TemperatureStatisticsEntity existing = temperatureStatisticsRepository
                    .findByStationIdAndTemperatureRange(stationId, tempRangeStart);

            if (existing != null) {
                // 기존 통계 업데이트
                TemperatureStatistics updated = existing.toDomain().updateAverage(avgParkingBikeCount);
                existing.update(updated);
                updatedCount++;
            } else {
                // 새 통계 생성
                TemperatureStatistics newStats = TemperatureStatistics.create(
                        stationId, tempRangeStart, avgParkingBikeCount);
                toSave.add(TemperatureStatisticsEntity.fromDomain(newStats));
            }
        }

        // 새로 생성된 통계만 저장
        if (!toSave.isEmpty()) {
            temperatureStatisticsRepository.saveAll(toSave);
        }
        
        log.debug("기온별 통계 저장 완료 - stationId: {}, 신규: {}개, 업데이트: {}개", 
                stationId, toSave.size(), updatedCount);
    }
}

