package com.taja.interfaces.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class StationInitializationScheduler implements ApplicationRunner {

    private final StationScheduler stationScheduler;
    private final StationStatusScheduler stationStatusScheduler;

    @Override
    public void run(ApplicationArguments args) {
        log.info("===== 서버 시작 시 대여소 정보 수집 실행 =====");
        stationScheduler.initializeStationCollection();
        log.info("===== 대여소 정보 수집 완료, 대여소 실시간 상태 수집 1회 실행 =====");
        stationStatusScheduler.scheduleStationStatusCollection();
    }
}
