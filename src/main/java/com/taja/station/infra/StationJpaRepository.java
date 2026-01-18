package com.taja.station.infra;

import com.taja.station.domain.Station;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationJpaRepository extends JpaRepository<Station, Long> {
    List<Station> findAllByNumberIn(List<Integer> numbers);

    List<Station> findByNameContaining(String keyword);

    Optional<Station> findByNumber(int stationNumber);
}
