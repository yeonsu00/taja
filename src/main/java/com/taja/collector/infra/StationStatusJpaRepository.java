package com.taja.collector.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StationStatusJpaRepository extends JpaRepository<StationStatusEntity, Long> {
}
