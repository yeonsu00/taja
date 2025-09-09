package com.taja.station.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.taja.station.presentation.response.NearbyStationResponse;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StationServiceTest {

    @Mock
    private StationRedisRepository stationRedisRepository;

    @InjectMocks
    private StationService stationService;

    @DisplayName("latDelta와 lonDelta가 0일 때 빈 리스트를 반환한다.")
    @Test
    void findNearbyStations() {
        // given
        double centerLat = 37.5665;
        double centerLon = 126.9780;
        double latDelta = 0.0;
        double lonDelta = 0.0;

        // when
        List<NearbyStationResponse> result = stationService.findNearbyStations(centerLat, centerLon, latDelta, lonDelta);


        // then
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @DisplayName("stationRedisRepository를 호출할 때 latDelta와 lonDelta의 2배 값을 넘겨준다.")
    @Test
    void findNearbyStations_call_stationRedisRepository() {
        // given
        double centerLat = 37.5665;
        double centerLon = 126.9780;
        double latDelta = 0.005;
        double lonDelta = 0.01;

        double expectedHeight = latDelta * 2;
        double expectedWidth = lonDelta * 2;

        // when
        stationService.findNearbyStations(centerLat, centerLon, latDelta, lonDelta);

        // then
        verify(stationRedisRepository).findNearbyStations(centerLat, centerLon, expectedHeight, expectedWidth);
    }

    @DisplayName("주변 대여소 정보를 성공적으로 조회하고 반환한다.")
    @Test
    void findNearbyStations_whenStationsExist_returnStationList() {
        // given
        double centerLat = 37.5665;
        double centerLon = 126.9780;
        double latDelta = 0.005;
        double lonDelta = 0.01;

        double height = latDelta * 2;
        double width = lonDelta * 2;

        LocalDateTime requestedAt = LocalDateTime.now();

        List<NearbyStationResponse> expectedResponse = List.of(
                new NearbyStationResponse(1, 37.5660, 126.9775, 1, requestedAt),
                new NearbyStationResponse(2, 37.5670, 126.9785, 2, requestedAt)
        );

        when(stationRedisRepository.findNearbyStations(centerLat, centerLon, height, width))
                .thenReturn(expectedResponse);

        // when
        List<NearbyStationResponse> actualResponse = stationService.findNearbyStations(centerLat, centerLon, latDelta, lonDelta);

        // then
        assertThat(actualResponse).hasSize(2);
        assertThat(actualResponse).isEqualTo(expectedResponse);
        assertThat(actualResponse.getFirst().number().intValue()).isEqualTo(1);
    }

}