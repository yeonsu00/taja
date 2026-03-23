package com.taja.interfaces.api.station.response;

import com.taja.application.cache.StationInfo.BikeCountInfo;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public record StationStatusResponse(
        Long stationId,
        int availableBikeCount,
        String availableBikeCountTimestamp
) {

    private static final DateTimeFormatter ISO_OFFSET_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

    public static StationStatusResponse from(BikeCountInfo bikeCountInfo) {
        String timestamp = bikeCountInfo.requestedAt()
                .atOffset(ZoneOffset.UTC)
                .format(ISO_OFFSET_FORMATTER);
        return new StationStatusResponse(
                bikeCountInfo.stationId(),
                bikeCountInfo.availableBikeCount(),
                timestamp
        );
    }
}
