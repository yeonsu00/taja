package com.taja.station.infra;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.taja.station.presentation.response.NearbyStationResponse;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands.DistanceUnit;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoShape;

@ExtendWith(MockitoExtension.class)
class StationRedisRepositoryImplTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private GeoOperations<String, Object> geoOperations;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @InjectMocks
    private StationRedisRepositoryImpl stationRedisRepository;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForGeo()).thenReturn(geoOperations);
    }

    @DisplayName("주변 대여소를 성공적으로 조회하고, 각 대여소의 자전거 수를 HASH에서 가져와 합친다.")
    @Test
    void findNearbyStations_success() {
        // given
        GeoLocation<Object> location1 = createGeoLocation("101", 127.001, 37.501);
        GeoLocation<Object> location2 = createGeoLocation("102", 127.002, 37.502);
        setupGeoSearch(createGeoResults(location1, location2));

        setupHashOperations();
        String requestedAtStr = "2025-09-09T14:30:00";
        setupHashValues("101", 10, requestedAtStr);
        setupHashValues("102", 5, requestedAtStr);

        // when
        List<NearbyStationResponse> results = stationRedisRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).hasSize(2);
        NearbyStationResponse result1 = results.getFirst();
        assertThat(result1.number()).isEqualTo(101);
        assertThat(result1.bikeCount()).isEqualTo(10);
        assertThat(result1.requestedAt()).isEqualTo(LocalDateTime.parse(requestedAtStr));
        NearbyStationResponse result2 = results.get(1);
        assertThat(result2.number()).isEqualTo(102);
        assertThat(result2.bikeCount()).isEqualTo(5);
    }

    @DisplayName("GEO 검색 결과가 null일 경우 빈 리스트를 반환한다.")
    @Test
    void findNearbyStations_whenGeoResultIsNull() {
        // given
        setupGeoSearch(null);

        // when
        List<NearbyStationResponse> results = stationRedisRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).isNotNull().isEmpty();
    }

    @DisplayName("HASH 데이터가 없을 경우 기본값(자전거 0대, 시간 null)으로 응답한다.")
    @Test
    void findNearbyStations_whenHashDataIsMissing() {
        // given
        GeoLocation<Object> location = createGeoLocation("201", 127.1, 37.6);
        setupGeoSearch(createGeoResults(location));
        setupHashOperations();
        setupHashValues("201", null, null);

        // when
        List<NearbyStationResponse> results = stationRedisRepository.findNearbyStations(37.6, 127.1, 1.0, 1.0);

        // then
        assertThat(results).hasSize(1);
        NearbyStationResponse result = results.getFirst();
        assertThat(result.number()).isEqualTo(201);
        assertThat(result.bikeCount()).isEqualTo(0);
        assertThat(result.requestedAt()).isNull();
    }

    @DisplayName("대여소 번호가 숫자가 아닐 때(NumberFormatException), 해당 대여소를 결과에서 제외한다.")
    @Test
    void findNearbyStations_excludeStation_whenMemberIsNotNumeric() {
        // given
        GeoLocation<Object> validLocation = createGeoLocation("101", 127.001, 37.501);
        GeoLocation<Object> invalidLocation = createGeoLocation("STRING", 127.002, 37.502);
        setupGeoSearch(createGeoResults(validLocation, invalidLocation));

        setupHashOperations();
        setupHashValues("101", 10, "2025-09-09T15:00:00");

        // when
        List<NearbyStationResponse> results = stationRedisRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().number()).isEqualTo(101);
    }

    @DisplayName("날짜 형식이 잘못되었을 때(DateTimeParseException), 해당 대여소를 결과에서 제외한다.")
    @Test
    void findNearbyStations_excludeStation_whenDateFormatIsInvalid() {
        // given
        GeoLocation<Object> location = createGeoLocation("102", 127.003, 37.503);
        setupGeoSearch(createGeoResults(location));
        setupHashOperations();
        setupHashValues("102", 5, "INVALID_DATE");

        // when
        List<NearbyStationResponse> results = stationRedisRepository.findNearbyStations(37.5, 127.0, 1.0, 1.0);

        // then
        assertThat(results).isNotNull().isEmpty();
    }

    private void setupGeoSearch(GeoResults<GeoLocation<Object>> results) {
        when(geoOperations.search(anyString(), any(), any(GeoShape.class), any())).thenReturn(results);
    }

    private void setupHashOperations() {
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    private void setupHashValues(String stationNumber, Object bikeCount, Object requestedAt) {
        when(hashOperations.get("stations:" + stationNumber, "bikeCount")).thenReturn(bikeCount);
        when(hashOperations.get("stations:" + stationNumber, "requestedAt")).thenReturn(requestedAt);
    }

    private GeoLocation<Object> createGeoLocation(String id, double longitude, double latitude) {
        return new GeoLocation<>(id, new Point(longitude, latitude));
    }

    @SafeVarargs
    private GeoResults<GeoLocation<Object>> createGeoResults(GeoLocation<Object>... locations) {
        List<GeoResult<GeoLocation<Object>>> geoResultList = Arrays.stream(locations)
                .map(loc -> new GeoResult<>(loc, new Distance(0.1, DistanceUnit.KILOMETERS)))
                .collect(Collectors.toList());
        return new GeoResults<>(geoResultList, DistanceUnit.KILOMETERS);
    }
}
