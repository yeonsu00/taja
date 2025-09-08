package com.taja.status.application;

import com.taja.status.domain.StationStatus;
import java.util.List;

public interface StationStatusRepository {
    int saveAll(List<StationStatus> stationStatuses);
}
