package com.taja.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.application.statistics.dto.StationDailyAvg;
import com.taja.domain.statistics.DayOfWeekStatistics;
import java.time.DayOfWeek;
import java.util.List;
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
class DayOfWeekStatisticsServiceIntegrationTest {

    @Autowired
    private DayOfWeekStatisticsService dayOfWeekStatisticsService;

    @Autowired
    private DayOfWeekStatisticsRepository dayOfWeekStatisticsRepository;

    @DisplayName("요일별 통계 배치 처리 시,")
    @Nested
    class ProcessBatch {

        @DisplayName("신규 통계 데이터가 저장된다.")
        @Test
        void savesNewStatistics_whenNoExistingData() {
            // arrange
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            StationDailyAvg stationDailyAvg1 = new StationDailyAvg(1L, 10);
            StationDailyAvg stationDailyAvg2 = new StationDailyAvg(2L, 20);
            List<StationDailyAvg> stationDailyAvgs = List.of(stationDailyAvg1, stationDailyAvg2);

            // act
            int result = dayOfWeekStatisticsService.processBatch(dayOfWeek, stationDailyAvgs);

            // assert
            assertThat(result).isEqualTo(2);

            List<DayOfWeekStatistics> savedStats = dayOfWeekStatisticsRepository.findAllByStationIdsAndDayOfWeek(
                    List.of(1L, 2L), dayOfWeek);
            assertThat(savedStats).hasSize(2);
        }

        @DisplayName("기존 통계 데이터가 있으면 업데이트된다.")
        @Test
        void updatesExistingStatistics_whenDataExists() {
            // arrange - 기존 데이터 생성
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            DayOfWeekStatistics existing = DayOfWeekStatistics.create(1L, dayOfWeek, 10);
            dayOfWeekStatisticsRepository.saveAllDayOfWeekStatistics(List.of(existing));

            StationDailyAvg stationDailyAvg = new StationDailyAvg(1L, 15);
            List<StationDailyAvg> stationDailyAvgs = List.of(stationDailyAvg);

            // act
            int result = dayOfWeekStatisticsService.processBatch(dayOfWeek, stationDailyAvgs);

            // assert
            assertThat(result).isEqualTo(1);

            List<DayOfWeekStatistics> updatedStats = dayOfWeekStatisticsRepository.findAllByStationIdsAndDayOfWeek(
                    List.of(1L), dayOfWeek);
            assertThat(updatedStats).hasSize(1);
            DayOfWeekStatistics updated = updatedStats.getFirst();
            assertThat(updated.getAvgParkingBikeCount()).isEqualTo(12); // (10 * 1 + 15) / 2 = 12.5 -> 13
            assertThat(updated.getSampleCount()).isEqualTo(2L);
        }

        @DisplayName("빈 리스트가 주어지면 0을 반환한다.")
        @Test
        void returnsZero_whenInputIsEmpty() {
            // arrange
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;
            List<StationDailyAvg> stationDailyAvgs = List.of();

            // act
            int result = dayOfWeekStatisticsService.processBatch(dayOfWeek, stationDailyAvgs);

            // assert
            assertThat(result).isEqualTo(0);
        }

        @DisplayName("null이 주어지면 0을 반환한다.")
        @Test
        void returnsZero_whenInputIsNull() {
            // arrange
            DayOfWeek dayOfWeek = DayOfWeek.MONDAY;

            // act
            int result = dayOfWeekStatisticsService.processBatch(dayOfWeek, null);

            // assert
            assertThat(result).isEqualTo(0);
        }
    }
}
