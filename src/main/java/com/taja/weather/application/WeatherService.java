package com.taja.weather.application;

import com.taja.weather.domain.DistrictPoint;
import java.time.LocalDateTime;
import java.util.List;
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

    @Transactional
    public int uploadDistrictPointFromFile(MultipartFile file) {
        List<DistrictPoint> districtPoints =  districtPointFileReader.readDistrictPointFromFile(file);
        return districtPointRepository.upsert(districtPoints);
    }

    public void saveWeatherHistories(LocalDateTime requestedAt) {
        List<DistrictPoint> districtPoints = districtPointRepository.findAll();
        weatherApiService.loadAndSaveWeatherHistories(districtPoints, requestedAt);
    }
}
