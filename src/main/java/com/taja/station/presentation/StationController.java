package com.taja.station.presentation;

import com.taja.favorite.application.FavoriteStationService;
import com.taja.global.response.CommonApiResponse;
import com.taja.jwt.CustomUserDetails;
import com.taja.station.application.StationService;
import com.taja.station.presentation.request.NearbyStationRequest;
import com.taja.station.presentation.request.SearchStationRequest;
import com.taja.station.presentation.response.IsFavoriteStationResponse;
import com.taja.station.presentation.response.MapStationResponse;
import com.taja.station.presentation.response.StationSimpleResponse;
import com.taja.station.presentation.response.detail.StationDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/stations")
@Tag(name = "Station", description = "Station API")
public class StationController {

    private final StationService stationService;
    private final FavoriteStationService favoriteStationService;

    @Operation(summary = "대여소 파일 업로드", description = "엑셀 파일을 업로드하여 대여소 정보를 등록 및 수정합니다.")
    @PostMapping("/upload")
    public CommonApiResponse<String> readStationFile(@RequestParam("file") MultipartFile file) {
        LocalDateTime requestedAt = LocalDateTime.now();
        int count = stationService.uploadStationsFromFile(file, requestedAt);
        return CommonApiResponse.success(count + "개 대여소가 등록 및 수정되었습니다.");
    }

    @Operation(summary = "근처 대여소 조회", description = "지도 중심 좌표와 화면에 보이는 영역의 위도, 경도 차이를 이용해 근처 대여소를 조회합니다.")
    @GetMapping("/map/nearby")
    public CommonApiResponse<List<MapStationResponse>> findNearbyStations(
            @Valid @ModelAttribute NearbyStationRequest nearbyStationRequest) {
        List<MapStationResponse> nearbyStations = stationService.findNearbyStations(
                nearbyStationRequest.latitude(),
                nearbyStationRequest.longitude(),
                nearbyStationRequest.latDelta(),
                nearbyStationRequest.lonDelta()
        );

        return CommonApiResponse.success(nearbyStations, "근처 대여소 조회에 성공했습니다.");
    }

    @Operation(summary = "대여소 검색", description = "키워드와 지도 중심 좌표를 이용해 대여소를 검색합니다.")
    @GetMapping("/map/search")
    public CommonApiResponse<List<StationSimpleResponse>> searchStation(
            @Valid @ModelAttribute SearchStationRequest searchStationRequest) {
        double centerLat = searchStationRequest.lat();
        double centerLon = searchStationRequest.lon();

        List<StationSimpleResponse> searchedStations = stationService.searchStationsByName(
                searchStationRequest.keyword(), centerLat, centerLon);

        return CommonApiResponse.success(searchedStations, "대여소 검색에 성공했습니다.");
    }

    @Operation(summary = "대여소 상세 조회", description = "대여소 ID를 이용해 대여소의 상세 정보를 조회합니다.")
    @GetMapping("/{stationId}")
    public CommonApiResponse<StationDetailResponse> findStationDetail(
            @PathVariable("stationId") Long stationId) {
        StationDetailResponse stationDetailResponse = stationService.findStationDetail(stationId);
        return CommonApiResponse.success(stationDetailResponse, "대여소 상세 조회에 성공했습니다.");
    }

    @Operation(summary = "즐겨찾기 등록", description = "대여소 ID를 이용해 해당 대여소를 즐겨찾기에 등록합니다.")
    @PostMapping("/{stationId}/favorite")
    public CommonApiResponse<String> addFavoriteStation(@PathVariable("stationId") Long stationId,
                                                        @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        favoriteStationService.addFavoriteStationToMember(email, stationId);

        return CommonApiResponse.success("즐겨찾기 등록에 성공했습니다.");
    }

    @Operation(summary = "즐겨찾기 삭제", description = "대여소 ID를 이용해 해당 대여소를 즐겨찾기에서 삭제합니다.")
    @DeleteMapping("/{stationId}/favorite")
    public CommonApiResponse<String> deleteFavoriteStation(@PathVariable("stationId") Long stationId,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        favoriteStationService.deleteMemberFavoriteStation(email, stationId);

        return CommonApiResponse.success("즐겨찾기 삭제에 성공했습니다.");
    }

    @Operation(summary = "즐겨찾기 여부 조회", description = "대여소 ID를 이용해 해당 대여소가 즐겨찾기에 등록되어 있는지 여부를 조회합니다.")
    @GetMapping("/{stationId}/favorite")
    public CommonApiResponse<IsFavoriteStationResponse> isFavoriteStation(@PathVariable("stationId") Long stationId,
                                                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();

        boolean isFavorite = favoriteStationService.isFavoriteStation(email, stationId);
        IsFavoriteStationResponse isFavoriteStationResponse = new IsFavoriteStationResponse(isFavorite);

        return CommonApiResponse.success(isFavoriteStationResponse, "즐겨찾기 여부 조회에 성공했습니다.");
    }

    @Operation(summary = "즐겨찾기 대여소 조회", description = "회원이 즐겨찾기에 등록한 대여소 목록을 조회합니다.")
    @GetMapping("/map/favorite")
    public CommonApiResponse<List<MapStationResponse>> findFavoriteStations(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        List<MapStationResponse> favoriteStations = favoriteStationService.findFavoriteStationsByMemberEmail(email);
        return CommonApiResponse.success(favoriteStations, "즐겨찾기 대여소 조회에 성공했습니다.");
    }

}
