package com.taja.application.weather;

import com.taja.domain.weather.DistrictPoint;
import com.taja.domain.weather.WeatherHistory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final DistrictPointFileReader districtPointFileReader;
    private final DistrictPointRepository districtPointRepository;
    private final WeatherApiService weatherApiService;
    private final WeatherHistoryRepository weatherHistoryRepository;

    @Transactional
    public int uploadDistrictPointFromFile(MultipartFile file) {
        List<DistrictPoint> districtPoints = districtPointFileReader.readDistrictPointFromFile(file);
        return districtPointRepository.upsert(districtPoints);
    }

    public void saveWeatherHistories(LocalDateTime requestedAt) {
        List<DistrictPoint> districtPoints = districtPointRepository.findAll();
        weatherApiService.loadAndSaveWeatherHistories(districtPoints, requestedAt);
    }

    public Map<String, Map<Integer, Double>> findWeathersByBaseDate(LocalDate calculationDate) {
        List<WeatherHistory> weatherHistories = weatherHistoryRepository.findAllByBaseDate(calculationDate);
        return convertWeatherToMap(weatherHistories);
    }

    private Map<String, Map<Integer, Double>> convertWeatherToMap(List<WeatherHistory> weatherHistories) {
        Map<String, Map<Integer, Double>> result = new HashMap<>();

        for (WeatherHistory weather : weatherHistories) {
            String district = weather.getDistrict();
            Integer hour = weather.getBaseTime().getHour();
            Double temperature = weather.getTemperature();

            if (temperature == null) {
                continue;
            }

            result.computeIfAbsent(district, k -> new HashMap<>())
                    .put(hour, temperature);
        }

        return result;
    }

    public Double getTemperature(Map<String, Map<Integer, Double>> districtHourlyTempMap, String district, Integer hour) {
        Map<Integer, Double> hourlyMap = districtHourlyTempMap.get(district);

        if (hourlyMap == null) {
            return null;
        }

        return hourlyMap.get(hour);
    }
}
