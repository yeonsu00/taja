package com.taja.station.presentation;

import com.taja.global.response.CommonApiResponse;
import com.taja.station.application.StationService;
import com.taja.station.presentation.request.NearbyStationRequest;
import com.taja.station.presentation.request.SearchStationRequest;
import com.taja.station.presentation.response.NearbyStationResponse;
import com.taja.station.presentation.response.SearchStationResponse;
import com.taja.station.presentation.response.detail.StationDetailResponse;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/map/nearby")
    public CommonApiResponse<List<NearbyStationResponse>> findNearbyStations(
            @Valid @ModelAttribute NearbyStationRequest nearbyStationRequest) {
        List<NearbyStationResponse> nearbyStations = stationService.findNearbyStations(
                nearbyStationRequest.latitude(),
                nearbyStationRequest.longitude(),
                nearbyStationRequest.latDelta(),
                nearbyStationRequest.lonDelta()
        );

        return CommonApiResponse.success(nearbyStations, "근처 대여소 조회에 성공했습니다.");
    }

    @GetMapping("/map/search")
    public CommonApiResponse<List<SearchStationResponse>> searchStation(
            @Valid @ModelAttribute SearchStationRequest searchStationRequest) {
        double centerLat = searchStationRequest.lat();
        double centerLon = searchStationRequest.lon();

        List<SearchStationResponse> searchedStations = stationService.searchStationsByName(
                searchStationRequest.keyword(), centerLat, centerLon);

        return CommonApiResponse.success(searchedStations, "대여소 검색에 성공했습니다.");
    }

    @GetMapping("/{stationNumber}")
    public CommonApiResponse<StationDetailResponse> findStationDetail(@PathVariable("stationNumber") int stationNumber) {
        StationDetailResponse stationDetailResponse = stationService.findStationDetail(stationNumber);
        return CommonApiResponse.success(stationDetailResponse, "대여소 상세 조회에 성공했습니다.");
    }

}
