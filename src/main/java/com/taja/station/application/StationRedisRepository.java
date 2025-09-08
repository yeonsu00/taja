package com.taja.station.application;

import com.taja.status.domain.StationStatus;
import com.taja.station.domain.Station;
import java.time.LocalDateTime;

public interface StationRedisRepository {
    boolean saveStation(Station station, LocalDateTime requestedAt);

    boolean updateBikeCountAndRequestedAt(StationStatus stationStatus);
}
