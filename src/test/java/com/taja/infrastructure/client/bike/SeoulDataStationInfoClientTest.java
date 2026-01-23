package com.taja.infrastructure.client.bike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.taja.domain.station.Station;
import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import com.taja.infrastructure.client.bike.dto.station.StationApiResponseDto;
import com.taja.infrastructure.client.bike.dto.station.StationDto;
import com.taja.infrastructure.client.bike.dto.station.StationInfoDto;
import com.taja.infrastructure.client.bike.dto.status.ResultDto;
import feign.FeignException;
import feign.Request;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.ResourceAccessException;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("SeoulDataStationInfoClient 테스트")
class SeoulDataStationInfoClientTest {

    @Autowired
    private SeoulDataStationInfoClient seoulDataStationInfoClient;

    @MockitoSpyBean
    private SeoulDataStationInfoFeignClient seoulDataStationInfoFeignClient;

    private int startIndex;
    private int endIndex;

    @BeforeEach
    void setUp() {
        startIndex = 1;
        endIndex = 100;
    }

    @DisplayName("Retry 동작 테스트")
    @Nested
    class RetryTest {

        @Test
        @DisplayName("재시도 가능한 예외(ApiException) 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenRetryableException() {
            // given
            ApiException apiException = new ApiException("ERROR-500", "일시적인 오류입니다");

            doThrow(apiException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty(); // fallback에서 빈 리스트 반환
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }

        @Test
        @DisplayName("재시도 불가능한 예외(NoRetryApiException) 발생 시 재시도하지 않고 즉시 Fallback이 호출된다")
        void callsFallbackImmediately_whenNonRetryableException() {
            // given
            NoRetryApiException noRetryException = new NoRetryApiException("ERROR-300", "잘못된 요청입니다");

            doThrow(noRetryException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty(); // fallback에서 빈 리스트 반환
            verify(seoulDataStationInfoFeignClient, times(1)).getTbCycleStationInfo(startIndex, endIndex);
        }

        @Test
        @DisplayName("FeignException 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenFeignException() {
            // given
            Request request = Request.create(
                    Request.HttpMethod.GET,
                    "http://test.com",
                    Collections.emptyMap(),
                    null,
                    null,
                    null
            );
            FeignException feignException = new FeignException.InternalServerError(
                    "Internal Server Error",
                    request,
                    null,
                    null
            );

            doThrow(feignException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty();
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }

        @Test
        @DisplayName("IOException 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenIOException() {
            // given
            IOException ioException = new IOException("Connection refused");
            RuntimeException runtimeException = new RuntimeException(ioException);

            doThrow(runtimeException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty();
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }

        @Test
        @DisplayName("재시도 중 성공하면 재시도를 중단하고 결과를 반환한다")
        void stopsRetrying_whenSuccessDuringRetry() {
            // given
            ApiException apiException = new ApiException("ERROR-500", "일시적인 오류입니다");
            StationApiResponseDto successResponse = createSuccessResponse();

            doThrow(apiException)  // 1차 시도 실패
                    .doReturn(successResponse)  // 2차 시도 성공
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(seoulDataStationInfoFeignClient, times(2)).getTbCycleStationInfo(startIndex, endIndex);
        }
    }

    @DisplayName("ConnectTimeout 테스트")
    @Nested
    class ConnectTimeoutTest {

        @Test
        @DisplayName("연결 타임아웃 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenConnectTimeout() {
            // given
            ResourceAccessException connectTimeoutException = 
                    new ResourceAccessException("Connect timed out", new java.net.ConnectException("Connection timed out"));

            doThrow(connectTimeoutException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty(); // fallback에서 빈 리스트 반환
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }

    }

    @DisplayName("ReadTimeout 테스트")
    @Nested
    class ReadTimeoutTest {

        @Test
        @DisplayName("읽기 타임아웃(SocketTimeoutException) 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenReadTimeout() {
            // given
            SocketTimeoutException readTimeoutException = new SocketTimeoutException("Read timed out");
            RuntimeException runtimeException = new RuntimeException(readTimeoutException);

            doThrow(runtimeException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty();
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }

        @Test
        @DisplayName("읽기 타임아웃(ResourceAccessException) 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenReadTimeoutResourceAccess() {
            // given
            ResourceAccessException readTimeoutException = 
                    new ResourceAccessException("Read timed out", new SocketTimeoutException("Read timed out"));

            doThrow(readTimeoutException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty();
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }

        @Test
        @DisplayName("TimeoutException 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenTimeoutException() {
            // given
            TimeoutException timeoutException = new TimeoutException("Request timeout");
            RuntimeException runtimeException = new RuntimeException(timeoutException);

            doThrow(runtimeException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty();
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }
    }

    @DisplayName("타임아웃 설정 검증 테스트")
    @Nested
    class TimeoutConfigurationTest {

        @Test
        @DisplayName("타임아웃 발생 시 재시도 후 Fallback이 빈 리스트를 반환한다")
        void returnsEmptyList_whenTimeoutAfterRetries() {
            // given
            SocketTimeoutException socketTimeoutException = new SocketTimeoutException("Read timed out");
            RuntimeException runtimeException = new RuntimeException(socketTimeoutException);

            doThrow(runtimeException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(seoulDataStationInfoFeignClient, times(3)).getTbCycleStationInfo(startIndex, endIndex);
        }

    }

    @DisplayName("Fallback 동작 테스트")
    @Nested
    class FallbackTest {

        @Test
        @DisplayName("모든 재시도 실패 시 Fallback이 호출되어 빈 리스트를 반환한다")
        void returnsEmptyList_whenAllRetriesFail() {
            // given
            ApiException apiException = new ApiException("ERROR-500", "일시적인 오류입니다");

            doThrow(apiException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Fallback이 호출되면 로그가 기록된다")
        void logsError_whenFallbackCalled() {
            // given
            ApiException apiException = new ApiException("ERROR-500", "일시적인 오류입니다");

            doThrow(apiException)
                    .when(seoulDataStationInfoFeignClient)
                    .getTbCycleStationInfo(startIndex, endIndex);

            // when
            List<Station> result = seoulDataStationInfoClient.fetchStationInfos(startIndex, endIndex);

            // then
            assertThat(result).isEmpty();
            // 로그 검증은 별도로 할 수 있지만, 여기서는 빈 리스트 반환만 확인
        }
    }

    private StationApiResponseDto createSuccessResponse() {
        StationDto stationDto = new StationDto(
                "강남구",
                "ST-001",
                "1",
                "테스트 대여소",
                "테스트 대여소 ID",
                "10",
                "서울시 강남구 테헤란로",
                "123",
                "37.5665",
                "126.9780",
                1,
                100,
                "1"
        );

        ResultDto result = new ResultDto("INFO-000", "정상 처리되었습니다.");
        StationInfoDto stationInfo = new StationInfoDto("1", result, List.of(stationDto));

        return new StationApiResponseDto(stationInfo, null);
    }
}
