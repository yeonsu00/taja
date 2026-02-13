package com.taja.infrastructure.cache;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.taja.application.cache.StationInfo;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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

    @DisplayName("fetchAllFields는 캐시에서 대여소 전체 정보를 조회한다")
    @Test
    void fetchAllFields_retrievesFromCache() {
        // given
        Integer number = 101;
        String hashKey = "stations:101";
        Map<Object, Object> entries = new HashMap<>();
        entries.put("stationId", "1");
        entries.put("name", "테스트 대여소");
        entries.put("bikeCount", "5");
        entries.put("requestedAt", "2025-01-27T14:30:00");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(entries);

        // when
        Optional<StationInfo.StationHashInfo> result = stationHashRepository.fetchAllFields(number);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().number()).isEqualTo(number);
        assertThat(result.get().stationId()).isEqualTo(1L);
        assertThat(result.get().name()).isEqualTo("테스트 대여소");
        assertThat(result.get().bikeCount()).isEqualTo(5);
    }

    @DisplayName("fetchAllFields는 캐시에 데이터가 없으면 빈 Optional을 반환한다")
    @Test
    void fetchAllFields_whenCacheMiss_returnsEmpty() {
        // given
        Integer number = 102;
        String hashKey = "stations:102";

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(new HashMap<>());

        // when
        Optional<StationInfo.StationHashInfo> result = stationHashRepository.fetchAllFields(number);

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

    @DisplayName("fetchAllFields는 파싱 실패 시 빈 Optional을 반환한다")
    @Test
    void fetchAllFields_whenParsingFails_returnsEmpty() {
        // given
        Integer number = 109;
        String hashKey = "stations:109";
        Map<Object, Object> entries = new HashMap<>();
        entries.put("stationId", "invalid-id");
        entries.put("name", "테스트");
        entries.put("bikeCount", "invalid");
        entries.put("requestedAt", "invalid-date");

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(entries);

        // when
        Optional<StationInfo.StationHashInfo> result = stationHashRepository.fetchAllFields(number);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("fetchAllFields는 bikeCount가 없으면 0으로 처리한다")
    @Test
    void fetchAllFields_whenBikeCountIsNull_usesZero() {
        // given
        Integer number = 110;
        String hashKey = "stations:110";
        Map<Object, Object> entries = new HashMap<>();
        entries.put("stationId", "1");
        entries.put("name", "테스트 대여소");
        entries.put("requestedAt", "2025-01-27T14:30:00");
        // bikeCount 없음

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(entries);

        // when
        Optional<StationInfo.StationHashInfo> result = stationHashRepository.fetchAllFields(number);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().bikeCount()).isEqualTo(0);
    }

    @DisplayName("fetchAllFields는 requestedAt이 없으면 null로 처리한다")
    @Test
    void fetchAllFields_whenRequestedAtIsNull_usesNull() {
        // given
        Integer number = 111;
        String hashKey = "stations:111";
        Map<Object, Object> entries = new HashMap<>();
        entries.put("stationId", "1");
        entries.put("name", "테스트 대여소");
        entries.put("bikeCount", "5");
        // requestedAt 없음

        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries(hashKey)).thenReturn(entries);

        // when
        Optional<StationInfo.StationHashInfo> result = stationHashRepository.fetchAllFields(number);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().requestedAt()).isNull();
    }
}
