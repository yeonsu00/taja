package com.taja.application.status;

import com.taja.infrastructure.client.bike.BikeApiBatchFetcher;
import com.taja.infrastructure.client.bike.BikeApiClient;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StationStatusApiService {

    private final BikeApiClient bikeApiClient;
    private final BikeApiBatchFetcher bikeApiBatchFetcher;
    private final StationStatusService stationStatusService;

    private static final int TOTAL_COUNT = 2800;
    private static final int BATCH_SIZE = 200;

    public void loadStationStatuses(LocalDateTime requestedAt) {
        bikeApiBatchFetcher.fetchAll(TOTAL_COUNT, BATCH_SIZE, bikeApiClient::fetchStationStatuses)
                .subscribe(loadedStationStatuses -> stationStatusService.saveStationStatuses(requestedAt, loadedStationStatuses));
    }

}
