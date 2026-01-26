package com.taja.interfaces.listener;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.taja.application.cache.StationCacheService;
import com.taja.application.station.StationService;
import com.taja.application.station.event.StationEvent;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("StationCacheEventListener 테스트")
class StationCacheEventListenerTest {

    @Mock
    private StationCacheService stationCacheService;

    @Mock
    private StationService stationService;

    @InjectMocks
    private StationCacheEventListener stationCacheEventListener;

    @DisplayName("StationsSaved 이벤트 수신 시 DB에서 조회 후 StationCacheService.saveStations를 호출한다")
    @Test
    void handleStationsSaved_callsSaveStations() {
        // given
        LocalDateTime requestedAt = LocalDateTime.now();
        List<Station> eventStations = List.of(
                createTestStation(101, 37.5665, 126.9780),
                createTestStation(102, 37.5670, 126.9785)
        );
        StationEvent.StationsSaved event = new StationEvent.StationsSaved(eventStations, requestedAt);

        // DB에서 조회한 최신 상태
        List<Station> freshStations = List.of(
                createTestStation(101, 37.5665, 126.9780),
                createTestStation(102, 37.5670, 126.9785)
        );

        when(stationService.findStationByNumbers(List.of(101, 102)))
                .thenReturn(freshStations);

        // when
        stationCacheEventListener.handleStationsSaved(event);

        // then
        verify(stationService, times(1)).findStationByNumbers(List.of(101, 102));
        verify(stationCacheService, times(1)).saveStations(freshStations, requestedAt);
        verify(stationCacheService, never()).updateBikeCountAndRequestedAt(anyList());
    }

    @DisplayName("StationsSaved 이벤트 수신 시 Redis 저장 실패해도 처리한다")
    @Test
    void handleStationsSaved_handlesRedisException() {
        // given
        LocalDateTime requestedAt = LocalDateTime.now();
        List<Station> eventStations = List.of(createTestStation(104, 37.5665, 126.9780));
        StationEvent.StationsSaved event = new StationEvent.StationsSaved(eventStations, requestedAt);

        List<Station> freshStations = List.of(createTestStation(104, 37.5665, 126.9780));
        when(stationService.findStationByNumbers(List.of(104)))
                .thenReturn(freshStations);

        doThrow(new RuntimeException("Redis 연결 실패"))
                .when(stationCacheService).saveStations(anyList(), any(LocalDateTime.class));

        // when & then - 예외가 발생해도 메서드가 정상 종료되어야 함
        assertThatCode(() -> stationCacheEventListener.handleStationsSaved(event))
                .doesNotThrowAnyException();

        verify(stationService, times(1)).findStationByNumbers(List.of(104));
        verify(stationCacheService, times(1)).saveStations(freshStations, requestedAt);
    }

    @DisplayName("StationStatusesUpdated 이벤트 수신 시 StationCacheService.updateBikeCountAndRequestedAt를 호출한다")
    @Test
    void handleStationStatusesUpdated_callsUpdateBikeCountAndRequestedAt() {
        // given
        List<StationStatus> stationStatuses = List.of(
                createTestStationStatus(201, 5),
                createTestStationStatus(202, 10)
        );
        StationEvent.StationStatusesUpdated event = new StationEvent.StationStatusesUpdated(stationStatuses);

        // when
        stationCacheEventListener.handleStationStatusesUpdated(event);

        // then
        verify(stationCacheService, times(1)).updateBikeCountAndRequestedAt(stationStatuses);
        verify(stationCacheService, never()).saveStations(anyList(), any(LocalDateTime.class));
    }

    @DisplayName("StationStatusesUpdated 이벤트 수신 시 예외가 발생해도 처리한다")
    @Test
    void handleStationStatusesUpdated_handlesException() {
        // given
        List<StationStatus> stationStatuses = List.of(createTestStationStatus(203, 3));
        StationEvent.StationStatusesUpdated event = new StationEvent.StationStatusesUpdated(stationStatuses);

        doThrow(new RuntimeException("Redis 연결 실패"))
                .when(stationCacheService).updateBikeCountAndRequestedAt(anyList());

        // when & then - 예외가 발생해도 메서드가 정상 종료되어야 함
        assertThatCode(() -> stationCacheEventListener.handleStationStatusesUpdated(event))
                .doesNotThrowAnyException();

        verify(stationCacheService, times(1)).updateBikeCountAndRequestedAt(stationStatuses);
    }

    @DisplayName("빈 리스트가 포함된 StationsSaved 이벤트도 처리한다")
    @Test
    void handleStationsSaved_withEmptyList() {
        // given
        LocalDateTime requestedAt = LocalDateTime.now();
        List<Station> emptyStations = List.of();
        StationEvent.StationsSaved event = new StationEvent.StationsSaved(emptyStations, requestedAt);

        when(stationService.findStationByNumbers(List.of()))
                .thenReturn(List.of());

        // when
        stationCacheEventListener.handleStationsSaved(event);

        // then
        verify(stationService, times(1)).findStationByNumbers(List.of());
        verify(stationCacheService, times(1)).saveStations(List.of(), requestedAt);
    }

    @DisplayName("DB에서 조회한 대여소가 이벤트의 대여소와 다른 경우도 처리한다")
    @Test
    void handleStationsSaved_whenDbReturnsDifferentStations() {
        // given
        LocalDateTime requestedAt = LocalDateTime.now();
        List<Station> eventStations = List.of(
                createTestStation(201, 37.5665, 126.9780),
                createTestStation(202, 37.5670, 126.9785)
        );
        StationEvent.StationsSaved event = new StationEvent.StationsSaved(eventStations, requestedAt);

        // DB에서 조회한 결과가 다를 수 있음 (예: 일부만 조회됨)
        List<Station> dbStations = List.of(
                createTestStation(201, 37.5665, 126.9780)
        );

        when(stationService.findStationByNumbers(List.of(201, 202)))
                .thenReturn(dbStations);

        // when
        stationCacheEventListener.handleStationsSaved(event);

        // then
        verify(stationService, times(1)).findStationByNumbers(List.of(201, 202));
        verify(stationCacheService, times(1)).saveStations(dbStations, requestedAt);
    }

    @DisplayName("DB에서 조회한 대여소가 빈 리스트인 경우도 처리한다")
    @Test
    void handleStationsSaved_whenDbReturnsEmpty() {
        // given
        LocalDateTime requestedAt = LocalDateTime.now();
        List<Station> eventStations = List.of(createTestStation(301, 37.5665, 126.9780));
        StationEvent.StationsSaved event = new StationEvent.StationsSaved(eventStations, requestedAt);

        when(stationService.findStationByNumbers(List.of(301)))
                .thenReturn(List.of());  // DB에 없음

        // when
        stationCacheEventListener.handleStationsSaved(event);

        // then
        verify(stationService, times(1)).findStationByNumbers(List.of(301));
        verify(stationCacheService, times(1)).saveStations(List.of(), requestedAt);
    }

    @DisplayName("빈 리스트가 포함된 StationStatusesUpdated 이벤트도 처리한다")
    @Test
    void handleStationStatusesUpdated_withEmptyList() {
        // given
        List<StationStatus> emptyStatuses = List.of();
        StationEvent.StationStatusesUpdated event = new StationEvent.StationStatusesUpdated(emptyStatuses);

        // when
        stationCacheEventListener.handleStationStatusesUpdated(event);

        // then
        verify(stationCacheService, times(1)).updateBikeCountAndRequestedAt(emptyStatuses);
    }

    @DisplayName("여러 이벤트가 순차적으로 처리된다")
    @Test
    void handlesMultipleEvents_sequentially() {
        // given
        LocalDateTime requestedAt = LocalDateTime.now();
        List<Station> eventStations = List.of(createTestStation(301, 37.5665, 126.9780));
        StationEvent.StationsSaved savedEvent = new StationEvent.StationsSaved(eventStations, requestedAt);

        List<Station> dbStations = List.of(createTestStation(301, 37.5665, 126.9780));
        when(stationService.findStationByNumbers(List.of(301)))
                .thenReturn(dbStations);

        List<StationStatus> statuses = List.of(createTestStationStatus(301, 5));
        StationEvent.StationStatusesUpdated updatedEvent = new StationEvent.StationStatusesUpdated(statuses);

        // when
        stationCacheEventListener.handleStationsSaved(savedEvent);
        stationCacheEventListener.handleStationStatusesUpdated(updatedEvent);

        // then
        verify(stationService, times(1)).findStationByNumbers(List.of(301));
        verify(stationCacheService, times(1)).saveStations(dbStations, requestedAt);
        verify(stationCacheService, times(1)).updateBikeCountAndRequestedAt(statuses);
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

    private StationStatus createTestStationStatus(Integer stationNumber, Integer bikeCount) {
        return StationStatus.builder()
                .stationNumber(stationNumber)
                .parkingBikeCount(bikeCount)
                .requestedDate(LocalDate.now())
                .requestedTime(LocalTime.now())
                .build();
    }
}
