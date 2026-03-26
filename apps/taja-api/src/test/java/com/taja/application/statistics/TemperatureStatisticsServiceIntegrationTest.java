package com.taja.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.domain.statistics.TemperatureStatistics;
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
class TemperatureStatisticsServiceIntegrationTest {

    @Autowired
    private TemperatureStatisticsService temperatureStatisticsService;

    @Autowired
    private TemperatureStatisticsRepository temperatureStatisticsRepository;

    @DisplayName("기온별 통계 저장 시,")
    @Nested
    class SaveStatistics {

        @DisplayName("통계 데이터가 저장된다.")
        @Test
        void savesStatistics_whenDataProvided() {
            // arrange
            TemperatureStatistics stats1 = TemperatureStatistics.create(1L, 20.0, 10);
            TemperatureStatistics stats2 = TemperatureStatistics.create(2L, 25.0, 20);
            List<TemperatureStatistics> statistics = List.of(stats1, stats2);

            // act
            temperatureStatisticsService.saveStatistics(statistics);

            // assert
            List<TemperatureStatistics> savedStats = temperatureStatisticsRepository.findAllByStationIds(List.of(1L, 2L));
            assertThat(savedStats).hasSize(2);
        }
    }

    @DisplayName("대여소 ID로 통계 조회 시,")
    @Nested
    class FindStatisticsByStationIds {

        @DisplayName("해당 대여소의 통계 데이터를 조회할 수 있다.")
        @Test
        void findsStatistics_whenDataExists() {
            // arrange
            TemperatureStatistics stats = TemperatureStatistics.create(1L, 20.0, 10);
            temperatureStatisticsRepository.saveTemperatureStatistics(List.of(stats));

            // act
            List<TemperatureStatistics> result = temperatureStatisticsService.findStatisticsByStationIds(List.of(1L));

            // assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStationId()).isEqualTo(1L);
            assertThat(result.get(0).getTemperatureRange()).isEqualTo(20.0);
        }

        @DisplayName("데이터가 없으면 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyList_whenNoDataExists() {
            // act
            List<TemperatureStatistics> result = temperatureStatisticsService.findStatisticsByStationIds(List.of(999L));

            // assert
            assertThat(result).isEmpty();
        }
    }
}
