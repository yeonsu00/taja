package com.taja.infrastructure.weather;

import com.taja.application.weather.WeatherHistoryRepository;
import com.taja.domain.weather.WeatherHistory;
import java.time.LocalDate;
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
        weatherHistories.forEach(weatherHistory -> weatherHistory.updateRequestedAt(requestedAt));
        weatherHistoryJpaRepository.saveAll(weatherHistories);
    }

    @Override
    public List<WeatherHistory> findAllByBaseDate(LocalDate baseDate) {
        return weatherHistoryJpaRepository.findAllByBaseDate(baseDate);
    }
}
