package com.taja.application.weather;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.HashMap;
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
class WeatherServiceIntegrationTest {

    @Autowired
    private WeatherService weatherService;

    @DisplayName("날짜별 기온 데이터 조회 시,")
    @Nested
    class FindWeathersByBaseDate {

        @DisplayName("해당 날짜의 기온 데이터를 조회할 수 있다.")
        @Test
        void findsWeathers_whenDataExists() {
            // arrange
            LocalDate calculationDate = LocalDate.of(2024, 1, 1);

            // act
            Map<String, Map<Integer, Double>> result = weatherService.findWeathersByBaseDate(calculationDate);

            // assert
            assertThat(result).isNotNull();
        }

        @DisplayName("데이터가 없으면 빈 맵을 반환한다.")
        @Test
        void returnsEmptyMap_whenNoDataExists() {
            // arrange
            LocalDate calculationDate = LocalDate.of(2099, 12, 31);

            // act
            Map<String, Map<Integer, Double>> result = weatherService.findWeathersByBaseDate(calculationDate);

            // assert
            assertThat(result).isEmpty();
        }
    }

    @DisplayName("기온 조회 시,")
    @Nested
    class GetTemperature {

        @DisplayName("자치구와 시간에 해당하는 기온을 반환한다.")
        @Test
        void returnsTemperature_whenDataExists() {
            // arrange
            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();
            Map<Integer, Double> hourlyTemp = new HashMap<>();
            hourlyTemp.put(9, 20.0);
            districtHourlyTempMap.put("강남구", hourlyTemp);

            // act
            Double result = weatherService.getTemperature(districtHourlyTempMap, "강남구", 9);

            // assert
            assertThat(result).isEqualTo(20.0);
        }

        @DisplayName("자치구가 없으면 null을 반환한다.")
        @Test
        void returnsNull_whenDistrictNotFound() {
            // arrange
            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();

            // act
            Double result = weatherService.getTemperature(districtHourlyTempMap, "존재하지않는구", 9);

            // assert
            assertThat(result).isNull();
        }

        @DisplayName("시간이 없으면 null을 반환한다.")
        @Test
        void returnsNull_whenHourNotFound() {
            // arrange
            Map<String, Map<Integer, Double>> districtHourlyTempMap = new HashMap<>();
            Map<Integer, Double> hourlyTemp = new HashMap<>();
            hourlyTemp.put(9, 20.0);
            districtHourlyTempMap.put("강남구", hourlyTemp);

            // act
            Double result = weatherService.getTemperature(districtHourlyTempMap, "강남구", 10);

            // assert
            assertThat(result).isNull();
        }
    }
}
