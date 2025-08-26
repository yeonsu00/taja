package com.taja.station.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StationJpaRepository extends JpaRepository<StationEntity, Long> {
}
