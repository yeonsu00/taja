package com.taja.collector.scheduler;

import com.taja.collector.application.StationStatusService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationStatusScheduler {

    private final StationStatusService stationStatusService;

    @Scheduled(cron = "0 0/2 * * * *")
    public void scheduleStationStatusCollection() {
        log.info("===== 대여소 실시간 현황 수집 스케줄러 시작 =====");

        LocalDateTime scheduledTime = LocalDateTime.now();
        log.info("스케줄 실행 시각: {}", scheduledTime);

        try {
            stationStatusService.loadStationStatuses(scheduledTime);
        } catch (Exception e) {
            log.error("스케줄링 작업 실행 중 오류가 발생했습니다.", e);
        }
    }

}
