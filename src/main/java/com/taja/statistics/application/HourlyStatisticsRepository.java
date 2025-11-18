package com.taja.statistics.application;

import com.taja.statistics.infra.HourlyStatisticsEntity;
import java.util.List;

public interface HourlyStatisticsRepository {
    HourlyStatisticsEntity findByStationIdAndHour(Long stationId, int hour);

    void saveAll(List<HourlyStatisticsEntity> toSave);
}
