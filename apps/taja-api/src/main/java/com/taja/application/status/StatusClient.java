package com.taja.application.status;

import com.taja.infrastructure.client.bike.dto.status.StationStatusDto;
import java.util.List;
import reactor.core.publisher.Mono;

public interface StatusClient {
    Mono<List<StationStatusDto>> fetchStationStatuses(int startIndex, int endIndex);
}
