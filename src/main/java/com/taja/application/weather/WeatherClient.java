package com.taja.application.weather;

import com.taja.domain.weather.DistrictPoint;
import com.taja.domain.weather.WeatherHistory;
import java.time.LocalDateTime;

public interface WeatherClient {

    WeatherHistory fetchWeatherHistory(DistrictPoint districtPoint, LocalDateTime requestedAt);

}
