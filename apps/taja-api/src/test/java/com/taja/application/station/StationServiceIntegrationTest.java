package com.taja.application.station;

import static org.assertj.core.api.Assertions.assertThat;

import com.taja.domain.station.Station;
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
class StationServiceIntegrationTest {

    @Autowired
    private StationService stationService;

    @DisplayName("모든 대여소 조회 시,")
    @Nested
    class FindAllStations {

        @DisplayName("모든 대여소를 조회할 수 있다.")
        @Test
        void findsAllStations() {
            // act
            List<Station> result = stationService.findAllStations();

            // assert
            assertThat(result).isNotNull();
        }
    }

}
