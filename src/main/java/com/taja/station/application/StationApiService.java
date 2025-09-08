package com.taja.station.application;

import com.taja.bikeapi.application.BikeApiBatchFetcher;
import com.taja.bikeapi.application.BikeApiClient;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class StationApiService {

    private final BikeApiClient bikeApiClient;
    private final BikeApiBatchFetcher bikeApiBatchFetcher;
    private final StationService stationService;

    private static final int TOTAL_COUNT = 3500;
    private static final int BATCH_SIZE = 500;

    public void loadStations(LocalDateTime requestedAt) {
        bikeApiBatchFetcher.fetchAll(TOTAL_COUNT, BATCH_SIZE, bikeApiClient::fetchStations)
                .subscribe(loadedStations -> stationService.saveStations(loadedStations, requestedAt));
    }
}
