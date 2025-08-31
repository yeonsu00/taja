package com.taja.collector.application;

import com.taja.collector.application.dto.BikeApiResponseDto;
import com.taja.collector.application.dto.StationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class StationStatusApiService {

    private final WebClient webClient;

    @Value("${bike.api.key}")
    private String apiKey;

    @Value("${bike.api.path}")
    private String apiPath;

    public List<StationDto> getStationStatusApiResponse() {
        int startIndex = 1;
        int endIndex = 1000;
        String path = apiKey + apiPath + startIndex + "/" + endIndex;

        System.out.println(path);
        BikeApiResponseDto apiResponse = webClient.get()
                .uri(path)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(BikeApiResponseDto.class)
                .block();

        if (apiResponse != null && apiResponse.rentBikeStatus() != null) {
            return apiResponse.rentBikeStatus().stations();
        }

        return List.of();
    }

}
