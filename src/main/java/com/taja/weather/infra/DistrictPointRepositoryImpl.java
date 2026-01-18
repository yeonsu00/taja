package com.taja.weather.infra;

import com.taja.weather.application.DistrictPointRepository;
import com.taja.weather.domain.DistrictPoint;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class DistrictPointRepositoryImpl implements DistrictPointRepository {

    private final DistrictPointJpaRepository districtPointJpaRepository;

    @Override
    public int upsert(List<DistrictPoint> districtPoints) {
        for (DistrictPoint districtPoint : districtPoints) {
            DistrictPoint existing = districtPointJpaRepository
                    .findByDistrictName(districtPoint.getDistrictName())
                    .orElse(null);

            if (existing != null) {
                existing.updatePoint(districtPoint.getXPoint(), districtPoint.getYPoint());
            } else {
                districtPointJpaRepository.save(districtPoint);
            }
        }
        return districtPoints.size();
    }

    @Override
    public List<DistrictPoint> findAll() {
        return districtPointJpaRepository.findAll();
    }

}
