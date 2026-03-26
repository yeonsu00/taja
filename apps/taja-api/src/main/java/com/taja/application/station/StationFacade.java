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
import com.taja.interfaces.api.station.response.NearbyStationsResponse;
import com.taja.interfaces.api.station.response.StationClusterResponse;
import com.taja.interfaces.api.station.response.detail.RecentPostResponse;
import com.taja.interfaces.api.station.response.detail.StationDetailResponse;
import com.taja.interfaces.api.station.response.detail.TodayAvailableBikeResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Component
public class StationFacade {

    private static final int RECENT_POSTS_SIZE = 3;
    private static final double CLUSTER_THRESHOLD = 0.03;
    private static final int GRID_SIZE = 10;

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
    public NearbyStationsResponse findStationsInBounds(double centerLat, double centerLon,
                                                       double latDelta, double lonDelta) {
        double height = (latDelta * 2) * 111.0;
        double width = (lonDelta * 2) * 88.8;

        List<StationInfo.StationGeoInfo> geoInfos = stationCacheService.findStationsInBounds(centerLat, centerLon, height, width);

        if (latDelta >= CLUSTER_THRESHOLD || lonDelta >= CLUSTER_THRESHOLD) {
            List<StationClusterResponse> clusters = clusterStations(geoInfos, centerLat, centerLon, latDelta, lonDelta);
            return NearbyStationsResponse.ofClusters(clusters);
        }

        List<StationInfo.StationFullInfo> stationInfos = stationCacheService.findStationInfos(geoInfos);
        List<MapStationResponse> responses = StationInfo.StationFullInfo.toMapStationResponses(stationInfos);
        return NearbyStationsResponse.ofStations(responses);
    }

    private List<StationClusterResponse> clusterStations(List<StationInfo.StationGeoInfo> geoInfos,
                                                          double centerLat, double centerLon,
                                                          double latDelta, double lonDelta) {
        double minLat = centerLat - latDelta;
        double maxLat = centerLat + latDelta;
        double minLon = centerLon - lonDelta;
        double maxLon = centerLon + lonDelta;

        double cellLatSize = (maxLat - minLat) / GRID_SIZE;
        double cellLonSize = (maxLon - minLon) / GRID_SIZE;

        Map<Long, Integer> cellCounts = new HashMap<>();

        for (StationInfo.StationGeoInfo geo : geoInfos) {
            int row = Math.min((int) ((geo.latitude() - minLat) / cellLatSize), GRID_SIZE - 1);
            int col = Math.min((int) ((geo.longitude() - minLon) / cellLonSize), GRID_SIZE - 1);
            row = Math.max(row, 0);
            col = Math.max(col, 0);
            long cellKey = (long) row * GRID_SIZE + col;
            cellCounts.merge(cellKey, 1, Integer::sum);
        }

        List<StationClusterResponse> clusters = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cellCounts.entrySet()) {
            int row = (int) (entry.getKey() / GRID_SIZE);
            int col = (int) (entry.getKey() % GRID_SIZE);
            double clusterLat = minLat + (row + 0.5) * cellLatSize;
            double clusterLon = minLon + (col + 0.5) * cellLonSize;
            clusters.add(new StationClusterResponse(clusterLat, clusterLon, entry.getValue()));
        }

        return clusters;
    }

    @Transactional(readOnly = true)
    public StationDetailResponse findStationDetail(Long stationId, LocalDateTime requestedAt) {
        Station station = stationService.findStationByStationId(stationId);

        List<StationInfo.NearbyAvailableStation> nearbyAvailableStations =
                stationCacheService.findNearbyAvailableStations(station.getLatitude(), station.getLongitude(), 1.0, station.getNumber());

        TodayAvailableBikeResponse todayAvailableBike = stationStatusFacade.getTodayAvailableBike(
                station.getStationId(), station.getNumber(), requestedAt);
        List<BoardInfo.PostItem> recentPostItems = postService.findRecentPosts(station.getStationId(), RECENT_POSTS_SIZE);
        List<RecentPostResponse> recentPosts = recentPostItems.stream()
                .limit(RECENT_POSTS_SIZE)
                .map(item -> new RecentPostResponse(item.writer(), item.content()))
                .toList();

        List<HourlyStatistics> hourlyStatistics = hourlyStatisticsService.findHourlyStatisticsByStationId(station.getStationId());
        List<DayOfWeekStatistics> dayOfWeekStatistics = dayOfWeekStatisticsService.findDayOfWeekStatisticsByStationId(station.getStationId());
        List<TemperatureStatistics> temperatureStatistics = temperatureStatisticsService.findTemperatureStatisticsByStationId(station.getStationId());

        return StationDetailResponse.of(station, todayAvailableBike, recentPosts, nearbyAvailableStations,
                hourlyStatistics, dayOfWeekStatistics, temperatureStatistics);
    }
}
