package com.taja.interfaces.api.station.response.detail;

import com.taja.domain.statistics.DayOfWeekStatistics;
import com.taja.domain.statistics.HourlyStatistics;
import com.taja.domain.statistics.TemperatureStatistics;
import com.taja.domain.station.OperationMode;
import com.taja.domain.station.Station;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record StationDetailResponse(
        Long stationId,
        String number,
        String name,
        String address,
        double latitude,
        double longitude,
        List<OperationModeResponse> operationMode,
        TodayAvailableBikeResponse todayAvailableBike,
        List<RecentPostResponse> recentPosts,
        List<NearbyAvailableStationDetailResponse> nearbyAvailableStations,
        List<HourlyAvailableItemResponse> hourlyAvailable,
        List<DailyAvailableItemResponse> dailyAvailable,
        List<TemperatureAvailableItemResponse> temperatureAvailable
) {

    private static final Map<DayOfWeek, String> DAY_OF_WEEK_KOREAN = Map.of(
            DayOfWeek.MONDAY, "월",
            DayOfWeek.TUESDAY, "화",
            DayOfWeek.WEDNESDAY, "수",
            DayOfWeek.THURSDAY, "목",
            DayOfWeek.FRIDAY, "금",
            DayOfWeek.SATURDAY, "토",
            DayOfWeek.SUNDAY, "일"
    );

    public static StationDetailResponse of(
            Station station,
            TodayAvailableBikeResponse todayAvailableBike,
            List<RecentPostResponse> recentPosts,
            List<Station> nearbyStations,
            List<HourlyStatistics> hourlyStatistics,
            List<DayOfWeekStatistics> dayOfWeekStatistics,
            List<TemperatureStatistics> temperatureStatistics
    ) {
        List<OperationModeResponse> operationModes = getOperationModeResponses(station);

        List<NearbyAvailableStationDetailResponse> nearbyAvailableStations = nearbyStations.stream()
                .map(nearbyStation -> new NearbyAvailableStationDetailResponse(
                        nearbyStation.getStationId(),
                        String.valueOf(nearbyStation.getNumber()),
                        nearbyStation.getName(),
                        nearbyStation.getLatitude(),
                        nearbyStation.getLongitude(),
                        nearbyStation.calculateDistanceTo(station.getLatitude(), station.getLongitude())
                ))
                .sorted(Comparator.comparingInt(NearbyAvailableStationDetailResponse::distance))
                .toList();

        List<HourlyAvailableItemResponse> hourlyAvailable = toHourlyAvailable(hourlyStatistics);
        List<DailyAvailableItemResponse> dailyAvailable = toDailyAvailable(dayOfWeekStatistics);
        List<TemperatureAvailableItemResponse> temperatureAvailable = toTemperatureAvailable(temperatureStatistics);

        return new StationDetailResponse(
                station.getStationId(),
                String.valueOf(station.getNumber()),
                station.getName(),
                station.getAddress(),
                station.getLatitude(),
                station.getLongitude(),
                operationModes,
                todayAvailableBike,
                recentPosts,
                nearbyAvailableStations,
                hourlyAvailable,
                dailyAvailable,
                temperatureAvailable
        );
    }

    private static final DateTimeFormatter BASE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static List<HourlyAvailableItemResponse> toHourlyAvailable(List<HourlyStatistics> hourlyStatistics) {
        if (hourlyStatistics == null || hourlyStatistics.isEmpty()) {
            return List.of();
        }
        String baseDate = hourlyStatistics.stream()
                .map(HourlyStatistics::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(BASE_DATE_FORMAT::format)
                .orElse(null);
        return hourlyStatistics.stream()
                .sorted(Comparator.comparing(HourlyStatistics::getBaseHour))
                .map(h -> new HourlyAvailableItemResponse(
                        h.getBaseHour(),
                        h.getAvgParkingBikeCount() != null ? h.getAvgParkingBikeCount() : 0,
                        baseDate))
                .toList();
    }

    private static List<DailyAvailableItemResponse> toDailyAvailable(List<DayOfWeekStatistics> dayOfWeekStatistics) {
        if (dayOfWeekStatistics == null || dayOfWeekStatistics.isEmpty()) {
            return List.of();
        }
        String baseDate = dayOfWeekStatistics.stream()
                .map(DayOfWeekStatistics::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(BASE_DATE_FORMAT::format)
                .orElse(null);
        return dayOfWeekStatistics.stream()
                .sorted(Comparator.comparing(DayOfWeekStatistics::getDayOfWeek))
                .map(d -> new DailyAvailableItemResponse(
                        DAY_OF_WEEK_KOREAN.get(d.getDayOfWeek()),
                        d.getAvgParkingBikeCount() != null ? d.getAvgParkingBikeCount() : 0,
                        baseDate))
                .toList();
    }

    private static List<TemperatureAvailableItemResponse> toTemperatureAvailable(List<TemperatureStatistics> temperatureStatistics) {
        if (temperatureStatistics == null || temperatureStatistics.isEmpty()) {
            return List.of();
        }
        String baseDate = temperatureStatistics.stream()
                .map(TemperatureStatistics::getUpdatedAt)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .map(BASE_DATE_FORMAT::format)
                .orElse(null);
        return temperatureStatistics.stream()
                .sorted(Comparator.comparing(TemperatureStatistics::getTemperatureRange))
                .map(t -> new TemperatureAvailableItemResponse(
                        t.getTemperatureRange(),
                        t.getAvgParkingBikeCount() != null ? t.getAvgParkingBikeCount() : 0,
                        baseDate))
                .toList();
    }

    private static List<OperationModeResponse> getOperationModeResponses(Station station) {
        List<OperationModeResponse> operationModes = new ArrayList<>();
        OperationMode mode = station.getOperationMode();

        if (mode == OperationMode.LCD) {
            operationModes.add(new OperationModeResponse(OperationMode.LCD.name(), station.getLcdHoldCount()));
        } else if (mode == OperationMode.QR) {
            operationModes.add(new OperationModeResponse(OperationMode.QR.name(), station.getQrHoldCount()));
        } else if (mode == OperationMode.LCD_QR) {
            operationModes.add(new OperationModeResponse(OperationMode.LCD.name(), station.getLcdHoldCount()));
            operationModes.add(new OperationModeResponse(OperationMode.QR.name(), station.getQrHoldCount()));
        } else if (mode == OperationMode.NEW) {
            operationModes.add(new OperationModeResponse(OperationMode.NEW.name(), station.getTotalHoldCount()));
        }

        return operationModes;
    }

}
