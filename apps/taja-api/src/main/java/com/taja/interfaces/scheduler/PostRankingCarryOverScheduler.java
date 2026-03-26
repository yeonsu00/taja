package com.taja.interfaces.scheduler;

import com.taja.application.board.PostService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PostRankingCarryOverScheduler {

    private final PostService postService;

    @Scheduled(cron = "0 58 23 * * *")
    public void carryOverRankingScores() {
        log.info("===== 랭킹 Score Carry-Over 스케줄러 시작 (23:58) =====");
        LocalDate today = LocalDate.now();
        postService.updateTomorrowRankingScores(today);
    }
}
