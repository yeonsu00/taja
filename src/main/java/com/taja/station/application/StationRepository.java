package com.taja.station.application;

import com.taja.station.domain.Station;
import java.util.List;

public interface StationRepository {
    int upsert(List<Station> stations);

    List<Station> findByNameContaining(String keyword);
}
