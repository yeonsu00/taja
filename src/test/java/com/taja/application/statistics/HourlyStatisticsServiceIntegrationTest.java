package com.taja.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.domain.statistics.HourlyStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class HourlyStatisticsServiceIntegrationTest {

    @Autowired
    private HourlyStatisticsService hourlyStatisticsService;

    @Autowired
    private HourlyStatisticsRepository hourlyStatisticsRepository;

    @DisplayName("시간대별 통계 배치 처리 시,")
    @Nested
    class ProcessBatch {

        @DisplayName("신규 통계 데이터가 저장된다.")
        @Test
        void savesNewStatistics_whenNoExistingData() {
            // arrange
            Map<Integer, Integer> hourlyAvg1 = new HashMap<>();
            hourlyAvg1.put(9, 10);
            hourlyAvg1.put(10, 15);

            Map<Integer, Integer> hourlyAvg2 = new HashMap<>();
            hourlyAvg2.put(9, 20);

            StationHourlyAvg stationHourlyAvg1 = new StationHourlyAvg(1L, hourlyAvg1);
            StationHourlyAvg stationHourlyAvg2 = new StationHourlyAvg(2L, hourlyAvg2);
            List<StationHourlyAvg> stationHourlyAvgs = List.of(stationHourlyAvg1, stationHourlyAvg2);

            // act
            int result = hourlyStatisticsService.processBatch(stationHourlyAvgs);

            // assert
            assertThat(result).isEqualTo(3); // 3개 시간대 통계

            List<HourlyStatistics> savedStats = hourlyStatisticsRepository.findAllByStationIds(List.of(1L, 2L));
            assertThat(savedStats).hasSize(3);
        }

        @DisplayName("기존 통계 데이터가 있으면 업데이트된다.")
        @Test
        void updatesExistingStatistics_whenDataExists() {
            // arrange - 기존 데이터 생성
            HourlyStatistics existing = HourlyStatistics.create(1L, 9, 10);
            hourlyStatisticsRepository.saveAllHourlyStatistics(List.of(existing));

            Map<Integer, Integer> hourlyAvg = new HashMap<>();
            hourlyAvg.put(9, 15);
            StationHourlyAvg stationHourlyAvg = new StationHourlyAvg(1L, hourlyAvg);
            List<StationHourlyAvg> stationHourlyAvgs = List.of(stationHourlyAvg);

            // act
            int result = hourlyStatisticsService.processBatch(stationHourlyAvgs);

            // assert
            assertThat(result).isEqualTo(1);

            List<HourlyStatistics> updatedStats = hourlyStatisticsRepository.findAllByStationIds(List.of(1L));
            assertThat(updatedStats).hasSize(1);
            HourlyStatistics updated = updatedStats.get(0);
            assertThat(updated.getAvgParkingBikeCount()).isEqualTo(12); // (10 * 1 + 15) / 2 = 12.5 -> 13
            assertThat(updated.getSampleCount()).isEqualTo(2L);
        }

        @DisplayName("빈 리스트가 주어지면 0을 반환한다.")
        @Test
        void returnsZero_whenInputIsEmpty() {
            // arrange
            List<StationHourlyAvg> stationHourlyAvgs = List.of();

            // act
            int result = hourlyStatisticsService.processBatch(stationHourlyAvgs);

            // assert
            assertThat(result).isEqualTo(0);
        }

        @DisplayName("null이 주어지면 0을 반환한다.")
        @Test
        void returnsZero_whenInputIsNull() {
            // act
            int result = hourlyStatisticsService.processBatch(null);

            // assert
            assertThat(result).isEqualTo(0);
        }
    }
}
