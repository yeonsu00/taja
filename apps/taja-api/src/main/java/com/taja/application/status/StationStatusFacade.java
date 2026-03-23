package com.taja.application.status;

import com.taja.application.statistics.HourlyStatisticsService;
import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.domain.statistics.HourlyStatistics;
import com.taja.interfaces.api.station.response.detail.BikeCountByTimeResponse;
import com.taja.interfaces.api.station.response.detail.TodayAvailableBikeResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class StationStatusFacade {

    private final StationStatusHourlyAvgService stationStatusHourlyAvgService;
    private final HourlyStatisticsService hourlyStatisticsService;

    @Transactional(readOnly = true)
    public TodayAvailableBikeResponse getTodayAvailableBike(Long stationId, int stationNumber, LocalDateTime requestedAt) {
        LocalDateTime timestamp = requestedAt.withMinute(0).withSecond(0).withNano(0);
        int currentHour = requestedAt.getHour();
        LocalDate today = requestedAt.toLocalDate();

        List<BikeCountByTimeResponse> observedBikeCountByHour = getObservedBikeCountByHour(stationNumber, today, currentHour);
        List<BikeCountByTimeResponse> predictedBikeCountByHour = getPredictedBikeCountByHour(stationId, currentHour);

        return new TodayAvailableBikeResponse(timestamp, observedBikeCountByHour, predictedBikeCountByHour);
    }

    private List<BikeCountByTimeResponse> getObservedBikeCountByHour(int stationNumber, LocalDate today, int currentHour) {
        List<StationHourlyAvg> stationHourlyAvgs = stationStatusHourlyAvgService
                .findStationHourlyAvgsByDateAndStationNumbers(today, List.of(stationNumber));

        Map<Integer, Integer> observedByHour = stationHourlyAvgs.stream()
                .findFirst()
                .map(StationHourlyAvg::hourlyAvgParkingBikeCounts)
                .orElse(Map.of())
                .entrySet().stream()
                .filter(e -> e.getKey() <= currentHour)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a));

        List<BikeCountByTimeResponse> result = new ArrayList<>();
        for (int hour = 0; hour <= currentHour; hour++) {
            int bikeCount = observedByHour.getOrDefault(hour, 0);
            result.add(new BikeCountByTimeResponse(hour, bikeCount));
        }
        return result;
    }

    private List<BikeCountByTimeResponse> getPredictedBikeCountByHour(Long stationId, int currentHour) {
        List<HourlyStatistics> hourlyStats = hourlyStatisticsService.findByStationIds(List.of(stationId));
        Map<Integer, Integer> predictedByHour = hourlyStats.stream()
                .collect(Collectors.toMap(HourlyStatistics::getBaseHour,
                        stats -> stats.getAvgParkingBikeCount() != null ? stats.getAvgParkingBikeCount() : 0,
                        (a, b) -> a));

        List<BikeCountByTimeResponse> result = new ArrayList<>();
        for (int hour = currentHour + 1; hour <= 23; hour++) {
            int bikeCount = predictedByHour.getOrDefault(hour, 0);
            result.add(new BikeCountByTimeResponse(hour, bikeCount));
        }
        return result;
    }
}
