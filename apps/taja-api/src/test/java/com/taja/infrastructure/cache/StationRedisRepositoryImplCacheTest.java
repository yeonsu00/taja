package com.taja.infrastructure.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.taja.application.cache.StationInfo;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import com.taja.infrastructure.station.StationJpaRepository;
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
@DisplayName("StationRedisRepositoryImpl 캐시 테스트")
class StationRedisRepositoryImplCacheTest {

    @Mock
    private StationHashRepository stationHashRepository;

    @Mock
    private StationJpaRepository stationJpaRepository;

    @InjectMocks
    private StationRedisRepositoryImpl stationRedisRepository;

    @DisplayName("캐시 미스 시 DB에서 배치 조회 후 캐시에 저장하고 반환한다")
    @Test
    void findStationInfos_whenCacheMiss_loadsFromDbInBatchAndSavesToCache() {
        // given
        Integer stationNumber = 101;
        double lat = 37.5665;
        double lon = 126.9780;

        when(stationHashRepository.findMissingNumbers(List.of(stationNumber)))
                .thenReturn(List.of(stationNumber));
        when(stationHashRepository.acquireBulkLoadLock()).thenReturn(true);

        Station station = createTestStation(stationNumber, lat, lon);
        when(stationJpaRepository.findAllByNumberIn(List.of(stationNumber)))
                .thenReturn(List.of(station));

        StationInfo.StationHashInfo cachedHashInfo = new StationInfo.StationHashInfo(
                stationNumber, 1L, "테스트 대여소 101", 0, LocalDateTime.now()
        );
        when(stationHashRepository.fetchAllFields(stationNumber))
                .thenReturn(Optional.of(cachedHashInfo));

        // when
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(stationNumber, lat, lon)
        );
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().number()).isEqualTo(stationNumber);
        verify(stationJpaRepository).findAllByNumberIn(List.of(stationNumber));
        verify(stationHashRepository).saveStationInfosWithPipeline(anyList(), any(LocalDateTime.class));
    }

    @DisplayName("캐시 히트 시 즉시 반환하고 DB 조회하지 않는다")
    @Test
    void getOrRefresh_whenCacheHit_returnsImmediately() {
        // given
        Integer stationNumber = 102;
        double lat = 37.5665;
        double lon = 126.9780;

        StationInfo.StationHashInfo hashInfo = new StationInfo.StationHashInfo(
                stationNumber, 1L, "테스트 대여소 102", 5, LocalDateTime.now()
        );
        when(stationHashRepository.fetchAllFields(stationNumber))
                .thenReturn(Optional.of(hashInfo));
        when(stationHashRepository.isThresholdReached(stationNumber))
                .thenReturn(false);  // TTL 충분

        // when
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(stationNumber, lat, lon)
        );
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().number()).isEqualTo(stationNumber);
        assertThat(results.getFirst().bikeCount()).isEqualTo(5);
        verify(stationJpaRepository, never()).findByNumber(anyInt());
        verify(stationHashRepository, never()).saveStationInfosWithPipeline(anyList(), any());
    }

    @DisplayName("TTL이 임계값보다 적을 때 현재 캐시 데이터를 반환하고 비동기로 갱신한다")
    @Test
    void getOrRefresh_whenThresholdReached_returnsCacheAndRefreshesAsync() throws InterruptedException {
        // given
        Integer stationNumber = 103;
        double lat = 37.5665;
        double lon = 126.9780;

        StationInfo.StationHashInfo hashInfo = new StationInfo.StationHashInfo(
                stationNumber, 1L, "테스트 대여소 103", 3, LocalDateTime.now()
        );
        when(stationHashRepository.fetchAllFields(stationNumber))
                .thenReturn(Optional.of(hashInfo));
        when(stationHashRepository.isThresholdReached(stationNumber))
                .thenReturn(true);  // TTL 임계값 도달

        // when
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(stationNumber, lat, lon)
        );
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then - 즉시 반환
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().number()).isEqualTo(stationNumber);

        // 비동기 갱신이 시작되었는지 확인 (약간의 대기 후)
        Thread.sleep(100);
        verify(stationHashRepository).isThresholdReached(stationNumber);
    }

    @DisplayName("분산락 획득 실패 시 갱신을 수행하지 않는다")
    @Test
    void refreshCacheWithLock_whenLockAcquisitionFails_skipsRefresh() throws InterruptedException {
        // given
        Integer stationNumber = 104;
        double lat = 37.5665;
        double lon = 126.9780;

        StationInfo.StationHashInfo hashInfo = new StationInfo.StationHashInfo(
                stationNumber, 1L, "테스트 대여소 104", 2, LocalDateTime.now()
        );
        when(stationHashRepository.fetchAllFields(stationNumber))
                .thenReturn(Optional.of(hashInfo));
        when(stationHashRepository.isThresholdReached(stationNumber))
                .thenReturn(true);
        when(stationHashRepository.acquireLock(stationNumber))
                .thenReturn(false);  // 락 획득 실패

        // when
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(stationNumber, lat, lon)
        );
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).hasSize(1);
        Thread.sleep(100);
        verify(stationHashRepository).acquireLock(stationNumber);
        verify(stationJpaRepository, never()).findByNumber(anyInt());
        verify(stationHashRepository, never()).saveStationInfosWithPipeline(anyList(), any());
    }

    @DisplayName("분산락 획득 성공 시 DB 조회 후 캐시를 갱신한다")
    @Test
    void refreshCacheWithLock_whenLockAcquired_refreshesCache() throws InterruptedException {
        // given
        Integer stationNumber = 105;
        double lat = 37.5665;
        double lon = 126.9780;

        StationInfo.StationHashInfo hashInfo = new StationInfo.StationHashInfo(
                stationNumber, 1L, "테스트 대여소 105", 1, LocalDateTime.now()
        );
        when(stationHashRepository.fetchAllFields(stationNumber))
                .thenReturn(Optional.of(hashInfo));
        when(stationHashRepository.isThresholdReached(stationNumber))
                .thenReturn(true);
        when(stationHashRepository.acquireLock(stationNumber))
                .thenReturn(true);  // 락 획득 성공

        Station station = createTestStation(stationNumber, lat, lon);
        when(stationJpaRepository.findByNumber(stationNumber))
                .thenReturn(Optional.of(station));

        // when
        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(stationNumber, lat, lon)
        );
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).hasSize(1);
        Thread.sleep(200);  // 비동기 작업 완료 대기
        verify(stationHashRepository).acquireLock(stationNumber);
        verify(stationJpaRepository).findByNumber(stationNumber);
        verify(stationHashRepository).saveStationInfosWithPipeline(anyList(), any(LocalDateTime.class));
        verify(stationHashRepository).releaseLock(stationNumber);
    }

    @DisplayName("DB에도 대여소가 없으면 해당 대여소를 결과에서 제외한다")
    @Test
    void findStationInfos_whenStationNotFoundInDb_excludesStation() {
        // given
        Integer stationNumber = 999;
        double lat = 37.5665;
        double lon = 126.9780;

        when(stationHashRepository.findMissingNumbers(List.of(stationNumber)))
                .thenReturn(List.of(stationNumber));
        when(stationHashRepository.acquireBulkLoadLock()).thenReturn(true);
        when(stationJpaRepository.findAllByNumberIn(List.of(stationNumber)))
                .thenReturn(List.of());  // DB에도 없음
        when(stationHashRepository.fetchAllFields(stationNumber))
                .thenReturn(Optional.empty());

        List<StationInfo.StationGeoInfo> geoInfos = List.of(
                new StationInfo.StationGeoInfo(stationNumber, lat, lon)
        );

        // when
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(geoInfos);

        // then
        assertThat(results).isEmpty();
        verify(stationJpaRepository).findAllByNumberIn(List.of(stationNumber));
    }

    @DisplayName("여러 대여소 조회 시 캐시 미스된 것만 DB에서 배치 조회한다")
    @Test
    void findStationInfos_whenMultipleStations_loadsOnlyMissingFromDb() {
        // given
        StationInfo.StationGeoInfo geo1 = new StationInfo.StationGeoInfo(201, 37.5665, 126.9780);
        StationInfo.StationGeoInfo geo2 = new StationInfo.StationGeoInfo(202, 37.5670, 126.9785);

        // 202번만 캐시 미스
        when(stationHashRepository.findMissingNumbers(List.of(201, 202)))
                .thenReturn(List.of(202));
        when(stationHashRepository.findMissingNumbers(List.of(202)))
                .thenReturn(List.of(202));
        when(stationHashRepository.acquireBulkLoadLock()).thenReturn(true);

        Station station2 = createTestStation(202, 37.5670, 126.9785);
        when(stationJpaRepository.findAllByNumberIn(List.of(202)))
                .thenReturn(List.of(station2));

        StationInfo.StationHashInfo hashInfo1 = new StationInfo.StationHashInfo(
                201, 1L, "테스트 대여소 201", 5, LocalDateTime.now()
        );
        StationInfo.StationHashInfo hashInfo2 = new StationInfo.StationHashInfo(
                202, 2L, "테스트 대여소 202", 0, null
        );
        when(stationHashRepository.fetchAllFields(201)).thenReturn(Optional.of(hashInfo1));
        when(stationHashRepository.fetchAllFields(202)).thenReturn(Optional.of(hashInfo2));

        // when
        List<StationInfo.StationFullInfo> results = stationRedisRepository.findStationInfos(List.of(geo1, geo2));

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).number()).isEqualTo(201);
        assertThat(results.get(1).number()).isEqualTo(202);
        verify(stationJpaRepository).findAllByNumberIn(List.of(202));  // 미스된 것만 배치 조회
    }

    @DisplayName("update 시 캐시 누락된 대여소가 있으면 DB에서 정적 정보를 조회해 재적재한 뒤 갱신한다")
    @Test
    void updateBikeCountAndRequestedAt_whenSomeKeysMissing_refillsStaticFieldsThenUpdates() {
        // given
        StationStatus status1 = createTestStatus(301, 5);
        StationStatus status2 = createTestStatus(302, 8);
        List<StationStatus> statuses = List.of(status1, status2);

        // 302번만 캐시 누락 상태
        when(stationHashRepository.findMissingNumbers(List.of(301, 302)))
                .thenReturn(List.of(302));

        Station station302 = createTestStation(302, 37.5670, 126.9785);
        when(stationJpaRepository.findAllByNumberIn(List.of(302)))
                .thenReturn(List.of(station302));

        // when
        stationRedisRepository.updateBikeCountAndRequestedAtWithPipeline(statuses);

        // then
        verify(stationHashRepository).findMissingNumbers(List.of(301, 302));
        verify(stationJpaRepository).findAllByNumberIn(List.of(302));
        verify(stationHashRepository).saveStationInfosWithPipeline(eq(List.of(station302)), any(LocalDateTime.class));
        verify(stationHashRepository).updateBikeCountAndRequestedAtWithPipeline(statuses);
    }

    @DisplayName("update 시 모든 캐시가 존재하면 DB 조회 없이 갱신만 수행한다")
    @Test
    void updateBikeCountAndRequestedAt_whenAllKeysPresent_skipsDbLookup() {
        // given
        StationStatus status1 = createTestStatus(401, 3);
        StationStatus status2 = createTestStatus(402, 7);
        List<StationStatus> statuses = List.of(status1, status2);

        when(stationHashRepository.findMissingNumbers(List.of(401, 402)))
                .thenReturn(List.of());

        // when
        stationRedisRepository.updateBikeCountAndRequestedAtWithPipeline(statuses);

        // then
        verify(stationHashRepository).findMissingNumbers(List.of(401, 402));
        verify(stationJpaRepository, never()).findAllByNumberIn(anyList());
        verify(stationHashRepository, never()).saveStationInfosWithPipeline(anyList(), any());
        verify(stationHashRepository).updateBikeCountAndRequestedAtWithPipeline(statuses);
    }

    private StationStatus createTestStatus(Integer stationNumber, Integer bikeCount) {
        return StationStatus.builder()
                .stationNumber(stationNumber)
                .parkingBikeCount(bikeCount)
                .requestedDate(LocalDate.now())
                .requestedTime(LocalTime.now())
                .build();
    }

    private Station createTestStation(Integer number, double lat, double lon) {
        return Station.builder()
                .stationId(1L)
                .number(number)
                .name("테스트 대여소 " + number)
                .district("강남구")
                .address("테스트 주소")
                .latitude(lat)
                .longitude(lon)
                .operationMode(OperationMode.LCD_QR)
                .build();
    }
}
