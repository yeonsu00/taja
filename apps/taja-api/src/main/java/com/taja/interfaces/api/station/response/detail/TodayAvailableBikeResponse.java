package com.taja.interfaces.api.station.response.detail;

import java.time.LocalDateTime;
import java.util.List;

public record TodayAvailableBikeResponse(
        LocalDateTime timeStamp,
        List<BikeCountByTimeResponse> observedBikeCountByHour,
        List<BikeCountByTimeResponse> predictedBikeCountByHour
) {
}
