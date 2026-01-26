package com.taja.interfaces.listener;

import com.taja.application.cache.StationCacheService;
import com.taja.application.station.StationService;
import com.taja.application.station.event.StationEvent;
import com.taja.domain.station.Station;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationCacheEventListener {

    private final StationCacheService stationCacheService;
    private final StationService stationService;

    @Async
    @Transactional(readOnly = true)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStationsSaved(StationEvent.StationsSaved event) {
        log.info("대여소 저장 이벤트 수신: {}개 대여소 캐시 갱신 시작", event.stations().size());
        
        List<Integer> stationNumbers = event.stations().stream()
                .map(Station::getNumber)
                .collect(Collectors.toList());
        List<Station> stations = stationService.findStationByNumbers(stationNumbers);
        
        stationCacheService.saveStations(stations, event.requestedAt());
        log.info("대여소 캐시 갱신 완료: {}개", stations.size());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStationStatusesUpdated(StationEvent.StationStatusesUpdated event) {
        log.info("대여소 상태 업데이트 이벤트 수신: {}개 대여소 상태 캐시 갱신 시작", event.stationStatuses().size());
        stationCacheService.updateBikeCountAndRequestedAt(event.stationStatuses());
        log.info("대여소 상태 캐시 갱신 완료: {}개", event.stationStatuses().size());
    }
}
