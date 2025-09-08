package com.taja.station.presentation;

import com.taja.global.response.CommonApiResponse;
import com.taja.station.application.StationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class StationController {

    private final StationService stationService;

    @PostMapping("/stations/upload")
    public CommonApiResponse<String> readStationFile(@RequestParam("file") MultipartFile file) {
        int count = stationService.uploadStations(file);
        return CommonApiResponse.success(count + "개 대여소가 등록 및 수정되었습니다.");
    }
}
