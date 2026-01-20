package com.taja.interfaces.api.statistics;

import com.taja.global.response.CommonApiResponse;
import com.taja.application.statistics.StatisticsFacade;
import com.taja.interfaces.api.statistics.request.StatisticsRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
@Tag(name = "Statistics", description = "Statistics API")
public class StatisticsController {

    private final StatisticsFacade statisticsFacade;

    @Operation(summary = "시간대별 통계 계산", description = "해당 날짜의 시간대별 통계를 계산하여 업데이트합니다.")
    @PostMapping("/hourly")
    public CommonApiResponse<String> calculateHourlyStatistics(@Valid @RequestBody StatisticsRequest request) {
        int count = statisticsFacade.calculateHourlyStatistics(request.requestedAt());
        return CommonApiResponse.success(count + "개 대여소의 시간대별 통계 계산이 완료되었습니다.");
    }

    @Operation(summary = "요일별 통계 계산", description = "해당 날짜의 요일별 통계를 계산하여 업데이트합니다.")
    @PostMapping("/day-of-week")
    public CommonApiResponse<String> calculateDayOfWeekStatistics(@Valid @RequestBody StatisticsRequest request) {
        int count = statisticsFacade.calculateDayOfWeekStatistics(request.requestedAt());
        return CommonApiResponse.success(count + "개 대여소의 요일별 통계 계산이 완료되었습니다.");
    }

    @Operation(summary = "기온별 통계 계산", description = "해당 날짜의 기온별 통계를 계산하여 업데이트합니다.")
    @PostMapping("/temperature")
    public CommonApiResponse<String> calculateTemperatureStatistics(@Valid @RequestBody StatisticsRequest request) {
        int count = statisticsFacade.calculateTemperatureStatistics(request.requestedAt());
        return CommonApiResponse.success(count + "개 대여소의 기온별 통계 계산이 완료되었습니다.");
    }

}
