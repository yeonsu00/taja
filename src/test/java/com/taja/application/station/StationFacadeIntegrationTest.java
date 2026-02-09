package com.taja.application.station;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;

import com.taja.application.board.BoardInfo;
import com.taja.application.board.PostService;
import com.taja.application.cache.StationCacheService;
import com.taja.application.status.StationStatusFacade;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
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
}
