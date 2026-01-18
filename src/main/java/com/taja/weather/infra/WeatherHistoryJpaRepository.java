package com.taja.weather.infra;

import com.taja.weather.domain.WeatherHistory;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeatherHistoryJpaRepository extends JpaRepository<WeatherHistory, Long> {

    @Query("SELECT w FROM WeatherHistory w WHERE w.baseDate = :baseDate ORDER BY w.district, w.baseTime")
    List<WeatherHistory> findAllByBaseDate(@Param("baseDate") LocalDate baseDate);

}
