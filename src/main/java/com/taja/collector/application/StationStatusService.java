package com.taja.collector.application;

import com.taja.collector.application.dto.StationDto;
import com.taja.collector.domain.StationStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StationStatusService {

    private final StationStatusApiService stationStatusApiService;
    private final StationStatusRepository stationStatusRepository;

    @Transactional
    public int uploadStationStatus() {
        List<StationDto> response = stationStatusApiService.getStationStatusApiResponse();

        LocalDateTime requestedAt = LocalDateTime.now();

        List<StationStatus> stationStatuses = response.stream()
                .map(stationDto -> stationDto.toStationStatus(requestedAt))
                .toList();

        return stationStatusRepository.saveAll(stationStatuses);
    }

}
