package com.taja.weather.scheduler;

import com.taja.weather.application.WeatherService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherScheduler {

    private final WeatherService weatherService;

    @Scheduled(cron = "0 45 * * * *")
    public void scheduleWeatherCollection() {
        LocalDateTime scheduledTime = LocalDateTime.now();
        log.info("===== 초단기실황 날씨 정보 수집 스케줄러 시작 : {} =====", scheduledTime);

        try {
            weatherService.saveWeatherHistories(scheduledTime);
        } catch (Exception e) {
            log.error("초단기실황 날씨 정보 수집 스케줄링 작업 실행 중 오류가 발생했습니다.", e);
        }
    }

}
