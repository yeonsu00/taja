package com.taja.weather.infra;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeatherHistoryJpaRepository extends JpaRepository<WeatherHistoryEntity, Long> {

    @Query("SELECT w FROM WeatherHistoryEntity w WHERE w.baseDate = :baseDate ORDER BY w.district, w.baseTime")
    List<WeatherHistoryEntity> findAllByBaseDate(@Param("baseDate") LocalDate baseDate);

}
