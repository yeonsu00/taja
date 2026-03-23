package com.taja.infrastructure.client.bike;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import com.taja.infrastructure.client.bike.dto.status.BikeApiResponseDto;
import com.taja.infrastructure.client.bike.dto.status.BikeStatusDto;
import com.taja.infrastructure.client.bike.dto.status.ResultDto;
import com.taja.infrastructure.client.bike.dto.status.StationStatusDto;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("SeoulDataStationStatusClient 테스트")
class SeoulDataStationStatusClientTest {

    private MockWebServer mockWebServer;
    private SeoulDataStationStatusClient client;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        
        objectMapper = new ObjectMapper();
        String apiKey = "test-api-key";
        String apiStatusPath = "/test/path/";
        
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        
        client = new SeoulDataStationStatusClient(webClient, apiKey, apiStatusPath);
    }

    @AfterEach
    void tearDown() throws Exception {
        if (mockWebServer != null) {
            try {
                // 모든 대기 중인 요청 취소
                try {
                    mockWebServer.takeRequest(100, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    // 무시
                }
                
                // 큐 비우기
                mockWebServer.shutdown();
            } catch (Exception e) {
                // 강제 종료
                try {
                    mockWebServer.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    @DisplayName("성공 케이스 테스트")
    @Nested
    class SuccessTest {

        @Test
        @DisplayName("정상 응답 시 StationStatusDto 리스트를 반환한다")
        void returnsStationStatusList_whenSuccess() throws Exception {
            // given
            StationStatusDto stationStatus = new StationStatusDto(
                    "10", "1. 테스트 대여소", "5", "50", "37.5665", "126.9780", "ST-001"
            );
            BikeStatusDto bikeStatus = new BikeStatusDto(
                    1, new ResultDto("INFO-000", "정상 처리되었습니다."), List.of(stationStatus)
            );
            BikeApiResponseDto response = new BikeApiResponseDto(bikeStatus, null);
            
            String responseBody = objectMapper.writeValueAsString(response);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseBody));

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(list -> list.size() == 1 && list.get(0).stationId().equals("ST-001"))
                    .verifyComplete();
        }
    }

    @DisplayName("재시도 테스트")
    @Nested
    class RetryTest {

        @Test
        @DisplayName("재시도 가능한 예외(ApiException) 발생 시 3번 재시도 후 빈 리스트를 반환한다")
        void returnsEmptyList_after3Retries_whenRetryableException() throws Exception {
            // given
            ResultDto errorResult = new ResultDto("ERROR-500", "일시적인 오류입니다");
            BikeStatusDto bikeStatus = new BikeStatusDto(0, errorResult, null);
            BikeApiResponseDto errorResponse = new BikeApiResponseDto(bikeStatus, null);
            
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            
            // 3번 실패 응답
            for (int i = 0; i < 3; i++) {
                mockWebServer.enqueue(new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(responseBody));
            }

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(List::isEmpty)
                    .verifyComplete();
            
            // 재시도 확인 (1번 시도 + 3번 재시도 = 총 4번 요청)
            await().atMost(5, TimeUnit.SECONDS)
                    .until(() -> mockWebServer.getRequestCount() >= 4);
            
            assertThat(mockWebServer.getRequestCount()).isGreaterThanOrEqualTo(4);
        }

        @Test
        @DisplayName("재시도 불가능한 예외(NoRetryApiException) 발생 시 재시도하지 않고 즉시 빈 리스트를 반환한다")
        void returnsEmptyListImmediately_whenNonRetryableException() throws Exception {
            // given
            ResultDto errorResult = new ResultDto("ERROR-334", "요청종료위치 보다 요청시작위치가 더 큽니다");
            BikeStatusDto bikeStatus = new BikeStatusDto(0, errorResult, null);
            BikeApiResponseDto errorResponse = new BikeApiResponseDto(bikeStatus, null);
            
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseBody));

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(List::isEmpty)
                    .verifyComplete();
            
            // 재시도 없이 1번만 요청
            await().atMost(2, TimeUnit.SECONDS)
                    .until(() -> mockWebServer.getRequestCount() == 1);
            
            assertThat(mockWebServer.getRequestCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("재시도 중 성공하면 재시도를 중단하고 결과를 반환한다")
        void stopsRetrying_whenSuccessDuringRetry() throws Exception {
            // given
            // 1차 시도: 실패
            ResultDto errorResult = new ResultDto("ERROR-500", "일시적인 오류입니다");
            BikeStatusDto errorBikeStatus = new BikeStatusDto(0, errorResult, null);
            BikeApiResponseDto errorResponse = new BikeApiResponseDto(errorBikeStatus, null);
            String errorResponseBody = objectMapper.writeValueAsString(errorResponse);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(errorResponseBody));
            
            // 2차 시도: 성공
            StationStatusDto stationStatus = new StationStatusDto(
                    "10", "1. 테스트 대여소", "5", "50", "37.5665", "126.9780", "ST-001"
            );
            BikeStatusDto successBikeStatus = new BikeStatusDto(
                    1, new ResultDto("INFO-000", "정상 처리되었습니다."), List.of(stationStatus)
            );
            BikeApiResponseDto successResponse = new BikeApiResponseDto(successBikeStatus, null);
            String successResponseBody = objectMapper.writeValueAsString(successResponse);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(successResponseBody));

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(list -> list.size() == 1)
                    .verifyComplete();
            
            // 2번 요청 (1번 실패 + 1번 성공)
            await().atMost(3, TimeUnit.SECONDS)
                    .until(() -> mockWebServer.getRequestCount() == 2);
            
            assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
        }
    }

    @DisplayName("타임아웃 테스트")
    @Nested
    class TimeoutTest {

        @Test
        @DisplayName("연결 실패로 인한 타임아웃 발생 시 재시도 후 빈 리스트를 반환한다")
        void returnsEmptyList_afterRetries_whenConnectionTimeout() throws Exception {
            // given -
            MockWebServer timeoutServer = new MockWebServer();
            try {
                timeoutServer.start();
                String serverUrl = timeoutServer.url("/").toString();
                timeoutServer.shutdown();
                
                WebClient timeoutWebClient = WebClient.builder()
                        .baseUrl(serverUrl)
                        .build();
                
                SeoulDataStationStatusClient timeoutClient = new SeoulDataStationStatusClient(
                        timeoutWebClient, "test-api-key", "/test/path/");

                // when
                Mono<List<StationStatusDto>> result = timeoutClient.fetchStationStatuses(1, 10);

                // then
                StepVerifier.create(result)
                        .expectNextMatches(List::isEmpty)
                        .verifyComplete();
            } finally {
                timeoutServer.shutdown();
            }
        }

        @Test
        @DisplayName("네트워크 에러로 인한 타임아웃 발생 시 재시도 후 빈 리스트를 반환한다")
        void returnsEmptyList_afterRetries_whenNetworkError() throws Exception {
            // given - 잘못된 URL로 연결 실패 시뮬레이션
            WebClient timeoutWebClient = WebClient.builder()
                    .baseUrl("http://localhost:99999") // 존재하지 않는 포트
                    .build();
            
            SeoulDataStationStatusClient timeoutClient = new SeoulDataStationStatusClient(
                    timeoutWebClient, "test-api-key", "/test/path/");

            // when
            Mono<List<StationStatusDto>> result = timeoutClient.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(List::isEmpty)
                    .verifyComplete();
        }
    }

    @DisplayName("에러 응답 테스트")
    @Nested
    class ErrorResponseTest {

        @Test
        @DisplayName("최상위 RESULT만 있는 에러 응답 시 빈 리스트를 반환한다")
        void returnsEmptyList_whenTopLevelErrorResponse() throws Exception {
            // given
            ResultDto errorResult = new ResultDto("ERROR-334", "요청종료위치 보다 요청시작위치가 더 큽니다");
            BikeApiResponseDto errorResponse = new BikeApiResponseDto(null, errorResult);
            
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseBody));

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(List::isEmpty)
                    .verifyComplete();
        }

        @Test
        @DisplayName("rentBikeStatus가 null인 응답 시 빈 리스트를 반환한다")
        void returnsEmptyList_whenRentBikeStatusIsNull() throws Exception {
            // given
            BikeApiResponseDto response = new BikeApiResponseDto(null, null);
            String responseBody = objectMapper.writeValueAsString(response);
            
            mockWebServer.enqueue(new MockResponse()
                    .setResponseCode(200)
                    .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .setBody(responseBody));

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(List::isEmpty)
                    .verifyComplete();
        }
    }

    @DisplayName("Fallback 테스트")
    @Nested
    class FallbackTest {

        @Test
        @DisplayName("모든 재시도 실패 시 Fallback이 빈 리스트를 반환한다")
        void returnsEmptyList_whenAllRetriesFail() throws Exception {
            // given
            ResultDto errorResult = new ResultDto("ERROR-500", "일시적인 오류입니다");
            BikeStatusDto bikeStatus = new BikeStatusDto(0, errorResult, null);
            BikeApiResponseDto errorResponse = new BikeApiResponseDto(bikeStatus, null);
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            
            // 4번 실패 (1번 시도 + 3번 재시도)
            for (int i = 0; i < 4; i++) {
                mockWebServer.enqueue(new MockResponse()
                        .setResponseCode(200)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody(responseBody));
            }

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(list -> {
                        assertThat(list).isNotNull();
                        assertThat(list).isEmpty();
                        return true;
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("WebClientException 발생 시 재시도 후 빈 리스트를 반환한다")
        void returnsEmptyList_afterRetries_whenWebClientException() {
            // given - 500 에러로 WebClientException 발생
            for (int i = 0; i < 4; i++) {
                mockWebServer.enqueue(new MockResponse()
                        .setResponseCode(500)
                        .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .setBody("Internal Server Error"));
            }

            // when
            Mono<List<StationStatusDto>> result = client.fetchStationStatuses(1, 10);

            // then
            StepVerifier.create(result)
                    .expectNextMatches(List::isEmpty)
                    .verifyComplete();
        }
    }
}
