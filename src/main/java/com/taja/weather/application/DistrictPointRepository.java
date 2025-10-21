package com.taja.weather.application;

import com.taja.weather.domain.DistrictPoint;
import java.util.List;

public interface DistrictPointRepository {
    int upsert(List<DistrictPoint> districtPoints);
}
