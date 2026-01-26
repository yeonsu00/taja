package com.taja.application.station;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.taja.application.cache.StationInfo;
import com.taja.application.cache.StationRedisRepository;
import com.taja.interfaces.api.station.response.MapStationResponse;
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

        double height = latDelta * 2;
        double width = lonDelta * 2;

        when(stationRedisRepository.findNearbyStations(centerLat, centerLon, height, width))
                .thenReturn(List.of());
        when(stationRedisRepository.findStationInfos(List.of()))
                .thenReturn(List.of());

        // when
        List<MapStationResponse> result = stationService.findNearbyStations(centerLat, centerLon, latDelta, lonDelta);

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

        when(stationRedisRepository.findNearbyStations(centerLat, centerLon, expectedHeight, expectedWidth))
                .thenReturn(List.of());
        when(stationRedisRepository.findStationInfos(List.of()))
                .thenReturn(List.of());

        // when
        stationService.findNearbyStations(centerLat, centerLon, latDelta, lonDelta);

        // then
        verify(stationRedisRepository).findNearbyStations(centerLat, centerLon, expectedHeight, expectedWidth);
        verify(stationRedisRepository).findStationInfos(List.of());
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

        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(1, 37.5660, 126.9775),
                new StationInfo.StationGeoInfo(2, 37.5670, 126.9785)
        );

        List<StationInfo.StationFullInfo> stationInfos = List.of(
                new StationInfo.StationFullInfo(1L, 1, 37.5660, 126.9775, 1, requestedAt),
                new StationInfo.StationFullInfo(2L, 2, 37.5670, 126.9785, 2, requestedAt)
        );

        when(stationRedisRepository.findNearbyStations(centerLat, centerLon, height, width))
                .thenReturn(geoInfos);
        when(stationRedisRepository.findStationInfos(geoInfos))
                .thenReturn(stationInfos);

        // when
        List<MapStationResponse> actualResponse = stationService.findNearbyStations(centerLat, centerLon, latDelta,
                lonDelta);

        // then
        assertThat(actualResponse).hasSize(2);
        assertThat(actualResponse.getFirst().number().intValue()).isEqualTo(1);
        assertThat(actualResponse.getFirst().bikeCount()).isEqualTo(1);
        assertThat(actualResponse.get(1).number().intValue()).isEqualTo(2);
        assertThat(actualResponse.get(1).bikeCount()).isEqualTo(2);
    }

}