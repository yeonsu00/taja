package com.taja.station.scheduler;

import com.taja.station.application.StationApiService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StationScheduler {

    private final StationApiService stationApiService;

    @Scheduled(cron = "0 0/1 * * * *")
    public void scheduleStationCollection() {
        LocalDateTime scheduledTime = LocalDateTime.now();
        log.info("===== 대여소 정보 수집 스케줄러 시작 : {} =====", scheduledTime);

        try {
            stationApiService.loadStations(scheduledTime);
        } catch (Exception e) {
            log.error("대여소 정보 수집 스케줄링 작업 실행 중 오류가 발생했습니다.", e);
        }

    }

}
