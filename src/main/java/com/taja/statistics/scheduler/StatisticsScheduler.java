package com.taja.statistics.scheduler;

import com.taja.statistics.application.StatisticsFacade;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class StatisticsScheduler {

    private final StatisticsFacade statisticsFacade;

    @Scheduled(cron = "0 3 4 * * *")
    public void scheduleHourlyStatisticsCalculation() {
        LocalDate today = LocalDate.now();
        log.info("===== 시간대별 통계 계산 스케줄러 시작 : {} =====", today);

        statisticsFacade.calculateHourlyStatistics(today);
    }

    @Scheduled(cron = "0 13 4 * * *")
    public void scheduleDayOfWeekStatisticsCalculation() {
        LocalDate today = LocalDate.now();
        log.info("===== 요일별 통계 계산 스케줄러 시작 : {} =====", today);

        statisticsFacade.calculateDayOfWeekStatistics(today);
    }

    @Scheduled(cron = "0 23 4 * * *")
    public void scheduleTemperatureStatisticsCalculation() {
        LocalDate today = LocalDate.now();
        log.info("===== 기온별 통계 계산 스케줄러 시작 : {} =====", today);

        statisticsFacade.calculateTemperatureStatistics(today);
    }
}
