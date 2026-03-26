package com.taja.application.weather;

import com.taja.domain.weather.WeatherHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WeatherHistoryRepository {
    void saveAll(List<WeatherHistory> weatherHistories, LocalDateTime requestedAt);

    List<WeatherHistory> findAllByBaseDate(LocalDate yesterday);
}
