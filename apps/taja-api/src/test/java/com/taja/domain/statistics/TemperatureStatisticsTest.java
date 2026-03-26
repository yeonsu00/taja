package com.taja.domain.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TemperatureStatisticsTest {

    @DisplayName("평균 남은 자전거 수 업데이트 시,")
    @Nested
    class UpdateAvgParkingBikeCount {

        @DisplayName("샘플이 1개일 때 두 번째 값으로 평균이 올바르게 계산된다.")
        @Test
        void calculatesAverageCorrectly_whenOneSampleExists() {
            // arrange
            Long stationId = 1L;
            Double temperatureRange = 20.0;
            Integer firstValue = 5;
            TemperatureStatistics statistics = TemperatureStatistics.create(stationId, temperatureRange, firstValue);

            // act
            Integer secondValue = 15;
            statistics.updateAvgParkingBikeCount(secondValue);

            // assert
            Integer expectedAverage = (firstValue + secondValue) / 2;
            assertAll(
                    () -> assertThat(statistics.getAvgParkingBikeCount()).isEqualTo(expectedAverage),
                    () -> assertThat(statistics.getSampleCount()).isEqualTo(2L)
            );
        }

        @DisplayName("여러 샘플이 있을 때 평균이 올바르게 계산된다.")
        @Test
        void calculatesAverageCorrectly_whenMultipleSamplesExist() {
            // arrange
            Long stationId = 1L;
            Double temperatureRange = 25.0;
            TemperatureStatistics statistics = TemperatureStatistics.create(stationId, temperatureRange, 10);

            // act
            statistics.updateAvgParkingBikeCount(20);
            statistics.updateAvgParkingBikeCount(30);
            statistics.updateAvgParkingBikeCount(40);

            // assert
            // (10 + 20 + 30 + 40) / 4 = 25
            assertAll(
                    () -> assertThat(statistics.getAvgParkingBikeCount()).isEqualTo(25),
                    () -> assertThat(statistics.getSampleCount()).isEqualTo(4L)
            );
        }

        @DisplayName("동일한 값들을 추가해도 평균이 올바르게 계산된다.")
        @Test
        void calculatesAverageCorrectly_whenSameValuesAreAdded() {
            // arrange
            Long stationId = 1L;
            Double temperatureRange = 15.0;
            Integer value = 10;
            TemperatureStatistics statistics = TemperatureStatistics.create(stationId, temperatureRange, value);

            // act
            statistics.updateAvgParkingBikeCount(value);
            statistics.updateAvgParkingBikeCount(value);
            statistics.updateAvgParkingBikeCount(value);

            // assert
            assertAll(
                    () -> assertThat(statistics.getAvgParkingBikeCount()).isEqualTo(value),
                    () -> assertThat(statistics.getSampleCount()).isEqualTo(4L)
            );
        }

        @DisplayName("0 값이 포함되어도 평균이 올바르게 계산된다.")
        @Test
        void calculatesAverageCorrectly_whenZeroValueIsIncluded() {
            // arrange
            Long stationId = 1L;
            Double temperatureRange = 30.0;
            TemperatureStatistics statistics = TemperatureStatistics.create(stationId, temperatureRange, 10);

            // act
            statistics.updateAvgParkingBikeCount(0);
            statistics.updateAvgParkingBikeCount(20);

            // assert
            // (10 + 0 + 20) / 3 = 10
            assertAll(
                    () -> assertThat(statistics.getAvgParkingBikeCount()).isEqualTo(10),
                    () -> assertThat(statistics.getSampleCount()).isEqualTo(3L)
            );
        }

        @DisplayName("큰 값들로도 평균이 올바르게 계산된다.")
        @Test
        void calculatesAverageCorrectly_whenLargeValuesAreAdded() {
            // arrange
            Long stationId = 1L;
            Double temperatureRange = 35.0;
            TemperatureStatistics statistics = TemperatureStatistics.create(stationId, temperatureRange, 100);

            // act
            statistics.updateAvgParkingBikeCount(200);
            statistics.updateAvgParkingBikeCount(300);

            // assert
            // (100 + 200 + 300) / 3 = 200
            assertAll(
                    () -> assertThat(statistics.getAvgParkingBikeCount()).isEqualTo(200),
                    () -> assertThat(statistics.getSampleCount()).isEqualTo(3L)
            );
        }
    }
}