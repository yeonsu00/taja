package com.taja.collector.application;

import com.taja.collector.domain.StationStatus;
import java.util.List;

public interface StationStatusRepository {
    int saveAll(List<StationStatus> stationStatuses);
}
