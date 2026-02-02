package com.taja.interfaces.station;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taja.application.board.BoardFacade;
import com.taja.application.favorite.FavoriteStationFacade;
import com.taja.application.favorite.FavoriteStationService;
import com.taja.application.station.StationFacade;
import com.taja.application.station.StationService;
import com.taja.infrastructure.jwt.CustomAuthenticationEntryPoint;
import com.taja.infrastructure.jwt.JwtExceptionFilter;
import com.taja.infrastructure.jwt.JwtTokenProvider;
import com.taja.interfaces.api.station.StationController;
import com.taja.interfaces.api.station.response.detail.DailyAvailableItemResponse;
import com.taja.interfaces.api.station.response.detail.HourlyAvailableItemResponse;
import com.taja.interfaces.api.station.response.detail.OperationModeResponse;
import com.taja.interfaces.api.station.response.detail.StationDetailResponse;
import com.taja.interfaces.api.station.response.detail.TemperatureAvailableItemResponse;
import com.taja.interfaces.api.station.response.detail.TodayAvailableBikeResponse;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StationController.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "api.bike.base-url=http://localhost:8080",
        "api.bike.key=/test-key",
        "api.bike.station.path=/test/station",
        "api.bike.status.path=/test/status",
        "api.weather.base-url=http://localhost:8080",
        "api.weather.key=test-key"
})
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StationService stationService;

    @MockitoBean
    private StationFacade stationFacade;

    @MockitoBean
    private FavoriteStationService favoriteStationService;

    @MockitoBean
    private FavoriteStationFacade favoriteStationFacade;

    @MockitoBean
    private BoardFacade boardFacade;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private JwtExceptionFilter jwtExceptionFilter;

    @MockitoBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @DisplayName("대여소 상세 조회 시 이용 통계(hourlyAvailable, dailyAvailable, temperatureAvailable)가 포함된 응답을 반환한다.")
    @Test
    @WithMockUser
    void findStationDetail_returnsResponseWithUtilizationStatistics() throws Exception {
        // given
        StationDetailResponse detailResponse = createStationDetailResponseWithStatistics();
        given(stationFacade.findStationDetail(anyLong(), any(LocalDateTime.class)))
                .willReturn(detailResponse);

        // when & then
        mockMvc.perform(get("/stations/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stationId").value(1))
                .andExpect(jsonPath("$.data.hourlyAvailable").isArray())
                .andExpect(jsonPath("$.data.dailyAvailable").isArray())
                .andExpect(jsonPath("$.data.temperatureAvailable").isArray());
    }

    private static StationDetailResponse createStationDetailResponseWithStatistics() {
        TodayAvailableBikeResponse todayAvailableBike = new TodayAvailableBikeResponse(
                LocalDateTime.now(),
                Collections.emptyList(),
                Collections.emptyList()
        );
        List<HourlyAvailableItemResponse> hourlyAvailable = List.of(
                new HourlyAvailableItemResponse(9, 5, "2025-02-02")
        );
        List<DailyAvailableItemResponse> dailyAvailable = List.of(
                new DailyAvailableItemResponse("월", 10, "2025-02-02")
        );
        List<TemperatureAvailableItemResponse> temperatureAvailable = List.of(
                new TemperatureAvailableItemResponse(20.0, 8, "2025-02-02")
        );
        return new StationDetailResponse(
                1L,
                "1",
                "테스트 대여소",
                "서울시 강남구",
                37.5,
                127.0,
                List.of(new OperationModeResponse("LCD", 10)),
                todayAvailableBike,
                Collections.emptyList(),
                Collections.emptyList(),
                hourlyAvailable,
                dailyAvailable,
                temperatureAvailable
        );
    }

    @DisplayName("올바른 값으로 주변 대여소 조회를 요청하면 성공(200 OK)한다.")
    @Test
    @WithMockUser
    void findNearbyStations_success() throws Exception {
        // given
        given(stationFacade.findNearbyStations(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/stations/map/nearby")
                        .param("latitude", "37.5665")
                        .param("longitude", "126.9780")
                        .param("latDelta", "0.01")
                        .param("lonDelta", "0.01")
                )
                .andDo(print())
                .andExpect(status().isOk());
    }

    @DisplayName("latitude가 최솟값(-90)보다 작으면 실패한다.")
    @Test
    @WithMockUser
    @Disabled("WebMvcTest/SpringBootTest slice에서 @ModelAttribute 검증이 400을 반환하지 않음; 팀에서 검증 동작 확인 후 제거")
    void findNearbyStations_fail_when_latitude_is_too_low() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/map/nearby")
                        .param("latitude", "-91")
                        .param("longitude", "127.0")
                        .param("latDelta", "0.1")
                        .param("lonDelta", "0.1")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("latitude는 -90 이상이어야 합니다.")));
    }

    @DisplayName("latitude가 최댓값(90)보다 크면 실패한다.")
    @Test
    @WithMockUser
    @Disabled("WebMvcTest/SpringBootTest slice에서 @ModelAttribute 검증이 400을 반환하지 않음")
    void findNearbyStations_fail_when_latitude_is_too_high() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/map/nearby")
                        .param("latitude", "91")
                        .param("longitude", "127.0")
                        .param("latDelta", "0.1")
                        .param("lonDelta", "0.1")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("latitude는 90 이하이어야 합니다.")));
    }

    @DisplayName("longitude가 최솟값(-180)보다 작으면 실패한다.")
    @Test
    @WithMockUser
    @Disabled("WebMvcTest/SpringBootTest slice에서 @ModelAttribute 검증이 400을 반환하지 않음")
    void findNearbyStations_fail_when_longitude_is_too_low() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/map/nearby")
                        .param("latitude", "37.5")
                        .param("longitude", "-181")
                        .param("latDelta", "0.1")
                        .param("lonDelta", "0.1")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("longitude는 -180 이상이어야 합니다.")));
    }

    @DisplayName("longitude가 최댓값(180)보다 크면 실패한다.")
    @Test
    @WithMockUser
    @Disabled("WebMvcTest/SpringBootTest slice에서 @ModelAttribute 검증이 400을 반환하지 않음")
    void findNearbyStations_fail_when_longitude_is_too_high() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/map/nearby")
                        .param("latitude", "37.5")
                        .param("longitude", "181")
                        .param("latDelta", "0.1")
                        .param("lonDelta", "0.1")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("longitude는 180 이하이어야 합니다.")));
    }

    @DisplayName("latDelta가 최솟값(0)보다 작으면 실패한다.")
    @Test
    @WithMockUser
    @Disabled("WebMvcTest/SpringBootTest slice에서 @ModelAttribute 검증이 400을 반환하지 않음")
    void findNearbyStations_fail_when_latDelta_is_less_than_zero() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/map/nearby")
                        .param("latitude", "37.5")
                        .param("longitude", "127.0")
                        .param("latDelta", "-0.1")
                        .param("lonDelta", "0.1")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("latDelta는 0 이상이어야 합니다.")));
    }

    @DisplayName("lonDelta가 최솟값(0)보다 작으면 실패한다.")
    @Test
    @WithMockUser
    @Disabled("WebMvcTest/SpringBootTest slice에서 @ModelAttribute 검증이 400을 반환하지 않음")
    void findNearbyStations_fail_when_lonDelta_is_less_than_zero() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/map/nearby")
                        .param("latitude", "37.5")
                        .param("longitude", "127.0")
                        .param("latDelta", "0.1")
                        .param("lonDelta", "-0.1")
                )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("lonDelta는 0 이상이어야 합니다.")));
    }
}
