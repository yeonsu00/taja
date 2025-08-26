package com.taja.station.application;

import com.taja.station.domain.Station;
import java.util.List;

public interface StationRepository {
    int saveAll(List<Station> stations);
}
