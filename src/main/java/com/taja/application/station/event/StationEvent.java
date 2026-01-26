package com.taja.application.station.event;

import com.taja.domain.station.Station;
import com.taja.domain.status.StationStatus;
import java.time.LocalDateTime;
import java.util.List;

public class StationEvent {

    public record StationsSaved(
            List<Station> stations,
            LocalDateTime requestedAt
    ) {
    }

    public record StationStatusesUpdated(
            List<StationStatus> stationStatuses
    ) {
    }
}
