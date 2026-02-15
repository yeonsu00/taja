package com.taja.application.weather;

import com.taja.domain.weather.DistrictPoint;
import java.util.List;

public interface DistrictPointRepository {
    int upsert(List<DistrictPoint> districtPoints);

    List<DistrictPoint> findAll();
}
