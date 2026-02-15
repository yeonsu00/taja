package com.taja.application.station;

import com.taja.domain.station.Station;
import java.util.List;

public interface StationClient {
    List<Station> fetchStationInfos(int startIndex, int endIndex);
}
