package com.taja.interfaces.listener;

import com.taja.application.cache.StationCacheService;
import com.taja.application.station.event.StationEvent.StationsSaved;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationCacheEventListener {

    private final StationCacheService stationCacheService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStationsSaved(StationsSaved event) {
        log.info("대여소 저장 이벤트 수신: {}개 대여소 캐시 갱신 시작", event.stations().size());
        stationCacheService.saveStations(event.stations(), event.requestedAt());
        log.info("대여소 캐시 갱신 완료: {}개", event.stations().size());
    }
}
