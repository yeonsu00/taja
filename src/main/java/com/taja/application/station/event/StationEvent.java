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
            List<StationStatus> stationStatuses,
            int startIndex,
            int endIndex
    ) {
        public static StationStatusesUpdated from(List<StationStatus> stationStatuses, int startIndex, int endIndex) {
            return new StationStatusesUpdated(stationStatuses, startIndex, endIndex);
        }
    }

    public record StationStatusesCollected(
            LocalDateTime requestedAt
    ) {
        public static StationStatusesCollected from(LocalDateTime requestedAt) {
            return new StationStatusesCollected(requestedAt);
        }
    }
}
