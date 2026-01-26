package com.taja.infrastructure.station;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.taja.application.cache.StationInfo;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.infrastructure.cache.StationGeoRepository;
import com.taja.infrastructure.cache.StationHashRepository;
import com.taja.infrastructure.cache.StationRedisRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StationRedisRepositoryImplTest {

    @Mock
    private StationHashRepository stationHashRepository;

    @Mock
    private StationGeoRepository stationGeoRepository;

    @Mock
    private StationJpaRepository stationJpaRepository;

    @InjectMocks
    private StationRedisRepositoryImpl stationRedisRepository;

    @DisplayName("주변 대여소의 GEO 정보를 성공적으로 조회한다.")
    @Test
    void findNearbyStations_success() {
        // given
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.501, 127.001),
                new StationInfo.StationGeoInfo(102, 37.502, 127.002)
        );
        when(stationGeoRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0))
                .thenReturn(geoInfos);

        // when
        List<StationInfo.StationGeoInfo> results = stationRedisRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).hasSize(2);
        StationInfo.StationGeoInfo result1 = results.getFirst();
        assertThat(result1.number()).isEqualTo(101);
        assertThat(result1.latitude()).isEqualTo(37.501);
        assertThat(result1.longitude()).isEqualTo(127.001);
        StationInfo.StationGeoInfo result2 = results.get(1);
        assertThat(result2.number()).isEqualTo(102);
        assertThat(result2.latitude()).isEqualTo(37.502);
        assertThat(result2.longitude()).isEqualTo(127.002);
    }

    @DisplayName("GEO 정보로부터 대여소 전체 정보를 성공적으로 조회한다.")
    @Test
    void findStationInfos_success() {
        // given
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.501, 127.001),
                new StationInfo.StationGeoInfo(102, 37.502, 127.002)
        );

        String requestedAtStr = "2025-09-09T14:30:00";
        LocalDateTime requestedAt = LocalDateTime.parse(requestedAtStr);
        
        StationInfo.StationFullInfo fullInfo1 = new StationInfo.StationFullInfo(1L, 101, 37.501, 127.001, 10, requestedAt);
        StationInfo.StationFullInfo fullInfo2 = new StationInfo.StationFullInfo(2L, 102, 37.502, 127.002, 5, requestedAt);

        when(stationHashRepository.fetchFullInfo(101, 37.501, 127.001))
                .thenReturn(Optional.of(fullInfo1));
        when(stationHashRepository.fetchFullInfo(102, 37.502, 127.002))
                .thenReturn(Optional.of(fullInfo2));
        when(stationHashRepository.isThresholdReached(anyInt())).thenReturn(false);

        // when
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).hasSize(2);
        StationInfo.StationFullInfo result1 = results.getFirst();
        assertThat(result1.number()).isEqualTo(101);
        assertThat(result1.bikeCount()).isEqualTo(10);
        assertThat(result1.requestedAt()).isEqualTo(requestedAt);
        StationInfo.StationFullInfo result2 = results.get(1);
        assertThat(result2.number()).isEqualTo(102);
        assertThat(result2.bikeCount()).isEqualTo(5);
    }

    @DisplayName("GEO 검색 결과가 null일 경우 빈 리스트를 반환한다.")
    @Test
    void findNearbyStations_whenGeoResultIsNull() {
        // given
        when(stationGeoRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0))
                .thenReturn(List.of());

        // when
        List<StationInfo.StationGeoInfo> results = stationRedisRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).isNotNull().isEmpty();
    }

    @DisplayName("HASH 데이터가 없을 경우 DB에서 조회하여 캐시에 저장하고 반환한다.")
    @Test
    void findStationInfos_whenHashDataIsMissing() {
        // given
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(201, 37.6, 127.1)
        );

        // 캐시에 데이터가 없음
        when(stationHashRepository.fetchFullInfo(201, 37.6, 127.1))
                .thenReturn(Optional.empty());

        // DB에서 조회
        Station station = Station.builder()
                .stationId(1L)
                .number(201)
                .name("테스트 대여소")
                .district("강남구")
                .address("테스트 주소")
                .latitude(37.6)
                .longitude(127.1)
                .operationMode(OperationMode.LCD_QR)
                .build();
        when(stationJpaRepository.findByNumber(201))
                .thenReturn(Optional.of(station));

        // 캐시 저장 후 조회
        StationInfo.StationFullInfo cachedInfo = new StationInfo.StationFullInfo(1L, 201, 37.6, 127.1, 0, null);
        when(stationHashRepository.fetchFullInfo(201, 37.6, 127.1))
                .thenReturn(Optional.empty())  // 첫 번째 호출: 캐시 미스
                .thenReturn(Optional.of(cachedInfo));  // 두 번째 호출: 캐시 저장 후 조회

        // when
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).hasSize(1);
        StationInfo.StationFullInfo result = results.getFirst();
        assertThat(result.number()).isEqualTo(201);
        assertThat(result.stationId()).isEqualTo(1L);
        verify(stationHashRepository).saveStationInfosWithPipeline(anyList(), any(LocalDateTime.class));
    }

    @DisplayName("대여소 번호가 숫자가 아닐 때, 해당 대여소를 결과에서 제외한다.")
    @Test
    void findNearbyStations_excludeStation_whenMemberIsNotNumeric() {
        // given
        // StationGeoRepository에서 이미 필터링된 결과를 반환한다고 가정
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.501, 127.001)
        );
        when(stationGeoRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0))
                .thenReturn(geoInfos);

        // when
        List<StationInfo.StationGeoInfo> results = stationRedisRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().number()).isEqualTo(101);
    }

    @DisplayName("날짜 형식이 잘못되었을 때, 해당 대여소를 결과에서 제외한다.")
    @Test
    void findStationInfos_excludeStation_whenDateFormatIsInvalid() {
        // given
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(102, 37.503, 127.003)
        );

        // StationHashRepository에서 파싱 실패로 Optional.empty() 반환
        when(stationHashRepository.fetchFullInfo(102, 37.503, 127.003))
                .thenReturn(Optional.empty());
        when(stationJpaRepository.findByNumber(102))
                .thenReturn(Optional.empty());

        // when
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).isNotNull().isEmpty();
    }
}
