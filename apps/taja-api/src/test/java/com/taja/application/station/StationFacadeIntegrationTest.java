package com.taja.application.station;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.taja.application.board.BoardInfo;
import com.taja.application.board.PostService;
import com.taja.application.cache.StationCacheService;
import com.taja.application.cache.StationInfo;
import com.taja.application.status.StationStatusFacade;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.interfaces.api.station.response.NearbyStationsResponse;
import com.taja.interfaces.api.station.response.StationClusterResponse;
import com.taja.interfaces.api.station.response.detail.StationDetailResponse;
import com.taja.interfaces.api.station.response.detail.TodayAvailableBikeResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StationFacadeIntegrationTest {

    @Autowired
    private StationFacade stationFacade;

    @Autowired
    private StationRepository stationRepository;

    @MockitoBean
    private StationCacheService stationCacheService;

    @MockitoBean
    private StationStatusFacade stationStatusFacade;

    @MockitoBean
    private PostService postService;

    @DisplayName("대여소 상세 조회 시 이용 통계(hourlyAvailable, dailyAvailable, temperatureAvailable)가 포함된 응답을 반환한다.")
    @Test
    void findStationDetail_returnsResponseWithUtilizationStatistics() {
        // given - 대여소 저장
        Station station = Station.builder()
                .name("테스트 대여소")
                .number(999)
                .district("강남구")
                .address("서울시 강남구")
                .latitude(37.5)
                .longitude(127.0)
                .operationMode(OperationMode.LCD)
                .lcdHoldCount(10)
                .qrHoldCount(0)
                .totalHoldCount(10)
                .build();
        List<Station> saved = stationRepository.upsert(List.of(station));
        Long stationId = saved.getFirst().getStationId();
        assertThat(stationId).isNotNull();

        given(stationCacheService.findStationsInBounds(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(Collections.emptyList());
        given(stationCacheService.findStationInfos(any()))
                .willReturn(Collections.emptyList());
        given(stationStatusFacade.getTodayAvailableBike(anyLong(), anyInt(), any(LocalDateTime.class)))
                .willReturn(new TodayAvailableBikeResponse(
                        LocalDateTime.now(),
                        Collections.emptyList(),
                        Collections.emptyList()));
        given(postService.findRecentPosts(anyLong(), anyInt()))
                .willReturn(List.<BoardInfo.PostItem>of());

        // when
        StationDetailResponse response = stationFacade.findStationDetail(stationId, LocalDateTime.now());

        // then
        assertThat(response).isNotNull();
        assertThat(response.stationId()).isEqualTo(stationId);
        assertThat(response.hourlyAvailable()).isNotNull();
        assertThat(response.dailyAvailable()).isNotNull();
        assertThat(response.temperatureAvailable()).isNotNull();
    }

    @DisplayName("좁은 영역(delta < 0.03) 조회 시 viewType이 stations이고 개별 대여소 정보를 반환한다.")
    @Test
    void findStationsInBounds_narrowArea_returnsStations() {
        // given
        double centerLat = 37.5;
        double centerLon = 127.0;
        double latDelta = 0.01;
        double lonDelta = 0.01;

        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.501, 127.001),
                new StationInfo.StationGeoInfo(102, 37.502, 127.002)
        );

        LocalDateTime now = LocalDateTime.now();
        List<StationInfo.StationFullInfo> fullInfos = List.of(
                new StationInfo.StationFullInfo(1L, 101, 37.501, 127.001, 5, now),
                new StationInfo.StationFullInfo(2L, 102, 37.502, 127.002, 3, now)
        );

        given(stationCacheService.findStationsInBounds(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(geoInfos);
        given(stationCacheService.findStationInfos(geoInfos))
                .willReturn(fullInfos);

        // when
        NearbyStationsResponse response = stationFacade.findStationsInBounds(centerLat, centerLon, latDelta, lonDelta);

        // then
        assertThat(response.viewType()).isEqualTo("stations");
        assertThat(response.stations()).hasSize(2);
        assertThat(response.clusters()).isNull();
        verify(stationCacheService).findStationInfos(geoInfos);
    }

    @DisplayName("넓은 영역(delta >= 0.03) 조회 시 viewType이 clusters이고 Redis Hash 조회를 하지 않는다.")
    @Test
    void findStationsInBounds_wideArea_returnsClustersWithoutHashLookup() {
        // given
        double centerLat = 37.5;
        double centerLon = 127.0;
        double latDelta = 0.05;
        double lonDelta = 0.05;

        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.51, 127.01),
                new StationInfo.StationGeoInfo(102, 37.52, 127.02),
                new StationInfo.StationGeoInfo(103, 37.51, 127.01)
        );

        given(stationCacheService.findStationsInBounds(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(geoInfos);

        // when
        NearbyStationsResponse response = stationFacade.findStationsInBounds(centerLat, centerLon, latDelta, lonDelta);

        // then
        assertThat(response.viewType()).isEqualTo("clusters");
        assertThat(response.clusters()).isNotEmpty();
        assertThat(response.stations()).isNull();
        verify(stationCacheService, never()).findStationInfos(any());
    }

    @DisplayName("같은 셀에 속하는 대여소들은 하나의 클러스터로 묶인다.")
    @Test
    void findStationsInBounds_stationsInSameCell_groupedIntoOneCluster() {
        // given
        double centerLat = 37.5;
        double centerLon = 127.0;
        double latDelta = 0.05;
        double lonDelta = 0.05;

        // 같은 셀에 속하도록 아주 가까운 좌표 3개
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.501, 127.001),
                new StationInfo.StationGeoInfo(102, 37.502, 127.002),
                new StationInfo.StationGeoInfo(103, 37.501, 127.001)
        );

        given(stationCacheService.findStationsInBounds(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(geoInfos);

        // when
        NearbyStationsResponse response = stationFacade.findStationsInBounds(centerLat, centerLon, latDelta, lonDelta);

        // then
        assertThat(response.clusters()).hasSize(1);
        StationClusterResponse cluster = response.clusters().getFirst();
        assertThat(cluster.stationCount()).isEqualTo(3);
    }

    @DisplayName("서로 다른 셀에 속하는 대여소들은 별도의 클러스터로 분리된다.")
    @Test
    void findStationsInBounds_stationsInDifferentCells_separateClusters() {
        // given
        double centerLat = 37.5;
        double centerLon = 127.0;
        double latDelta = 0.05;
        double lonDelta = 0.05;

        // 서로 다른 셀에 속하도록 멀리 떨어진 좌표
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.46, 126.96),
                new StationInfo.StationGeoInfo(102, 37.54, 127.04)
        );

        given(stationCacheService.findStationsInBounds(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(geoInfos);

        // when
        NearbyStationsResponse response = stationFacade.findStationsInBounds(centerLat, centerLon, latDelta, lonDelta);

        // then
        assertThat(response.clusters()).hasSize(2);
        assertThat(response.clusters())
                .extracting(StationClusterResponse::stationCount)
                .containsExactlyInAnyOrder(1, 1);
    }

    @DisplayName("대여소가 없는 넓은 영역 조회 시 빈 클러스터 목록을 반환한다.")
    @Test
    void findStationsInBounds_wideAreaNoStations_returnsEmptyClusters() {
        // given
        given(stationCacheService.findStationsInBounds(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(Collections.emptyList());

        // when
        NearbyStationsResponse response = stationFacade.findStationsInBounds(37.5, 127.0, 0.05, 0.05);

        // then
        assertThat(response.viewType()).isEqualTo("clusters");
        assertThat(response.clusters()).isEmpty();
        assertThat(response.stations()).isNull();
    }
}
