package com.taja.weather.infra;

import com.taja.weather.domain.DistrictPoint;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictPointJpaRepository extends JpaRepository<DistrictPoint, Long> {
    Optional<DistrictPoint> findByDistrictName(String districtName);
}
