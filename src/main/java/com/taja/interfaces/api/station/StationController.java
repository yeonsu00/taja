package com.taja.interfaces.api.station;

import com.taja.application.board.BoardFacade;
import com.taja.application.board.BoardInfo;
import com.taja.application.favorite.FavoriteStationFacade;
import com.taja.application.favorite.FavoriteStationService;
import com.taja.application.station.StationFacade;
import com.taja.global.exception.InvalidSortTypeException;
import com.taja.global.response.CommonApiResponse;
import com.taja.infrastructure.jwt.CustomUserDetails;
import com.taja.application.cache.StationCacheService;
import com.taja.application.station.StationService;
import com.taja.interfaces.api.station.request.CreatePostRequest;
import com.taja.interfaces.api.station.request.NearbyStationRequest;
import com.taja.interfaces.api.station.request.SearchStationRequest;
import com.taja.interfaces.api.station.response.IsFavoriteStationResponse;
import com.taja.interfaces.api.station.response.MapStationResponse;
import com.taja.interfaces.api.station.response.NearbyStationsResponse;
import com.taja.interfaces.api.station.response.StationStatusResponse;
import com.taja.interfaces.api.station.response.PostItemResponse;
import com.taja.interfaces.api.station.response.PostListResponse;
import com.taja.interfaces.api.station.response.StationSimpleResponse;
import com.taja.interfaces.api.station.response.detail.StationDetailResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final StationFacade stationFacade;
    private final StationCacheService stationCacheService;
    private final FavoriteStationService favoriteStationService;
    private final FavoriteStationFacade favoriteStationFacade;
    private final BoardFacade boardFacade;

    @Hidden
    @Operation(summary = "대여소 파일 업로드", description = "엑셀 파일을 업로드하여 대여소 정보를 등록 및 수정합니다.")
    @PostMapping("/upload")
    public CommonApiResponse<String> readStationFile(@RequestParam("file") MultipartFile file) {
        LocalDateTime requestedAt = LocalDateTime.now();
        int count = stationFacade.uploadStationsFromFile(file, requestedAt);
        return CommonApiResponse.success(count + "개 대여소가 등록 및 수정되었습니다.");
    }

    @Operation(summary = "지도 화면 영역 내 대여소 조회", description = "지도 중심 좌표와 화면에 보이는 영역의 위도, 경도 차이를 이용해 근처 대여소를 조회합니다.")
    @GetMapping("/map/nearby")
    public CommonApiResponse<NearbyStationsResponse> findStationsInBounds(
            @Valid @ModelAttribute NearbyStationRequest nearbyStationRequest) {
        NearbyStationsResponse response = stationFacade.findStationsInBounds(
                nearbyStationRequest.latitude(),
                nearbyStationRequest.longitude(),
                nearbyStationRequest.latDelta(),
                nearbyStationRequest.lngDelta()
        );

        return CommonApiResponse.success(response, "근처 대여소 조회에 성공했습니다.");
    }

    @Operation(summary = "대여소 검색", description = "키워드와 지도 중심 좌표를 이용해 대여소를 검색합니다.")
    @GetMapping("/map/search")
    public CommonApiResponse<List<StationSimpleResponse>> searchStation(
            @Valid @ModelAttribute SearchStationRequest searchStationRequest) {
        double centerLat = searchStationRequest.lat();
        double centerLng = searchStationRequest.lng();

        List<StationSimpleResponse> searchedStations = stationService.searchStationsByName(
                searchStationRequest.keyword(), centerLat, centerLng);

        return CommonApiResponse.success(searchedStations, "대여소 검색에 성공했습니다.");
    }

    @Operation(summary = "실시간 남은 자전거 수 조회", description = "대여소 번호로 실시간 남은 자전거 수를 조회합니다.")
    @GetMapping("/status/{stationNumber}")
    public CommonApiResponse<StationStatusResponse> getStationStatus(
            @PathVariable("stationNumber") Integer stationNumber) {
        StationStatusResponse response = StationStatusResponse.from(
                stationCacheService.getStationStatusByNumber(stationNumber));
        return CommonApiResponse.success(response, "실시간 남은 자전거 수 조회에 성공했습니다.");
    }

    @Operation(summary = "대여소 상세 조회", description = "대여소 ID를 이용해 대여소의 상세 정보를 조회합니다.")
    @GetMapping("/{stationId}")
    public CommonApiResponse<StationDetailResponse> findStationDetail(
            @PathVariable("stationId") Long stationId) {
        LocalDateTime requestedAt = LocalDateTime.now();
        StationDetailResponse stationDetailResponse = stationFacade.findStationDetail(stationId, requestedAt);
        return CommonApiResponse.success(stationDetailResponse, "대여소 상세 조회에 성공했습니다.");
    }

    @Operation(summary = "게시판 참여", description = "대여소 ID로 해당 대여소 게시판에 참여합니다.")
    @PostMapping("/{stationId}/posts/join")
    public CommonApiResponse<String> joinBoard(@PathVariable("stationId") Long stationId,
                                               @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        boardFacade.join(email, stationId);
        return CommonApiResponse.success("게시판 " + stationId + " 참여에 성공했습니다.");
    }

    @Operation(summary = "게시글 목록 조회", description = "대여소 ID로 해당 게시판의 게시글 목록을 조회합니다.")
    @GetMapping("/{stationId}/posts")
    public CommonApiResponse<PostListResponse> getPosts(@PathVariable("stationId") Long stationId,
                                                          @RequestParam(value = "sort", required = false) String sort,
                                                          @RequestParam(value = "cursor", required = false) String cursor,
                                                          @RequestParam(value = "size", defaultValue = "20") int size,
                                                          @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails != null ? customUserDetails.getUsername() : null;
        PostSort postSort = PostSort.fromValue(sort);

        BoardInfo.PostItems postItems;
        if (postSort == PostSort.LATEST) {
            postItems = boardFacade.findLatestPosts(email, stationId, cursor, size);
        } else if (postSort == PostSort.POPULAR) {
            postItems = boardFacade.findPopularPosts(email, stationId, cursor, size, LocalDate.now());
        } else {
            throw new InvalidSortTypeException("지원하지 않는 정렬 기준입니다: " + sort);
        }

        List<PostItemResponse> postItemResponses = PostItemResponse.from(postItems.items());
        PostListResponse response = new PostListResponse(postItemResponses, postItems.nextCursor());
        return CommonApiResponse.success(response, "게시글 목록 조회에 성공했습니다.");
    }

    @Operation(summary = "게시글 작성", description = "대여소 ID로 해당 게시판에 게시글을 작성합니다.")
    @PostMapping("/{stationId}/posts")
    public CommonApiResponse<String> createPost(@PathVariable("stationId") Long stationId,
                                                 @Valid @RequestBody CreatePostRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        boardFacade.createPost(email, stationId, request.content());
        return CommonApiResponse.success("게시글 작성에 성공했습니다.");
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
        List<MapStationResponse> favoriteStations = favoriteStationFacade.findFavoriteStationsByMemberEmail(email);
        return CommonApiResponse.success(favoriteStations, "즐겨찾기 대여소 조회에 성공했습니다.");
    }

}
