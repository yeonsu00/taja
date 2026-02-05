package com.taja.interfaces.api.status;

import com.taja.application.status.StationStatusService;
import com.taja.global.response.CommonApiResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Hidden
@RestController
@RequiredArgsConstructor
@RequestMapping("/station-status")
@Tag(name = "Station Status", description = "Station Status API")
public class StationStatusController {

    private final StationStatusService stationStatusService;

    @Operation(summary = "대여소 실시간 상태 업로드", description = "대여소 실시간 상태 정보를 수집하여 저장합니다.")
    @PostMapping("/upload")
    public CommonApiResponse<String> readStationStatus() {
        LocalDateTime now = LocalDateTime.now();
        stationStatusService.loadStationStatuses(now);
        return CommonApiResponse.success("대여소 실시간 상태가 등록되었습니다.");
    }

}
