package com.taja.status.scheduler;

import com.taja.status.application.StationStatusApiService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationStatusScheduler {

    private final StationStatusApiService stationStatusApiService;

    @Scheduled(cron = "0 0/5 * * * *")
    public void scheduleStationStatusCollection() {
        LocalDateTime scheduledTime = LocalDateTime.now();
        log.info("===== 대여소 실시간 상태 수집 스케줄러 시작 : {} =====", scheduledTime);

        try {
            stationStatusApiService.loadStationStatuses(scheduledTime);
        } catch (Exception e) {
            log.error("대여소 실시간 상태 수집 스케줄링 작업 실행 중 오류가 발생했습니다.", e);
        }
    }

}
