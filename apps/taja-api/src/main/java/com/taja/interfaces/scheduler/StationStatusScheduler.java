package com.taja.interfaces.scheduler;

import com.taja.application.status.StationStatusService;
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
    private final StationInitializationHolder stationInitializationHolder;

    @Scheduled(cron = "0 0/10 * * * *")
    public void scheduleStationStatusCollection() {
        if (!stationInitializationHolder.isInitialized()) {
            log.debug("대여소 정보 초기화 미완료로 대여소 실시간 상태 수집 스킵");
            return;
        }
        LocalDateTime scheduledTime = LocalDateTime.now();
        log.info("===== 대여소 실시간 상태 수집 스케줄러 시작 : {} =====", scheduledTime);

        try {
            stationStatusService.loadStationStatuses(scheduledTime);
        } catch (Exception e) {
            log.error("대여소 실시간 상태 수집 스케줄링 작업 실행 중 오류가 발생했습니다.", e);
        }
    }

}
