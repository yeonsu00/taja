package com.taja.collector.presentation;

import com.taja.collector.application.StationStatusService;
import com.taja.global.response.CommonApiResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StationStatusController {

    private final StationStatusService stationStatusService;

    @PostMapping("/station-status/upload")
    public CommonApiResponse<String> readStationStatus() {
        LocalDateTime now = LocalDateTime.now();
        stationStatusService.loadStationStatuses(now);
        return CommonApiResponse.success("대여소 실시간 상태가 등록되었습니다.");
    }

}
