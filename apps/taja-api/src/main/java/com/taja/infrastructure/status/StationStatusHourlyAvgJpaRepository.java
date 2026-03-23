package com.taja.infrastructure.status;

import com.taja.domain.status.StationStatusHourlyAvg;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StationStatusHourlyAvgJpaRepository extends JpaRepository<StationStatusHourlyAvg, Long> {

    @Query("""
            SELECT s FROM StationStatusHourlyAvg s
            WHERE s.baseDate = :baseDate AND s.baseHour = :baseHour AND s.stationNumber IN :stationNumbers
            """)
    List<StationStatusHourlyAvg> findAllByBaseDateAndBaseHourAndStationNumbers(
            @Param("baseDate") LocalDate baseDate,
            @Param("baseHour") Integer baseHour,
            @Param("stationNumbers") List<Integer> stationNumbers);

    @Query("SELECT s FROM StationStatusHourlyAvg s WHERE s.baseDate = :baseDate")
    List<StationStatusHourlyAvg> findAllByBaseDate(@Param("baseDate") LocalDate baseDate);

    @Query("""
            SELECT s FROM StationStatusHourlyAvg s
            WHERE s.baseDate = :baseDate AND s.stationNumber IN :stationNumbers
            """)
    List<StationStatusHourlyAvg> findAllByBaseDateAndStationNumbers(
            @Param("baseDate") LocalDate baseDate,
            @Param("stationNumbers") List<Integer> stationNumbers);
}
