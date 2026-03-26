package com.taja.interfaces.listener;

import com.taja.application.status.StationStatusHourlyAvgService;
import com.taja.application.station.event.StationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationStatusHourlyAvgEventListener {

    private final StationStatusHourlyAvgService stationStatusHourlyAvgService;

    @Async
    @EventListener
    public void handleStationStatusesCollected(StationEvent.StationStatusesCollected event) {
        log.info("StationStatusesCollected 이벤트 수신 - 일별/시간별 평균 갱신 시작, requestedAt: {}", event.requestedAt());
        stationStatusHourlyAvgService.updateHourlyAvgByRequestedAt(event.requestedAt());
    }
}
