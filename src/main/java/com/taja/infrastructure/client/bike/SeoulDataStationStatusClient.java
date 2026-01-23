package com.taja.infrastructure.client.bike;

import com.taja.application.status.StatusClient;
import com.taja.infrastructure.client.bike.dto.status.BikeApiResponseDto;
import com.taja.infrastructure.client.bike.dto.status.BikeStatusDto;
import com.taja.infrastructure.client.bike.dto.status.StationStatusDto;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class SeoulDataStationStatusClient implements StatusClient {

    private final WebClient bikeWebClient;
    private final String apiKey;
    private final String apiStatusPath;

    public SeoulDataStationStatusClient(
            @Qualifier("bikeWebClient") WebClient bikeWebClient,
            @Value("${api.bike.key}") String apiKey,
            @Value("${api.bike.status.path}") String apiStatusPath) {
        this.bikeWebClient = bikeWebClient;
        this.apiKey = apiKey;
        this.apiStatusPath = apiStatusPath;
    }

    @Override
    public Mono<List<StationStatusDto>> fetchStationStatuses(int startIndex, int endIndex) {
        String path = getPath(apiStatusPath, startIndex, endIndex);

        return bikeWebClient.get()
                .uri(path)
                .retrieve()
                .bodyToMono(BikeApiResponseDto.class)
                .map(response -> {
                    if (BikeApiResponseDto.hasErrorCode(response)) {
                        String resultCode = response.result().code();
                        String resultMessage = response.result().message();

                        return List.<StationStatusDto>of();
                    }

                    if (!BikeApiResponseDto.hasRentBikeStatus(response)) {
                        return List.<StationStatusDto>of();
                    }

                    BikeStatusDto rentBikeStatus = response.rentBikeStatus();
                    String resultCode = rentBikeStatus.result().code();
                    String resultMessage = rentBikeStatus.result().message();

                    if ("INFO-000".equals(resultCode)) {
                        List<StationStatusDto> stationStatuses = rentBikeStatus.stationStatuses();
                        log.info("✅ 대여소 상태 API 요청 성공 ({}-{}) | 수집된 데이터 수: {}", 
                                startIndex, endIndex, stationStatuses != null ? stationStatuses.size() : 0);
                        return stationStatuses != null ? stationStatuses : List.<StationStatusDto>of();
                    }

                    log.error("대여소 상태 API 에러 응답 ({}-{}) | CODE: {}, MESSAGE: {}",
                            startIndex, endIndex, resultCode, resultMessage);
                    return List.<StationStatusDto>of();
                })
                .onErrorResume(error -> {
                    log.error("대여소 상태 API 호출 실패 ({}-{}) | 오류: {}",
                            startIndex, endIndex, error.getMessage());
                    return Mono.just(List.<StationStatusDto>of());
                });
    }

    private String getPath(String apiPath, int startIndex, int endIndex) {
        return apiKey + apiPath + startIndex + "/" + endIndex;
    }

}
