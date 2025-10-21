package com.taja.weather.infra;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DistrictPointJpaRepository extends JpaRepository<DistrictPointEntity, Long> {
    Optional<DistrictPointEntity> findByDistrictName(String districtName);
}
