package com.taja.collector.infra.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taja.collector.infra.api.dto.BikeApiResponseDto;
import com.taja.collector.infra.api.dto.ResultDto;
import com.taja.collector.infra.api.dto.StationDto;
import com.taja.global.exception.StationStatusApiException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StationStatusApiClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${api.key}")
    private String apiKey;

    @Value("${api.bike.path}")
    private String apiPath;

    public Mono<List<StationDto>> fetchStationStatuses(int startIndex, int endIndex) {
        String path = getPath(startIndex, endIndex);

        return webClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .flatMap(this::handleApiResponse);
    }

    private String getPath(int startIndex, int endIndex) {
        return apiKey + apiPath + startIndex + "/" + endIndex;
    }

    private Mono<List<StationDto>> handleApiResponse(JsonNode jsonNode) {
        try {
            if (jsonNode.has("rentBikeStatus")) {
                BikeApiResponseDto successResponse = objectMapper.treeToValue(jsonNode, BikeApiResponseDto.class);
                String code = successResponse.rentBikeStatus().result().code();
                if ("INFO-000".equals(code)) {
                    return Mono.just(successResponse.rentBikeStatus().stations());
                }
                return Mono.error(new StationStatusApiException(code, successResponse.rentBikeStatus().result().message()));
            }

            if (jsonNode.has("RESULT")) {
                JsonNode resultNode = jsonNode.get("RESULT");
                String code = resultNode.get("CODE").asText();
                String message = resultNode.get("MESSAGE").asText();
                return Mono.error(new StationStatusApiException(code, message));
            }

            ResultDto simpleResponse = objectMapper.treeToValue(jsonNode, ResultDto.class);
            return Mono.error(new StationStatusApiException(simpleResponse.code(), simpleResponse.message()));

        } catch (Exception e) {
            return Mono.error(new StationStatusApiException("JSON_PARSING_ERROR", "응답 JSON 파싱에 실패했습니다: " + jsonNode.toString()));
        }
    }

}
