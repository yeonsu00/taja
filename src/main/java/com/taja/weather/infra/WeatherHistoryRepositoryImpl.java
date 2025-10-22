package com.taja.weather.infra;

import com.taja.weather.application.WeatherHistoryRepository;
import com.taja.weather.domain.WeatherHistory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WeatherHistoryRepositoryImpl implements WeatherHistoryRepository {

    private final WeatherHistoryJpaRepository weatherHistoryJpaRepository;

    @Override
    public void saveAll(List<WeatherHistory> weatherHistories, LocalDateTime requestedAt) {
        List<WeatherHistoryEntity> weatherHistoryEntities = weatherHistories.stream()
                .map(weatherHistory -> WeatherHistoryEntity.fromWeatherHistory(weatherHistory, requestedAt))
                .toList();
        weatherHistoryJpaRepository.saveAll(weatherHistoryEntities);
    }
}
