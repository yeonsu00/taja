package com.taja.infrastructure.station;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.taja.application.cache.StationInfo;
import com.taja.application.cache.StationInfo.BikeCountInfo;
import com.taja.application.status.StationStatusRepository;
import com.taja.domain.status.StationStatus;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.global.exception.StationNotFoundException;
import com.taja.infrastructure.cache.StationGeoRepository;
import com.taja.infrastructure.cache.StationHashRepository;
import com.taja.infrastructure.cache.StationRedisRepositoryImpl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Mock
    private StationStatusRepository stationStatusRepository;

    @InjectMocks
    private StationRedisRepositoryImpl stationRedisRepository;

    @DisplayName("주변 대여소의 GEO 정보를 성공적으로 조회한다.")
    @Test
    void findStations_WithinShape_success() {
        // given
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.501, 127.001),
                new StationInfo.StationGeoInfo(102, 37.502, 127.002)
        );
        when(stationGeoRepository.findStationsWithinBox(37.5, 127.0, 1.0, 1.0))
                .thenReturn(geoInfos);

        // when
        List<StationInfo.StationGeoInfo> results = stationRedisRepository.findStationsWithinBox(37.5, 127.0, 1.0, 1.0);

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

        StationInfo.StationHashInfo hashInfo1 = new StationInfo.StationHashInfo(101, 1L, "대여소1", 10, requestedAt);
        StationInfo.StationHashInfo hashInfo2 = new StationInfo.StationHashInfo(102, 2L, "대여소2", 5, requestedAt);

        when(stationHashRepository.fetchAllFields(101))
                .thenReturn(Optional.of(hashInfo1));
        when(stationHashRepository.fetchAllFields(102))
                .thenReturn(Optional.of(hashInfo2));
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
    void findStations_WithinShape_whenGeoResultIsNull() {
        // given
        when(stationGeoRepository.findStationsWithinBox(37.5, 127.0, 1.0, 1.0))
                .thenReturn(List.of());

        // when
        List<StationInfo.StationGeoInfo> results = stationRedisRepository.findStationsWithinBox(37.5, 127.0, 1.0, 1.0);

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

        // 캐시에 데이터가 없음 → 저장 후 재조회
        StationInfo.StationHashInfo cachedHashInfo = new StationInfo.StationHashInfo(201, 1L, "테스트 대여소", 0, null);
        when(stationHashRepository.fetchAllFields(201))
                .thenReturn(Optional.empty())  // 첫 번째 호출: 캐시 미스
                .thenReturn(Optional.of(cachedHashInfo));  // 두 번째 호출: 캐시 저장 후 조회

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
    void findStations_WithinShape_excludeStation_whenMemberIsNotNumeric() {
        // given
        // StationGeoRepository에서 이미 필터링된 결과를 반환한다고 가정
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(101, 37.501, 127.001)
        );
        when(stationGeoRepository.findStationsWithinBox(37.5, 127.0, 1.0, 1.0))
                .thenReturn(geoInfos);

        // when
        List<StationInfo.StationGeoInfo> results = stationRedisRepository.findStationsWithinBox(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().number()).isEqualTo(101);
    }

    @DisplayName("날짜 형식이 잘못되었을 때, 예외가 발생한다.")
    @Test
    void findStationInfos_whenDateFormatIsInvalid_throwsException() {
        // given
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(102, 37.503, 127.003)
        );

        // StationHashRepository에서 파싱 실패로 Optional.empty() 반환
        when(stationHashRepository.fetchAllFields(102))
                .thenReturn(Optional.empty());
        when(stationJpaRepository.findByNumber(102))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stationRedisRepository.findStationInfos(geoInfos))
                .isInstanceOf(StationNotFoundException.class)
                .hasMessageContaining("102 번 대여소를 찾을 수 없습니다");
        verify(stationJpaRepository).findByNumber(102);
        verify(stationHashRepository, never()).saveStationInfosWithPipeline(anyList(), any());
    }

    @DisplayName("getStationStatusByNumber는 Redis에 데이터가 있으면 Redis 값을 반환한다")
    @Test
    void getStationStatusByNumber_whenRedisHasData_returnsFromRedis() {
        // given
        Integer stationNumber = 101;
        Station station = Station.builder()
                .stationId(1L)
                .number(stationNumber)
                .name("테스트 대여소")
                .district("강남구")
                .address("테스트 주소")
                .latitude(37.6)
                .longitude(127.1)
                .operationMode(OperationMode.LCD_QR)
                .build();
        LocalDateTime requestedAt = LocalDateTime.of(2025, 8, 20, 14, 39, 0);
        StationInfo.StationHashInfo hashInfo = new StationInfo.StationHashInfo(stationNumber, 1L, "테스트 대여소", 7, requestedAt);

        when(stationJpaRepository.findByNumber(stationNumber)).thenReturn(Optional.of(station));
        when(stationHashRepository.fetchAllFields(stationNumber)).thenReturn(Optional.of(hashInfo));

        // when
        BikeCountInfo result = stationRedisRepository.getStationStatusByNumber(stationNumber);

        // then
        assertThat(result.stationId()).isEqualTo(1L);
        assertThat(result.availableBikeCount()).isEqualTo(7);
        assertThat(result.requestedAt()).isEqualTo(requestedAt);
        verify(stationStatusRepository, never()).findLatestByStationNumber(anyInt());
    }

    @DisplayName("getStationStatusByNumber는 Redis에 없고 DB에 있으면 DB 값을 반환한다")
    @Test
    void getStationStatusByNumber_whenRedisMiss_returnsFromDb() {
        // given
        Integer stationNumber = 102;
        Station station = Station.builder()
                .stationId(2L)
                .number(stationNumber)
                .name("테스트 대여소2")
                .district("서초구")
                .address("테스트 주소2")
                .latitude(37.5)
                .longitude(127.0)
                .operationMode(OperationMode.LCD_QR)
                .build();
        StationStatus dbStatus = StationStatus.builder()
                .stationNumber(stationNumber)
                .parkingBikeCount(5)
                .requestedDate(LocalDate.of(2025, 8, 20))
                .requestedTime(LocalTime.of(14, 39, 0))
                .build();

        when(stationJpaRepository.findByNumber(stationNumber)).thenReturn(Optional.of(station));
        when(stationHashRepository.fetchAllFields(stationNumber)).thenReturn(Optional.empty());
        when(stationStatusRepository.findLatestByStationNumber(stationNumber))
                .thenReturn(Optional.of(dbStatus));

        // when
        BikeCountInfo result = stationRedisRepository.getStationStatusByNumber(stationNumber);

        // then
        assertThat(result.stationId()).isEqualTo(2L);
        assertThat(result.availableBikeCount()).isEqualTo(5);
        assertThat(result.requestedAt()).isEqualTo(
                LocalDateTime.of(2025, 8, 20, 14, 39, 0));
    }

    @DisplayName("getStationStatusByNumber는 Redis와 DB 모두 없으면 0과 현재 시각으로 반환한다")
    @Test
    void getStationStatusByNumber_whenRedisAndDbBothEmpty_returnsZeroAndNow() {
        // given
        Integer stationNumber = 103;
        Station station = Station.builder()
                .stationId(3L)
                .number(stationNumber)
                .name("테스트 대여소3")
                .district("송파구")
                .address("테스트 주소3")
                .latitude(37.5)
                .longitude(127.0)
                .operationMode(OperationMode.LCD_QR)
                .build();

        when(stationJpaRepository.findByNumber(stationNumber)).thenReturn(Optional.of(station));
        when(stationHashRepository.fetchAllFields(stationNumber)).thenReturn(Optional.empty());
        when(stationStatusRepository.findLatestByStationNumber(stationNumber)).thenReturn(Optional.empty());

        // when
        BikeCountInfo result = stationRedisRepository.getStationStatusByNumber(stationNumber);

        // then
        assertThat(result.stationId()).isEqualTo(3L);
        assertThat(result.availableBikeCount()).isEqualTo(0);
        assertThat(result.requestedAt()).isNotNull();
    }

    @DisplayName("getStationStatusByNumber는 대여소 번호가 없으면 StationNotFoundException을 던진다")
    @Test
    void getStationStatusByNumber_whenStationNotFound_throwsException() {
        // given
        Integer stationNumber = 999;
        when(stationJpaRepository.findByNumber(stationNumber)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> stationRedisRepository.getStationStatusByNumber(stationNumber))
                .isInstanceOf(StationNotFoundException.class)
                .hasMessageContaining("999");
    }
}
