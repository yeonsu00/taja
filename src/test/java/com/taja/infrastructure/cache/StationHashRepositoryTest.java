package com.taja.infrastructure.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.taja.application.cache.StationInfo;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
@DisplayName("StationHashRepository 테스트")
class StationHashRepositoryTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedisTemplate<String, Object> redisTemplateMaster;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private StationHashRepository stationHashRepository;

    @BeforeEach
    void setUp() {
        stationHashRepository = new StationHashRepository(redisTemplate, redisTemplateMaster);
        // @Value 필드 설정
        ReflectionTestUtils.setField(stationHashRepository, "cacheTtlSec", 3600L);
        ReflectionTestUtils.setField(stationHashRepository, "refreshThresholdSec", 10L);
    }

    @DisplayName("fetchFullInfo는 캐시에서 대여소 정보를 조회한다")
    @Test
    void fetchFullInfo_retrievesFromCache() {
        // given
        Integer number = 101;
        double lat = 37.5665;
        double lon = 126.9780;
        String hashKey = "stations:101";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(List.of("5", "2025-01-27T14:30:00", "1"));

        // when
        Optional<StationInfo.StationFullInfo> result = stationHashRepository.fetchFullInfo(number, lat, lon);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().number()).isEqualTo(number);
        assertThat(result.get().bikeCount()).isEqualTo(5);
        assertThat(result.get().stationId()).isEqualTo(1L);
    }

    @DisplayName("fetchFullInfo는 캐시에 데이터가 없으면 빈 Optional을 반환한다")
    @Test
    void fetchFullInfo_whenCacheMiss_returnsEmpty() {
        // given
        Integer number = 102;
        double lat = 37.5665;
        double lon = 126.9780;
        String hashKey = "stations:102";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(Arrays.asList(null, null, null));  // stationId가 null

        // when
        Optional<StationInfo.StationFullInfo> result = stationHashRepository.fetchFullInfo(number, lat, lon);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("isThresholdReached는 TTL이 임계값보다 적으면 true를 반환한다")
    @Test
    void isThresholdReached_whenTtlBelowThreshold_returnsTrue() {
        // given
        Integer number = 103;
        String hashKey = "stations:103";

        when(redisTemplate.getExpire(hashKey)).thenReturn(5L);  // 5초 남음 (임계값 10초보다 적음)

        // when
        boolean result = stationHashRepository.isThresholdReached(number);

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("isThresholdReached는 TTL이 임계값보다 크면 false를 반환한다")
    @Test
    void isThresholdReached_whenTtlAboveThreshold_returnsFalse() {
        // given
        Integer number = 104;
        String hashKey = "stations:104";

        when(redisTemplate.getExpire(hashKey)).thenReturn(15L);  // 15초 남음 (임계값 10초보다 큼)

        // when
        boolean result = stationHashRepository.isThresholdReached(number);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("isThresholdReached는 TTL이 -1(만료되지 않음)이면 false를 반환한다")
    @Test
    void isThresholdReached_whenTtlIsNegative_returnsFalse() {
        // given
        Integer number = 105;
        String hashKey = "stations:105";

        when(redisTemplate.getExpire(hashKey)).thenReturn(-1L);  // 키가 없거나 만료되지 않음

        // when
        boolean result = stationHashRepository.isThresholdReached(number);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("acquireLock은 락 획득 성공 시 true를 반환한다")
    @Test
    void acquireLock_whenSuccessful_returnsTrue() {
        // given
        Integer number = 106;
        String lockKey = "lock:station:106";

        when(redisTemplateMaster.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(lockKey), eq("locked"), any(Duration.class)))
                .thenReturn(true);

        // when
        boolean result = stationHashRepository.acquireLock(number);

        // then
        assertThat(result).isTrue();
        verify(valueOperations).setIfAbsent(eq(lockKey), eq("locked"), eq(Duration.ofSeconds(5)));
    }

    @DisplayName("acquireLock은 락 획득 실패 시 false를 반환한다")
    @Test
    void acquireLock_whenFailed_returnsFalse() {
        // given
        Integer number = 107;
        String lockKey = "lock:station:107";

        when(redisTemplateMaster.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent(eq(lockKey), eq("locked"), any(Duration.class)))
                .thenReturn(false);  // 이미 락이 있음

        // when
        boolean result = stationHashRepository.acquireLock(number);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("releaseLock은 락을 해제한다")
    @Test
    void releaseLock_deletesLock() {
        // given
        Integer number = 108;
        String lockKey = "lock:station:108";

        // when
        stationHashRepository.releaseLock(number);

        // then
        verify(redisTemplateMaster).delete(lockKey);
    }

    @DisplayName("parseFullInfo는 파싱 실패 시 빈 Optional을 반환한다")
    @Test
    void parseFullInfo_whenParsingFails_returnsEmpty() {
        // given
        Integer number = 109;
        double lat = 37.5665;
        double lon = 126.9780;
        String hashKey = "stations:109";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(List.of("invalid", "invalid-date", "invalid-id"));  // 파싱 불가능한 값

        // when
        Optional<StationInfo.StationFullInfo> result = stationHashRepository.fetchFullInfo(number, lat, lon);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("fetchFullInfo는 bikeCount가 null이면 0으로 처리한다")
    @Test
    void fetchFullInfo_whenBikeCountIsNull_usesZero() {
        // given
        Integer number = 110;
        double lat = 37.5665;
        double lon = 126.9780;
        String hashKey = "stations:110";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(Arrays.asList(null, "2025-01-27T14:30:00", "1"));  // bikeCount가 null

        // when
        Optional<StationInfo.StationFullInfo> result = stationHashRepository.fetchFullInfo(number, lat, lon);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().bikeCount()).isEqualTo(0);
    }

    @DisplayName("fetchBikeCountByNumber는 Redis에서 남은 자전거 수를 조회한다")
    @Test
    void fetchBikeCountByNumber_retrievesFromCache() {
        // given
        Integer number = 101;
        String hashKey = "stations:101";
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(List.of("7", "2025-08-20T14:39:00", "1"));

        // when
        Optional<StationInfo.BikeCountInfo> result = stationHashRepository.fetchBikeCountByNumber(number);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().stationId()).isEqualTo(1L);
        assertThat(result.get().availableBikeCount()).isEqualTo(7);
        assertThat(result.get().requestedAt()).isEqualTo(
                java.time.LocalDateTime.parse("2025-08-20T14:39:00"));
    }

    @DisplayName("fetchBikeCountByNumber는 Redis에 stationId가 없으면 빈 Optional을 반환한다")
    @Test
    void fetchBikeCountByNumber_whenStationIdIsNull_returnsEmpty() {
        // given (List.of does not allow null; use Arrays.asList)
        Integer number = 102;
        String hashKey = "stations:102";
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(Arrays.asList("3", "2025-08-20T14:39:00", null));

        // when
        Optional<StationInfo.BikeCountInfo> result = stationHashRepository.fetchBikeCountByNumber(number);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("fetchBikeCountByNumber는 Redis에 requestedAt이 없으면 빈 Optional을 반환한다")
    @Test
    void fetchBikeCountByNumber_whenRequestedAtIsNull_returnsEmpty() {
        // given (List.of does not allow null; use Arrays.asList)
        Integer number = 103;
        String hashKey = "stations:103";
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(Arrays.asList("5", null, "1"));

        // when
        Optional<StationInfo.BikeCountInfo> result = stationHashRepository.fetchBikeCountByNumber(number);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("fetchBikeCountByNumber는 파싱 실패 시 빈 Optional을 반환한다")
    @Test
    void fetchBikeCountByNumber_whenParsingFails_returnsEmpty() {
        // given
        Integer number = 104;
        String hashKey = "stations:104";
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.multiGet(eq(hashKey), anyList()))
                .thenReturn(List.of("invalid", "invalid-date", "1"));

        // when
        Optional<StationInfo.BikeCountInfo> result = stationHashRepository.fetchBikeCountByNumber(number);

        // then
        assertThat(result).isEmpty();
    }
}
