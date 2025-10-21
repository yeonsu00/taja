package com.taja.weather.application;

import com.taja.weather.domain.DistrictPoint;
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

    @Transactional
    public int uploadDistrictPointFromFile(MultipartFile file) {
        List<DistrictPoint> districtPoints =  districtPointFileReader.readDistrictPointFromFile(file);
        return districtPointRepository.upsert(districtPoints);
    }
}
