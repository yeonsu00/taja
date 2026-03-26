package com.taja.infrastructure.client.bike;

import static com.taja.config.Resilience4jConfig.STATION_API_RESILIENCE;

import com.taja.application.station.StationClient;
import com.taja.domain.station.Station;
import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import com.taja.infrastructure.client.bike.dto.station.StationApiResponseDto;
import com.taja.infrastructure.client.bike.dto.station.StationDto;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeoulDataStationInfoClient implements StationClient {

    private final SeoulDataStationInfoFeignClient seoulDataStationInfoFeignClient;

    private static final String SUCCESS_RESULT_CODE = "INFO-000";
    private static final Set<String> NON_RETRYABLE_ERROR_CODES = Set.of(
            "ERROR-300", "ERROR-301", "ERROR-310", "ERROR-331",
            "ERROR-332", "ERROR-333", "ERROR-334", "ERROR-335", "ERROR-336", "INFO-200"
    );

    @Override
    @Retry(name = STATION_API_RESILIENCE, fallbackMethod = "fetchStationInfosFallback")
    public List<Station> fetchStationInfos(int startIndex, int endIndex) {
        StationApiResponseDto response = seoulDataStationInfoFeignClient.getTbCycleStationInfo(startIndex, endIndex);

        if (StationApiResponseDto.hasErrorCode(response)) {
            String resultCode = response.result().code();
            String resultMessage = response.result().message();

            if (NON_RETRYABLE_ERROR_CODES.contains(resultCode)) {
                throw new NoRetryApiException(resultCode, resultMessage);
            }

            throw new ApiException(resultCode, resultMessage);
        }

        if (!StationApiResponseDto.hasStationInfo(response)) {
            throw new ApiException("JSON_PARSING_ERROR", "API 응답 DTO 파싱에 실패했습니다.");
        }

        String resultCode = response.stationInfo().result().code();
        String resultMessage = response.stationInfo().result().message();

        if (SUCCESS_RESULT_CODE.equals(resultCode)) {
            List<StationDto> loadedStations = response.stationInfo().stations();
            log.info("✅ 대여소 정보 API 요청 성공 ({}-{}) | 수집된 데이터 수: {}", startIndex, endIndex, loadedStations.size());
            return StationDto.toStations(loadedStations);
        }

        if (NON_RETRYABLE_ERROR_CODES.contains(resultCode)) {
            throw new NoRetryApiException(resultCode, resultMessage);
        }

        throw new ApiException(resultCode, resultMessage);
    }

    public List<Station> fetchStationInfosFallback(int startIndex, int endIndex, Throwable t) {
        log.error("대여소 정보 API 호출 실패 ({}-{}) - 오류: {}", startIndex, endIndex, t.getMessage());
        return List.of();
    }
}
