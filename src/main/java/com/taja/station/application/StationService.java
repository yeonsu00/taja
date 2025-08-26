package com.taja.station.application;

import com.taja.station.domain.Station;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StationService {

    private final StationFileReader stationFileReader;
    private final StationRepository stationRepository;

    public int uploadStationData(MultipartFile file) {
        List<Station> readStations = stationFileReader.readStationsFromFile(file);
        return stationRepository.saveAll(readStations);
    }
}
