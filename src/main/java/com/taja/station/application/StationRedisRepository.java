package com.taja.station.application;

import com.taja.station.domain.Station;
import java.time.LocalDateTime;
import reactor.core.publisher.Mono;

public interface StationRedisRepository {
    Mono<Boolean> saveStation(Station station, LocalDateTime savedAt);
}
