package com.taja.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.taja.application.station.StationService;
import com.taja.application.statistics.dto.StationDailyAvg;
import com.taja.application.statistics.dto.StationDistricts;
import com.taja.application.statistics.dto.StationHourlyAvg;
import com.taja.application.status.StationStatusHourlyAvgService;
import com.taja.application.status.StationStatusService;
import com.taja.application.weather.WeatherService;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest
@ActiveProfiles("test")
class StatisticsFacadeIntegrationTest {

    @Autowired
    private StatisticsFacade statisticsFacade;

    @MockitoSpyBean
    private StationStatusService stationStatusService;

    @MockitoSpyBean
    private StationStatusHourlyAvgService stationStatusHourlyAvgService;

    @MockitoSpyBean
    private HourlyStatisticsService hourlyStatisticsService;

    @MockitoSpyBean
    private DayOfWeekStatisticsService dayOfWeekStatisticsService;

    @MockitoSpyBean
    private TemperatureStatisticsBatchService temperatureStatisticsBatchService;

    @MockitoSpyBean
    private WeatherService weatherService;

    @MockitoSpyBean
    private StationService stationService;

    @DisplayName("시간대별 통계를 계산할 때,")
    @Nested
    class CalculateHourlyStatistics {

        @DisplayName("대여소 상태 데이터가 있으면 시간대별 통계가 계산되고 저장된다.")
        @Test
        void calculatesHourlyStatistics_whenStationStatusesExist() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            Map<Integer, Integer> hourlyAvg1 = new HashMap<>();
            hourlyAvg1.put(9, 10);
            hourlyAvg1.put(10, 15);
            Map<Integer, Integer> hourlyAvg2 = new HashMap<>();
            hourlyAvg2.put(9, 20);

            StationHourlyAvg stationHourlyAvg1 = new StationHourlyAvg(1L, hourlyAvg1);
            StationHourlyAvg stationHourlyAvg2 = new StationHourlyAvg(2L, hourlyAvg2);
            List<StationHourlyAvg> stationHourlyAvgs = List.of(stationHourlyAvg1, stationHourlyAvg2);

            doReturn(stationHourlyAvgs).when(stationStatusHourlyAvgService).findStationHourlyAvgsByDate(calculationDate);
            doReturn(2).when(hourlyStatisticsService).processBatch(anyList());

            // act
            int result = statisticsFacade.calculateHourlyStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(2);

            // verify
            verify(stationStatusHourlyAvgService, times(1)).findStationHourlyAvgsByDate(calculationDate);
            verify(hourlyStatisticsService, times(1)).processBatch(anyList());
        }

        @DisplayName("대여소 상태 데이터가 없으면 0을 반환한다.")
        @Test
        void returnsZero_whenNoStationStatuses() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            doReturn(List.of()).when(stationStatusHourlyAvgService).findStationHourlyAvgsByDate(calculationDate);
            doReturn(0).when(hourlyStatisticsService).processBatch(anyList());

            // act
            int result = statisticsFacade.calculateHourlyStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(0);

            // verify
            verify(stationStatusHourlyAvgService, times(1)).findStationHourlyAvgsByDate(calculationDate);
        }

        @DisplayName("배치 처리 시 여러 배치로 나누어 처리된다.")
        @Test
        void processesInBatches_whenDataExceedsBatchSize() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            List<StationHourlyAvg> stationHourlyAvgs = createStationHourlyAvgs(250); // BATCH_SIZE(100)보다 큰 데이터

            doReturn(stationHourlyAvgs).when(stationStatusHourlyAvgService).findStationHourlyAvgsByDate(calculationDate);
            doReturn(100).when(hourlyStatisticsService).processBatch(anyList());

            // act
            int result = statisticsFacade.calculateHourlyStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(300); // 100 * 3 batches

            // verify
            verify(stationStatusHourlyAvgService, times(1)).findStationHourlyAvgsByDate(calculationDate);
            verify(hourlyStatisticsService, times(3)).processBatch(anyList()); // 3 batches
        }

        @DisplayName("집계 로직이 실패해도 예외가 발생한다.")
        @Test
        void throwsException_whenAggregationFails() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            doThrow(new RuntimeException("집계 실패"))
                    .when(stationStatusHourlyAvgService).findStationHourlyAvgsByDate(calculationDate);

            // act & assert
            RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                statisticsFacade.calculateHourlyStatistics(requestedAt);
            });

            assertThat(exception.getMessage()).contains("집계 실패");

            // verify
            verify(stationStatusHourlyAvgService, times(1)).findStationHourlyAvgsByDate(calculationDate);
            verify(hourlyStatisticsService, never()).processBatch(anyList());
        }
    }

    @DisplayName("요일별 통계를 계산할 때,")
    @Nested
    class CalculateDayOfWeekStatistics {

        @DisplayName("대여소 상태 데이터가 있으면 요일별 통계가 계산되고 저장된다.")
        @Test
        void calculatesDayOfWeekStatistics_whenStationStatusesExist() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2); // 화요일
            LocalDate calculationDate = requestedAt.minusDays(1); // 월요일
            DayOfWeek dayOfWeek = calculationDate.getDayOfWeek(); // MONDAY

            StationStatus status1 = createStationStatus(1, 10, calculationDate, LocalTime.of(9, 0));
            StationStatus status2 = createStationStatus(1, 15, calculationDate, LocalTime.of(10, 0));
            StationStatus status3 = createStationStatus(2, 20, calculationDate, LocalTime.of(9, 0));

            List<StationStatus> stationStatuses = List.of(status1, status2, status3);

            StationDailyAvg stationDailyAvg1 = new StationDailyAvg(1L, 12);
            StationDailyAvg stationDailyAvg2 = new StationDailyAvg(2L, 20);
            List<StationDailyAvg> stationDailyAvgs = List.of(stationDailyAvg1, stationDailyAvg2);

            doReturn(stationStatuses).when(stationStatusService).findStationStatusesByDate(calculationDate);
            doReturn(stationDailyAvgs).when(stationStatusService).calculateDailyAvgParkingBikeCount(stationStatuses);
            doReturn(2).when(dayOfWeekStatisticsService).processBatch(eq(dayOfWeek), anyList());

            // act
            int result = statisticsFacade.calculateDayOfWeekStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(2);

            // verify
            verify(stationStatusService, times(1)).findStationStatusesByDate(calculationDate);
            verify(stationStatusService, times(1)).calculateDailyAvgParkingBikeCount(stationStatuses);
            verify(dayOfWeekStatisticsService, times(1)).processBatch(eq(dayOfWeek), anyList());
        }

        @DisplayName("대여소 상태 데이터가 없으면 0을 반환한다.")
        @Test
        void returnsZero_whenNoStationStatuses() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);
            DayOfWeek dayOfWeek = calculationDate.getDayOfWeek();

            doReturn(List.of()).when(stationStatusService).findStationStatusesByDate(calculationDate);
            doReturn(List.of()).when(stationStatusService).calculateDailyAvgParkingBikeCount(anyList());
            doReturn(0).when(dayOfWeekStatisticsService).processBatch(eq(dayOfWeek), anyList());

            // act
            int result = statisticsFacade.calculateDayOfWeekStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(0);

            // verify
            verify(stationStatusService, times(1)).findStationStatusesByDate(calculationDate);
            verify(stationStatusService, times(1)).calculateDailyAvgParkingBikeCount(anyList());
            verify(dayOfWeekStatisticsService, never()).processBatch(eq(dayOfWeek), anyList());
        }

        @DisplayName("배치 처리 시 여러 배치로 나누어 처리된다.")
        @Test
        void processesInBatches_whenDataExceedsBatchSize() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);
            DayOfWeek dayOfWeek = calculationDate.getDayOfWeek();

            List<StationStatus> stationStatuses = List.of();
            List<StationDailyAvg> stationDailyAvgs = createStationDailyAvgs(250); // BATCH_SIZE(100)보다 큰 데이터

            doReturn(stationStatuses).when(stationStatusService).findStationStatusesByDate(calculationDate);
            doReturn(stationDailyAvgs).when(stationStatusService).calculateDailyAvgParkingBikeCount(stationStatuses);
            doReturn(100).when(dayOfWeekStatisticsService).processBatch(eq(dayOfWeek), anyList());

            // act
            int result = statisticsFacade.calculateDayOfWeekStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(300); // 100 * 3 batches

            // verify
            verify(stationStatusService, times(1)).findStationStatusesByDate(calculationDate);
            verify(dayOfWeekStatisticsService, times(3)).processBatch(eq(dayOfWeek), anyList()); // 3 batches
        }

        @DisplayName("집계 로직이 실패하면 예외가 발생한다.")
        @Test
        void throwsException_whenAggregationFails() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            doReturn(List.of()).when(stationStatusService).findStationStatusesByDate(calculationDate);
            doThrow(new RuntimeException("집계 실패"))
                    .when(stationStatusService).calculateDailyAvgParkingBikeCount(anyList());

            // act & assert
            RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                statisticsFacade.calculateDayOfWeekStatistics(requestedAt);
            });

            assertThat(exception.getMessage()).contains("집계 실패");

            // verify
            verify(stationStatusService, times(1)).findStationStatusesByDate(calculationDate);
            verify(dayOfWeekStatisticsService, never()).processBatch(any(DayOfWeek.class), anyList());
        }
    }

    @DisplayName("기온별 통계를 계산할 때,")
    @Nested
    class CalculateTemperatureStatistics {

        @DisplayName("대여소와 기온 데이터가 있으면 기온별 통계가 계산되고 저장된다.")
        @Test
        void calculatesTemperatureStatistics_whenStationsAndWeatherExist() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            Station station1 = createStation(1L, "대여소1", "강남구");
            Station station2 = createStation(2L, "대여소2", "서초구");
            List<Station> stations = List.of(station1, station2);

            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();
            Map<Integer, Double> gangnamTemp = new HashMap<>();
            gangnamTemp.put(9, 5.0);
            gangnamTemp.put(10, 6.0);
            Map<Integer, Double> seochoTemp = new HashMap<>();
            seochoTemp.put(9, 5.5);
            districtHourlyTempMap.put("강남구", gangnamTemp);
            districtHourlyTempMap.put("서초구", seochoTemp);

            doReturn(districtHourlyTempMap).when(weatherService).findWeathersByBaseDate(calculationDate);
            doReturn(stations).when(stationService).findAllStations();

            // act
            int result = statisticsFacade.calculateTemperatureStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(2);

            // verify
            verify(weatherService, times(1)).findWeathersByBaseDate(calculationDate);
            verify(stationService, times(1)).findAllStations();
            verify(temperatureStatisticsBatchService, times(1)).processBatch(
                    anyList(), any(LocalDate.class), any(Map.class), any(StationDistricts.class));
        }

        @DisplayName("대여소가 없으면 0을 반환한다.")
        @Test
        void returnsZero_whenNoStations() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();

            doReturn(districtHourlyTempMap).when(weatherService).findWeathersByBaseDate(calculationDate);
            doReturn(List.of()).when(stationService).findAllStations();

            // act
            int result = statisticsFacade.calculateTemperatureStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(0);

            // verify
            verify(weatherService, times(1)).findWeathersByBaseDate(calculationDate);
            verify(stationService, times(1)).findAllStations();
            verify(temperatureStatisticsBatchService, never()).processBatch(
                    anyList(), any(LocalDate.class), any(Map.class), any(StationDistricts.class));
        }

        @DisplayName("배치 처리 시 여러 배치로 나누어 처리된다.")
        @Test
        void processesInBatches_whenDataExceedsBatchSize() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            List<Station> stations = createStations(250); // BATCH_SIZE(100)보다 큰 데이터
            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();

            doReturn(districtHourlyTempMap).when(weatherService).findWeathersByBaseDate(calculationDate);
            doReturn(stations).when(stationService).findAllStations();

            // act
            int result = statisticsFacade.calculateTemperatureStatistics(requestedAt);

            // assert
            assertThat(result).isEqualTo(250);

            // verify
            verify(weatherService, times(1)).findWeathersByBaseDate(calculationDate);
            verify(stationService, times(1)).findAllStations();
            verify(temperatureStatisticsBatchService, times(3)).processBatch(
                    anyList(), any(LocalDate.class), any(Map.class), any(StationDistricts.class)); // 3 batches
        }

        @DisplayName("기온 데이터 조회가 실패하면 예외가 발생한다.")
        @Test
        void throwsException_whenWeatherServiceFails() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            doThrow(new RuntimeException("기온 데이터 조회 실패"))
                    .when(weatherService).findWeathersByBaseDate(calculationDate);

            // act & assert
            RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                statisticsFacade.calculateTemperatureStatistics(requestedAt);
            });

            assertThat(exception.getMessage()).contains("기온 데이터 조회 실패");

            // verify
            verify(weatherService, times(1)).findWeathersByBaseDate(calculationDate);
            verify(stationService, never()).findAllStations();
            verify(temperatureStatisticsBatchService, never()).processBatch(
                    anyList(), any(LocalDate.class), any(Map.class), any(StationDistricts.class));
        }

        @DisplayName("대여소 조회가 실패하면 예외가 발생한다.")
        @Test
        void throwsException_whenStationServiceFails() {
            // arrange
            LocalDate requestedAt = LocalDate.of(2024, 1, 2);
            LocalDate calculationDate = requestedAt.minusDays(1);

            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();

            doReturn(districtHourlyTempMap).when(weatherService).findWeathersByBaseDate(calculationDate);
            doThrow(new RuntimeException("대여소 조회 실패"))
                    .when(stationService).findAllStations();

            // act & assert
            RuntimeException exception = org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class, () -> {
                statisticsFacade.calculateTemperatureStatistics(requestedAt);
            });

            assertThat(exception.getMessage()).contains("대여소 조회 실패");

            // verify
            verify(weatherService, times(1)).findWeathersByBaseDate(calculationDate);
            verify(stationService, times(1)).findAllStations();
            verify(temperatureStatisticsBatchService, never()).processBatch(
                    anyList(), any(LocalDate.class), any(Map.class), any(StationDistricts.class));
        }
    }

    private StationStatus createStationStatus(Integer stationNumber, Integer parkingBikeCount,
                                               LocalDate requestedDate, LocalTime requestedTime) {
        StationStatus status = mock(StationStatus.class);
        when(status.getStationNumber()).thenReturn(stationNumber);
        when(status.getParkingBikeCount()).thenReturn(parkingBikeCount);
        when(status.getRequestedDate()).thenReturn(requestedDate);
        when(status.getRequestedTime()).thenReturn(requestedTime);
        return status;
    }

    private Station createStation(Long stationId, String name, String district) {
        Station station = mock(Station.class);
        when(station.getStationId()).thenReturn(stationId);
        when(station.getName()).thenReturn(name);
        when(station.getDistrict()).thenReturn(district);
        when(station.getNumber()).thenReturn(stationId.intValue());
        when(station.getAddress()).thenReturn("주소");
        when(station.getLatitude()).thenReturn(37.5665);
        when(station.getLongitude()).thenReturn(126.9780);
        when(station.getOperationMode()).thenReturn(OperationMode.QR);
        return station;
    }

    private List<StationHourlyAvg> createStationHourlyAvgs(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> {
                    Map<Integer, Integer> hourlyAvg = new HashMap<>();
                    hourlyAvg.put(9, 10);
                    return new StationHourlyAvg((long) (i + 1), hourlyAvg);
                })
                .toList();
    }

    private List<StationDailyAvg> createStationDailyAvgs(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> new StationDailyAvg((long) (i + 1), 10))
                .toList();
    }

    private List<Station> createStations(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> createStation((long) (i + 1), "대여소" + (i + 1), "강남구"))
                .toList();
    }
}
