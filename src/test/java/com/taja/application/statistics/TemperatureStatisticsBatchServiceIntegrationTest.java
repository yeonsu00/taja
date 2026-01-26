package com.taja.application.statistics;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.application.station.StationRepository;
import com.taja.application.statistics.dto.StationDistricts;
import com.taja.application.status.StationStatusRepository;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.domain.statistics.TemperatureStatistics;
import com.taja.domain.status.StationStatus;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TemperatureStatisticsBatchServiceIntegrationTest {

    @Autowired
    private TemperatureStatisticsBatchService temperatureStatisticsBatchService;

    @Autowired
    private TemperatureStatisticsService temperatureStatisticsService;

    @Autowired
    private StationStatusRepository stationStatusRepository;

    @Autowired
    private StationRepository stationRepository;

    @DisplayName("기온별 통계 배치 처리 시,")
    @Nested
    class ProcessBatch {

        @DisplayName("대여소 상태와 기온 데이터가 있으면 통계가 생성된다.")
        @Test
        void createsStatistics_whenStationStatusesAndWeatherExist() {
            // arrange
            LocalDate calculationDate = LocalDate.of(2024, 1, 1);
            Station station = createStation(1L, "대여소1", "강남구");
            station = stationRepository.upsert(List.of(station)).getFirst();

            // 대여소 상태 데이터 생성 및 저장
            StationStatus status1 = StationStatus.builder()
                    .stationNumber(1)
                    .parkingBikeCount(10)
                    .requestedDate(calculationDate)
                    .requestedTime(LocalTime.of(9, 0))
                    .build();
            stationStatusRepository.saveAll(List.of(status1));

            // 기온 데이터
            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();
            Map<Integer, Double> gangnamTemp = new HashMap<>();
            gangnamTemp.put(9, 20.0);
            districtHourlyTempMap.put("강남구", gangnamTemp);

            StationDistricts stationDistricts = StationDistricts.from(List.of(station));

            // act
            temperatureStatisticsBatchService.processBatch(
                    List.of(station), calculationDate, districtHourlyTempMap, stationDistricts);

            // assert
            List<TemperatureStatistics> savedStats = temperatureStatisticsService.findStatisticsByStationIds(List.of(station.getStationId()));
            assertThat(savedStats).isNotEmpty();
        }

        @DisplayName("기온 데이터가 없으면 통계가 생성되지 않는다.")
        @Test
        void doesNotCreateStatistics_whenWeatherDataMissing() {
            // arrange
            LocalDate calculationDate = LocalDate.of(2024, 1, 1);
            Station station = createStation(1L, "대여소1", "강남구");

            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();
            StationDistricts stationDistricts = StationDistricts.from(List.of(station));

            // act
            temperatureStatisticsBatchService.processBatch(
                    List.of(station), calculationDate, districtHourlyTempMap, stationDistricts);

            // assert
            List<TemperatureStatistics> savedStats = temperatureStatisticsService.findStatisticsByStationIds(List.of(1L));
            assertThat(savedStats).isEmpty();
        }
    }

    private Station createStation(Long stationId, String name, String district) {
        return Station.builder()
                .stationId(stationId)
                .name(name)
                .number(stationId.intValue())
                .district(district)
                .address("주소")
                .latitude(37.5665)
                .longitude(126.9780)
                .operationMode(OperationMode.QR)
                .build();
    }
}
