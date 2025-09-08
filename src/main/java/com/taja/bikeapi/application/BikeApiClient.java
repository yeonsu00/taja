package com.taja.bikeapi.application;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taja.bikeapi.application.dto.station.StationApiResponseDto;
import com.taja.bikeapi.application.dto.station.StationDto;
import com.taja.bikeapi.application.dto.status.BikeApiResponseDto;
import com.taja.bikeapi.application.dto.status.ResultDto;
import com.taja.bikeapi.application.dto.status.StationStatusDto;
import com.taja.global.exception.ApiException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class BikeApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.bike.path}")
    private String apiBikePath;

    @Value("${api.station.path}")
    private String apiStationPath;

    public Mono<List<StationStatusDto>> fetchStationStatuses(int startIndex, int endIndex) {
        String path = getPath(apiBikePath, startIndex, endIndex);

        return webClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(this::handleStationStatusApiResponse);
    }

    public Mono<List<StationDto>> fetchStations(int startIndex, int endIndex) {
        String path = getPath(apiStationPath, startIndex, endIndex);

        return webClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(this::handleStationApiResponse);

    }

    private String getPath(String apiPath, int startIndex, int endIndex) {
        return apiKey + apiPath + startIndex + "/" + endIndex;
    }

    private Mono<List<StationStatusDto>> handleStationStatusApiResponse(JsonNode jsonNode) {
        return handleApiResponse(
                jsonNode,
                "rentBikeStatus",
                BikeApiResponseDto.class,
                dto -> dto.rentBikeStatus().result().code(),
                dto -> dto.rentBikeStatus().result().message(),
                dto -> dto.rentBikeStatus().stationStatuses()
        );
    }

    private Mono<List<StationDto>> handleStationApiResponse(JsonNode jsonNode) {
        return handleApiResponse(
                jsonNode,
                "stationInfo",
                StationApiResponseDto.class,
                dto -> dto.stationInfo().result().code(),
                dto -> dto.stationInfo().result().message(),
                dto -> dto.stationInfo().stations()
        );
    }

    private <T, R> Mono<List<R>> handleApiResponse(
            JsonNode jsonNode,
            String rootKey,
            Class<T> responseClass,
            java.util.function.Function<T, String> codeExtractor,
            java.util.function.Function<T, String> messageExtractor,
            java.util.function.Function<T, List<R>> dataExtractor
    ) {
        try {
            if (jsonNode.has(rootKey)) {
                T successResponse = objectMapper.treeToValue(jsonNode, responseClass);
                String code = codeExtractor.apply(successResponse);
                if ("INFO-000".equals(code)) {
                    return Mono.just(dataExtractor.apply(successResponse));
                }
                return Mono.error(new ApiException(code, messageExtractor.apply(successResponse)));
            }

            if (jsonNode.has("RESULT")) {
                JsonNode resultNode = jsonNode.get("RESULT");
                String code = resultNode.get("CODE").asText();
                String message = resultNode.get("MESSAGE").asText();
                return Mono.error(new ApiException(code, message));
            }

            ResultDto simpleResponse = objectMapper.treeToValue(jsonNode, ResultDto.class);
            return Mono.error(new ApiException(simpleResponse.code(), simpleResponse.message()));

        } catch (Exception e) {
            return Mono.error(new ApiException("JSON_PARSING_ERROR", "응답 JSON 파싱에 실패했습니다: " + jsonNode.toString()));
        }
    }

}
