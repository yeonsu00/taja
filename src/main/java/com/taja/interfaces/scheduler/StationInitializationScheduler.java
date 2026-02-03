package com.taja.interfaces.scheduler;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationInitializationScheduler {

    private final StationScheduler stationScheduler;
    private final StationStatusScheduler stationStatusScheduler;

    @PostConstruct
    public void runStationInitialization() {
        log.info("===== 서버 시작 시 대여소 정보 수집 실행 =====");
        stationScheduler.initializeStationCollection();
        log.info("===== 대여소 정보 수집 완료, 대여소 실시간 상태 수집 1회 실행 =====");
        stationStatusScheduler.scheduleStationStatusCollection();
    }
}
