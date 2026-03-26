package com.taja.application.favorite;

import com.taja.application.cache.StationCacheService;
import com.taja.application.cache.StationInfo;
import com.taja.domain.station.Station;
import com.taja.interfaces.api.station.response.MapStationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class FavoriteStationFacade {

    private final FavoriteStationService favoriteStationService;
    private final StationCacheService stationCacheService;

    @Transactional(readOnly = true)
    public List<MapStationResponse> findFavoriteStationsByMemberEmail(String email) {
        List<Station> favoriteStations = favoriteStationService.findFavoriteStationsByMemberEmail(email);
        List<StationInfo.StationFullInfo> stationInfos = stationCacheService.findStationStatus(favoriteStations);
        return StationInfo.StationFullInfo.toMapStationResponses(stationInfos);
    }

}
