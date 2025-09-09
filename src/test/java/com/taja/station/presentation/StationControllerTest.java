package com.taja.station.presentation;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.taja.station.application.StationService;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = StationController.class)
class StationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StationService stationService;

    @DisplayName("올바른 값으로 주변 대여소 조회를 요청하면 성공(200 OK)한다.")
    @Test
    void findNearbyStations_success() throws Exception {
        // given
        given(stationService.findNearbyStations(anyDouble(), anyDouble(), anyDouble(), anyDouble()))
                .willReturn(Collections.emptyList());

        // when & then
        mockMvc.perform(get("/stations/nearby")
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
    void findNearbyStations_fail_when_latitude_is_too_low() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/nearby")
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
    void findNearbyStations_fail_when_latitude_is_too_high() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/nearby")
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
    void findNearbyStations_fail_when_longitude_is_too_low() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/nearby")
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
    void findNearbyStations_fail_when_longitude_is_too_high() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/nearby")
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
    void findNearbyStations_fail_when_latDelta_is_less_than_zero() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/nearby")
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
    void findNearbyStations_fail_when_lonDelta_is_less_than_zero() throws Exception {
        // when & then
        mockMvc.perform(get("/stations/nearby")
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
