package com.taja.weather.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherHistoryJpaRepository extends JpaRepository<WeatherHistoryEntity, Long> {
}
