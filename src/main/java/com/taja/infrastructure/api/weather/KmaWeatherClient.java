package com.taja.infrastructure.api.weather;

import static com.taja.config.Resilience4jConfig.WEATHER_API_RESILIENCE;

import com.taja.application.weather.WeatherClient;
import com.taja.domain.weather.DistrictPoint;
import com.taja.domain.weather.WeatherHistory;
import com.taja.global.exception.ApiException;
import com.taja.global.exception.NoRetryApiException;
import com.taja.infrastructure.api.weather.dto.WeatherApiResponseDto;
import io.github.resilience4j.retry.annotation.Retry;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KmaWeatherClient implements WeatherClient {

    private final KmaWeatherFeignClient kmaWeatherFeignClient;

    private static final String SUCCESS_RESULT_CODE = "00";
    
    private static final Set<String> NON_RETRYABLE_RESULT_CODES = Set.of("10", "11", "12", "20", "30", "31", "22", "32");

    @Value("${api.weather.key}")
    private String apiKey;

    @Override
    @Retry(name = WEATHER_API_RESILIENCE, fallbackMethod = "weatherApiFallback")
    public WeatherHistory fetchWeatherHistory(DistrictPoint districtPoint, LocalDateTime requestedAt) {
        String baseDate = requestedAt.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String baseTime = requestedAt.format(DateTimeFormatter.ofPattern("HHmm"));
        return getUltraShortNowcast(baseDate, baseTime, districtPoint);
    }

    public WeatherHistory weatherApiFallback(DistrictPoint districtPoint, LocalDateTime requestedAt, Throwable t) {
        log.warn("기상청 API 호출 실패 - 지역: {}, 요청 시간: {}, 오류: {}", districtPoint.getDistrictName(), requestedAt, t.getMessage());
        return null;
    }

    private WeatherHistory getUltraShortNowcast(String baseDate, String baseTime, DistrictPoint point) {
        WeatherApiResponseDto weatherApiResponseDto =
                kmaWeatherFeignClient.getUltraShortNowcast(
                        apiKey, "JSON", 10, 1, baseDate, baseTime, point.getXPoint(), point.getYPoint());

        if (weatherApiResponseDto == null
                || weatherApiResponseDto.response() == null
                || weatherApiResponseDto.response().header() == null) {
            throw new ApiException("JSON_PARSING_ERROR", "API 응답 DTO 파싱에 실패했습니다.");
        }

        String resultCode = weatherApiResponseDto.response().header().resultCode();
        String resultMsg = weatherApiResponseDto.response().header().resultMsg();

        if (SUCCESS_RESULT_CODE.equals(resultCode)) {
            return weatherApiResponseDto.toWeatherHistory(point.getDistrictName());
        }

        if (NON_RETRYABLE_RESULT_CODES.contains(resultCode)) {
            throw new NoRetryApiException(resultCode, resultMsg);
        }

        throw new ApiException(resultCode, resultMsg);
    }
}
