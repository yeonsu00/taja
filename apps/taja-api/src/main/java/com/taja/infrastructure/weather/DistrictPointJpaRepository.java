package com.taja.infrastructure.weather;

import com.taja.domain.weather.DistrictPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictPointJpaRepository extends JpaRepository<DistrictPoint, Long> {
    Optional<DistrictPoint> findByDistrictName(String districtName);
}
