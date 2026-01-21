package com.taja.application.status;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
class StationStatusServiceIntegrationTest {

    @Autowired
    private StationStatusService stationStatusService;

    @DisplayName("날짜별 대여소 상태 조회 시,")
    @Nested
    class FindStationStatusesByDate {

        @DisplayName("해당 날짜의 대여소 상태 데이터를 조회할 수 있다.")
        @Test
        void findsStationStatuses_whenDataExists() {
            // arrange
            LocalDate calculationDate = LocalDate.of(2024, 1, 1);

            // act
            List<StationStatus> result = stationStatusService.findStationStatusesByDate(calculationDate);

            // assert
            assertThat(result).isNotNull();
        }

        @DisplayName("데이터가 없으면 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyList_whenNoDataExists() {
            // arrange
            LocalDate calculationDate = LocalDate.of(2099, 12, 31);

            // act
            List<StationStatus> result = stationStatusService.findStationStatusesByDate(calculationDate);

            // assert
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("시간대별 평균 주차 자전거 수 계산 시,")
    @Nested
    class CalculateHourlyAvgParkingBikeCount {

        @DisplayName("같은 대여소의 같은 시간대에 여러 데이터가 있으면 평균을 계산하고 반올림한다 - 대여소1의 9시에 10대와 15대가 있으면 평균 12.5를 반올림하여 13이 된다")
        @Test
        void calculatesHourlyAverageCorrectly() {
            // arrange
            LocalDate date = LocalDate.of(2024, 1, 1);

            StationStatus status1 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(10)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();

            StationStatus status2 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(15)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();

            StationStatus status3 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(20)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(10, 0))
                    .build();

            StationStatus status4 = StationStatus.builder()
                    .stationId(2L)
                    .stationNumber(2)
                    .parkingBikeCount(30)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();

            List<StationStatus> stationStatuses = List.of(status1, status2, status3, status4);

            // act
            var result = stationStatusService.calculateHourlyAvgParkingBikeCount(stationStatuses);

            // assert
            assertThat(result).hasSize(2); // 2개 대여소

            var station1Result = result.stream()
                    .filter(avg -> avg.stationId().equals(1L))
                    .findFirst()
                    .orElseThrow();
            assertThat(station1Result.hourlyAvgParkingBikeCounts()).hasSize(2);
            // 대여소1의 9시: (10 + 15) / 2 = 12.5 -> Math.round(12.5) = 13
            assertThat(station1Result.hourlyAvgParkingBikeCounts().get(9)).isEqualTo(13);
            // 대여소1의 10시: 20대 하나만 있으므로 평균 20
            assertThat(station1Result.hourlyAvgParkingBikeCounts().get(10)).isEqualTo(20);

            var station2Result = result.stream()
                    .filter(avg -> avg.stationId().equals(2L))
                    .findFirst()
                    .orElseThrow();
            assertThat(station2Result.hourlyAvgParkingBikeCounts()).hasSize(1);
            // 대여소2의 9시: 30대 하나만 있으므로 평균 30
            assertThat(station2Result.hourlyAvgParkingBikeCounts().get(9)).isEqualTo(30);
        }

        @DisplayName("평균이 0.5 이상이면 올림하고 0.5 미만이면 내림한다 - 평균 12.7은 13이 되고, 평균 12.2는 12가 된다")
        @Test
        void roundsUpWhenAverageIsHalfOrMore_roundsDownWhenAverageIsLessThanHalf() {
            // arrange
            LocalDate date = LocalDate.of(2024, 1, 1);

            // 평균 12.7이 되는 케이스: (10 + 10 + 18) / 3 = 12.666... -> Math.round(12.666...) = 13
            StationStatus status1_1 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(10)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();
            StationStatus status1_2 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(10)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();
            StationStatus status1_3 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(18)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();

            // 평균 12.2가 되는 케이스: (10 + 10 + 17) / 3 = 12.333... -> Math.round(12.333...) = 12
            StationStatus status2_1 = StationStatus.builder()
                    .stationId(2L)
                    .stationNumber(2)
                    .parkingBikeCount(10)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();
            StationStatus status2_2 = StationStatus.builder()
                    .stationId(2L)
                    .stationNumber(2)
                    .parkingBikeCount(10)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();
            StationStatus status2_3 = StationStatus.builder()
                    .stationId(2L)
                    .stationNumber(2)
                    .parkingBikeCount(17)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();

            List<StationStatus> stationStatuses = List.of(
                    status1_1, status1_2, status1_3,
                    status2_1, status2_2, status2_3
            );

            // act
            var result = stationStatusService.calculateHourlyAvgParkingBikeCount(stationStatuses);

            // assert
            var station1Result = result.stream()
                    .filter(avg -> avg.stationId().equals(1L))
                    .findFirst()
                    .orElseThrow();
            // 대여소1의 9시: (10 + 10 + 18) / 3 = 12.666... -> Math.round(12.666...) = 13
            assertThat(station1Result.hourlyAvgParkingBikeCounts().get(9)).isEqualTo(13);

            var station2Result = result.stream()
                    .filter(avg -> avg.stationId().equals(2L))
                    .findFirst()
                    .orElseThrow();
            // 대여소2의 9시: (10 + 10 + 17) / 3 = 12.333... -> Math.round(12.333...) = 12
            assertThat(station2Result.hourlyAvgParkingBikeCounts().get(9)).isEqualTo(12);
        }

        @DisplayName("빈 리스트가 주어지면 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyList_whenInputIsEmpty() {
            // arrange
            List<StationStatus> stationStatuses = List.of();

            // act
            var result = stationStatusService.calculateHourlyAvgParkingBikeCount(stationStatuses);

            // assert
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("일평균 주차 자전거 수 계산 시,")
    @Nested
    class CalculateDailyAvgParkingBikeCount {

        @DisplayName("같은 대여소의 하루 동안 여러 시간대 데이터가 있으면 일평균을 계산하고 반올림한다 - 대여소1에 9시 10대, 10시 15대, 11시 20대가 있으면 평균 15가 된다")
        @Test
        void calculatesDailyAverageCorrectly() {
            // arrange
            LocalDate date = LocalDate.of(2024, 1, 1);

            StationStatus status1 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(10)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();

            StationStatus status2 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(15)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(10, 0))
                    .build();

            StationStatus status3 = StationStatus.builder()
                    .stationId(1L)
                    .stationNumber(1)
                    .parkingBikeCount(20)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(11, 0))
                    .build();

            StationStatus status4 = StationStatus.builder()
                    .stationId(2L)
                    .stationNumber(2)
                    .parkingBikeCount(30)
                    .requestedDate(date)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();

            List<StationStatus> stationStatuses = List.of(status1, status2, status3, status4);

            // act
            var result = stationStatusService.calculateDailyAvgParkingBikeCount(stationStatuses);

            // assert
            assertThat(result).hasSize(2); // 2개 대여소

            var station1Result = result.stream()
                    .filter(avg -> avg.stationId().equals(1L))
                    .findFirst()
                    .orElseThrow();
            // 대여소1의 일평균: (10 + 15 + 20) / 3 = 15.0 -> Math.round(15.0) = 15
            assertThat(station1Result.dailyAvgParkingBikeCount()).isEqualTo(15);

            var station2Result = result.stream()
                    .filter(avg -> avg.stationId().equals(2L))
                    .findFirst()
                    .orElseThrow();
            // 대여소2의 일평균: 30대 하나만 있으므로 평균 30
            assertThat(station2Result.dailyAvgParkingBikeCount()).isEqualTo(30);
        }

        @DisplayName("빈 리스트가 주어지면 빈 리스트를 반환한다.")
        @Test
        void returnsEmptyList_whenInputIsEmpty() {
            // arrange
            List<StationStatus> stationStatuses = List.of();

            // act
            var result = stationStatusService.calculateDailyAvgParkingBikeCount(stationStatuses);

            // assert
            assertThat(result).isEmpty();
        }
    }
}
