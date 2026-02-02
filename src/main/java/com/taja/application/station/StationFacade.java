package com.taja.application.station;

import com.taja.application.board.BoardInfo;
import com.taja.application.board.PostService;
import com.taja.application.cache.StationCacheService;
import com.taja.application.cache.StationInfo;
import com.taja.application.statistics.DayOfWeekStatisticsService;
import com.taja.application.statistics.HourlyStatisticsService;
import com.taja.application.statistics.TemperatureStatisticsService;
import com.taja.application.status.StationStatusFacade;
import com.taja.domain.station.Station;
import com.taja.domain.statistics.DayOfWeekStatistics;
import com.taja.domain.statistics.HourlyStatistics;
import com.taja.domain.statistics.TemperatureStatistics;
import com.taja.interfaces.api.station.response.MapStationResponse;
import com.taja.interfaces.api.station.response.detail.RecentPostResponse;
import com.taja.interfaces.api.station.response.detail.StationDetailResponse;
import com.taja.interfaces.api.station.response.detail.TodayAvailableBikeResponse;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class StationFacade {

    private static final int RECENT_POSTS_SIZE = 3;

    private final StationService stationService;
    private final StationCacheService stationCacheService;
    private final PostService postService;
    private final StationStatusFacade stationStatusFacade;
    private final HourlyStatisticsService hourlyStatisticsService;
    private final DayOfWeekStatisticsService dayOfWeekStatisticsService;
    private final TemperatureStatisticsService temperatureStatisticsService;

    @Transactional
    public int uploadStationsFromFile(MultipartFile file, LocalDateTime requestedAt) {
        List<Station> savedStations = stationService.uploadStationsFromFile(file);
        stationCacheService.saveStations(savedStations, requestedAt);
        return savedStations.size();
    }

    @Transactional(readOnly = true)
    public List<MapStationResponse> findNearbyStations(double centerLat, double centerLon,
                                                       double latDelta, double lonDelta) {
        double height = latDelta * 2;
        double width = lonDelta * 2;

        List<StationInfo.StationGeoInfo> geoInfos = stationCacheService.findNearbyStations(centerLat, centerLon, height, width);
        List<StationInfo.StationFullInfo> stationInfos = stationCacheService.findStationInfos(geoInfos);

        return StationInfo.StationFullInfo.toMapStationResponses(stationInfos);
    }

    @Transactional(readOnly = true)
    public StationDetailResponse findStationDetail(Long stationId, LocalDateTime requestedAt) {
        Station station = stationService.findStationByStationId(stationId);

        List<StationInfo.StationGeoInfo> geoInfos = stationCacheService.findNearbyStations(station.getLatitude(), station.getLongitude(), 1, 1);
        List<StationInfo.StationFullInfo> stationInfos = stationCacheService.findStationInfos(geoInfos);

        List<MapStationResponse> nearbyStationsResponse = StationInfo.StationFullInfo.toMapStationResponses(stationInfos);

        List<Integer> nearbyStationsNumber =
                MapStationResponse.extractAvailableNumbers(
                        nearbyStationsResponse,
                        station.getNumber()
                );

        List<Station> nearbyStations = stationService.findStationByNumbers(nearbyStationsNumber);

        TodayAvailableBikeResponse todayAvailableBike = stationStatusFacade.getTodayAvailableBike(
                station.getStationId(), station.getNumber(), requestedAt);
        List<BoardInfo.PostItem> recentPostItems = postService.findRecentPosts(station.getStationId(), RECENT_POSTS_SIZE);
        List<RecentPostResponse> recentPosts = recentPostItems.stream()
                .limit(RECENT_POSTS_SIZE)
                .map(item -> new RecentPostResponse(item.writer(), item.content()))
                .toList();

        List<HourlyStatistics> hourlyStatistics = hourlyStatisticsService.findByStationId(station.getStationId());
        List<DayOfWeekStatistics> dayOfWeekStatistics = dayOfWeekStatisticsService.findByStationId(station.getStationId());
        List<TemperatureStatistics> temperatureStatistics = temperatureStatisticsService.findByStationId(station.getStationId());

        return StationDetailResponse.of(station, todayAvailableBike, recentPosts, nearbyStations,
                hourlyStatistics, dayOfWeekStatistics, temperatureStatistics);
    }
}
