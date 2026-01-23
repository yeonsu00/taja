package com.taja.infrastructure.client.bike;

import com.taja.application.station.StationClient;
import com.taja.domain.station.Station;
import com.taja.infrastructure.client.bike.dto.station.StationDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeoulDataStationInfoClient implements StationClient {

    private final SeoulDataStationInfoFeignClient seoulDataStationInfoFeignClient;

    @Override
    public List<Station> fetchStationInfos(int startIndex, int endIndex) {
        List<StationDto> loadedStations = seoulDataStationInfoFeignClient.getTbCycleStationInfo(startIndex, endIndex)
                .stationInfo()
                .stations();

        return StationDto.toStations(loadedStations);
    }
}
