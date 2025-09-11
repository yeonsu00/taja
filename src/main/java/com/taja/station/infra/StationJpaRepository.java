package com.taja.station.infra;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationJpaRepository extends JpaRepository<StationEntity, Long> {
    List<StationEntity> findAllByNumberIn(List<Integer> numbers);

    List<StationEntity> findByNameContaining(String keyword);
}
