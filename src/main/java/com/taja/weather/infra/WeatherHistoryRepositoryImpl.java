package com.taja.weather.infra;

import com.taja.weather.application.WeatherHistoryRepository;
import com.taja.weather.domain.WeatherHistory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WeatherHistoryRepositoryImpl implements WeatherHistoryRepository {

    private final WeatherHistoryJpaRepository weatherHistoryJpaRepository;

    @Override
    public void saveAll(List<WeatherHistory> weatherHistories) {
        List<WeatherHistoryEntity> weatherHistoryEntities = weatherHistories.stream()
                .map(WeatherHistoryEntity::fromWeatherHistory)
                .toList();
        weatherHistoryJpaRepository.saveAll(weatherHistoryEntities);
    }
}
