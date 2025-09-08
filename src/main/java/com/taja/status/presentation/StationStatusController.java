package com.taja.status.presentation;

import com.taja.status.application.StationStatusApiService;
import com.taja.global.response.CommonApiResponse;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class StationStatusController {

    private final StationStatusApiService stationStatusApiService;

    @PostMapping("/station-status/upload")
    public CommonApiResponse<String> readStationStatus() {
        LocalDateTime now = LocalDateTime.now();
        stationStatusApiService.loadStationStatuses(now);
        return CommonApiResponse.success("대여소 실시간 상태가 등록되었습니다.");
    }

}
