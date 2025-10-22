package com.taja.weather.application;

import com.taja.weather.domain.WeatherHistory;
import java.util.List;

public interface WeatherHistoryRepository {
    void saveAll(List<WeatherHistory> weatherHistories);
}
