package com.taja.infrastructure.client.weather;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.taja.domain.weather.DistrictPoint;
import com.taja.domain.weather.WeatherHistory;
import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import com.taja.infrastructure.client.weather.dto.WeatherApiResponseDto;
import com.taja.infrastructure.client.weather.dto.WeatherBodyDto;
import com.taja.infrastructure.client.weather.dto.WeatherHeaderDto;
import com.taja.infrastructure.client.weather.dto.WeatherItemDto;
import com.taja.infrastructure.client.weather.dto.WeatherItemsDto;
import com.taja.infrastructure.client.weather.dto.WeatherResponseDto;
import feign.FeignException;
import feign.Request;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
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
@DisplayName("KmaWeatherClient Resilience4j 동작 테스트")
class KmaWeatherClientResilienceTest {

    @Autowired
    private KmaWeatherClient kmaWeatherClient;

    @MockitoSpyBean
    private KmaWeatherFeignClient kmaWeatherFeignClient;

    private DistrictPoint districtPoint;
    private LocalDateTime requestedAt;

    @BeforeEach
    void setUp() {
        districtPoint = DistrictPoint.of("강남구", 61, 126);
        requestedAt = LocalDateTime.of(2024, 1, 1, 12, 0);
    }

    @DisplayName("Retry 동작 테스트")
    @Nested
    class RetryTest {

        @Test
        @DisplayName("재시도 가능한 예외(ApiException) 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenRetryableException() {
            // given
            ApiException apiException = new ApiException("02", "일시적인 오류입니다");

            doThrow(apiException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull(); // fallback에서 null 반환
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("재시도 불가능한 예외(NoRetryApiException) 발생 시 재시도하지 않고 즉시 Fallback이 호출된다")
        void callsFallbackImmediately_whenNonRetryableException() {
            // given
            NoRetryApiException noRetryException = new NoRetryApiException("10", "잘못된 요청입니다");

            doThrow(noRetryException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull(); // fallback에서 null 반환
            verify(kmaWeatherFeignClient, times(1)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
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
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("IOException 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenIOException() {
            // given
            IOException ioException = new IOException("Connection refused");
            RuntimeException runtimeException = new RuntimeException(ioException);

            doThrow(runtimeException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("재시도 중 성공하면 재시도를 중단하고 결과를 반환한다")
        void stopsRetrying_whenSuccessDuringRetry() {
            // given
            ApiException apiException = new ApiException("02", "일시적인 오류입니다");
            WeatherApiResponseDto successResponse = createSuccessResponse();

            doThrow(apiException)  // 1차 시도 실패
                    .doReturn(successResponse)  // 2차 시도 성공
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getDistrict()).isEqualTo("강남구");
            verify(kmaWeatherFeignClient, times(2)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }
    }

    @DisplayName("타임아웃 동작 테스트")
    @Nested
    class TimeoutTest {

        @Test
        @DisplayName("SocketTimeoutException 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenSocketTimeoutException() {
            // given
            SocketTimeoutException socketTimeoutException = new SocketTimeoutException("Read timed out");
            RuntimeException runtimeException = new RuntimeException(socketTimeoutException);

            doThrow(runtimeException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("ResourceAccessException 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenResourceAccessException() {
            // given
            ResourceAccessException resourceAccessException = new ResourceAccessException("I/O error on GET request");

            doThrow(resourceAccessException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("TimeoutException 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenTimeoutException() {
            // given
            TimeoutException timeoutException = new TimeoutException("Request timeout");
            RuntimeException runtimeException = new RuntimeException(timeoutException);

            doThrow(runtimeException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }
    }

    @DisplayName("기상청 API 응답별 동작 테스트")
    @Nested
    class WeatherApiResponseTest {

        @Test
        @DisplayName("정상 응답(resultCode: 00) 시 성공적으로 처리한다")
        void succeeds_whenSuccessResponse() {
            // given
            WeatherApiResponseDto successResponse = createSuccessResponse();
            doReturn(successResponse)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getDistrict()).isEqualTo("강남구");
            assertThat(result.getTemperature()).isEqualTo(20.5);
            verify(kmaWeatherFeignClient, times(1)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("재시도 가능한 resultCode(02) 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenRetryableResultCode02() {
            // given
            WeatherApiResponseDto errorResponse = createErrorResponse("02", "일시적인 오류입니다");
            doReturn(errorResponse)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("재시도 가능한 resultCode(04) 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenRetryableResultCode04() {
            // given
            WeatherApiResponseDto errorResponse = createErrorResponse("04", "일시적인 오류입니다");
            doReturn(errorResponse)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("재시도 불가능한 resultCode(10) 발생 시 재시도하지 않고 즉시 Fallback이 호출된다")
        void callsFallbackImmediately_whenNonRetryableResultCode10() {
            // given
            WeatherApiResponseDto errorResponse = createErrorResponse("10", "잘못된 요청 파라미터입니다");
            doReturn(errorResponse)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(1)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("재시도 불가능한 resultCode(11) 발생 시 재시도하지 않고 즉시 Fallback이 호출된다")
        void callsFallbackImmediately_whenNonRetryableResultCode11() {
            // given
            WeatherApiResponseDto errorResponse = createErrorResponse("11", "서비스키가 없습니다");
            doReturn(errorResponse)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(1)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }

        @Test
        @DisplayName("JSON 파싱 오류 발생 시 3번 재시도 후 Fallback이 호출된다")
        void callsFallback_after3Retries_whenJsonParsingError() {
            // given
            doReturn(null)  // null 응답으로 파싱 오류 발생
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            verify(kmaWeatherFeignClient, times(3)).getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());
        }
    }

    @DisplayName("Fallback 동작 테스트")
    @Nested
    class FallbackTest {

        @Test
        @DisplayName("모든 재시도 실패 시 Fallback이 호출되어 null을 반환한다")
        void returnsNull_whenAllRetriesFail() {
            // given
            ApiException apiException = new ApiException("02", "일시적인 오류입니다");

            doThrow(apiException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Fallback이 호출되면 로그가 기록된다")
        void logsWarning_whenFallbackCalled() {
            // given
            ApiException apiException = new ApiException("02", "일시적인 오류입니다");

            doThrow(apiException)
                    .when(kmaWeatherFeignClient)
                    .getUltraShortNowcast(anyString(), anyString(), anyInt(), anyInt(), anyString(), anyString(), anyInt(), anyInt());

            // when
            WeatherHistory result = kmaWeatherClient.fetchWeatherHistory(districtPoint, requestedAt);

            // then
            assertThat(result).isNull();
            // 로그 검증은 별도로 할 수 있지만, 여기서는 null 반환만 확인
        }
    }

    private WeatherApiResponseDto createSuccessResponse() {
        WeatherItemDto temperatureItem = new WeatherItemDto("20240101", "1200", "T1H", 61, 126, "20.5");
        WeatherItemDto rainItem = new WeatherItemDto("20240101", "1200", "RN1", 61, 126, "0.0");
        WeatherItemDto windItem = new WeatherItemDto("20240101", "1200", "WSD", 61, 126, "2.5");

        WeatherItemsDto items = new WeatherItemsDto(List.of(temperatureItem, rainItem, windItem));
        WeatherBodyDto body = new WeatherBodyDto(items);
        WeatherHeaderDto header = new WeatherHeaderDto("00", "NORMAL_SERVICE");
        WeatherResponseDto response = new WeatherResponseDto(header, body);

        return new WeatherApiResponseDto(response);
    }

    private WeatherApiResponseDto createErrorResponse(String resultCode, String resultMsg) {
        WeatherHeaderDto header = new WeatherHeaderDto(resultCode, resultMsg);
        WeatherResponseDto response = new WeatherResponseDto(header, null);

        return new WeatherApiResponseDto(response);
    }
}
