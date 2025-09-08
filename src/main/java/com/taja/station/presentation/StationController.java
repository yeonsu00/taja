package com.taja.station.presentation;

import com.taja.global.response.CommonApiResponse;
import com.taja.station.application.StationService;
import com.taja.station.presentation.request.NearbyStationRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("stations")
public class StationController {

    private final StationService stationService;

    @PostMapping("/upload")
    public CommonApiResponse<String> readStationFile(@RequestParam("file") MultipartFile file) {
        LocalDateTime requestedAt = LocalDateTime.now();
        int count = stationService.uploadStationsFromFile(file, requestedAt);
        return CommonApiResponse.success(count + "개 대여소가 등록 및 수정되었습니다.");
    }

    @GetMapping("/nearby")
    public void findNearbyStations(@Valid @RequestBody NearbyStationRequest nearbyStationRequest) {

    }
}
