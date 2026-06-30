import http from 'k6/http';
import { check } from 'k6';

// ============================================================
// 콜드 스타트 스파이크 테스트 (조건 D)
//
// 목적: @Transactional 제거의 부작용 ① "cache miss 시 커넥션 churn" 검증.
//   - hash(stations:*)를 비운 직후 고정 RPS로 즉시 부하 → 콜드 스타트 전이 구간 관찰
//   - radius 모드 고정: latDelta/lngDelta < 0.03 → 항상 findStationInfos(hash/DB 경로) 실행
//     (cluster 모드는 클러스터링 후 반환해 DB 경로를 안 타므로 제외)
//
// 사용:
//   warm 베이스라인:  k6 run -e TESTID=warm -e DURATION=60s ...
//   (hash flush 후)
//   cold 스타트:       k6 run -e TESTID=cold -e DURATION=120s ...
//
// Prometheus remote-write:
//   K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
//   K6_PROMETHEUS_RW_TREND_STATS="p(95),p(99),avg,max" \
//   k6 run -o experimental-prometheus-rw k6/coldstart-load-test.js
// ============================================================

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const RATE = Number(__ENV.RATE || 700);          // 고정 도착률 (warm knee ~725 직전)
const DURATION = __ENV.DURATION || '120s';
const TESTID = __ENV.TESTID || 'cold';

// 서울 bbox
const LAT_MIN = 37.45, LAT_MAX = 37.65;
const LNG_MIN = 126.85, LNG_MAX = 127.10;
// radius delta (< 0.03)
const DELTA_MIN = 0.005, DELTA_MAX = 0.02;

function rand(min, max) { return Math.random() * (max - min) + min; }

export const options = {
    discardResponseBodies: true,
    summaryTrendStats: ['avg', 'min', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        spike: {
            executor: 'constant-arrival-rate',
            rate: RATE,
            timeUnit: '1s',
            duration: DURATION,
            preAllocatedVUs: 300,
            maxVUs: 3000,
            tags: { testid: TESTID },
        },
    },
    thresholds: {
        http_req_duration: ['p(99)<500'],
        http_req_failed: ['rate<0.01'],
        dropped_iterations: ['rate<0.01'],
    },
};

// 워밍업 없음 (콜드 상태를 그대로 측정)
export default function () {
    const lat = rand(LAT_MIN, LAT_MAX);
    const lng = rand(LNG_MIN, LNG_MAX);
    const latDelta = rand(DELTA_MIN, DELTA_MAX);
    const lngDelta = rand(DELTA_MIN, DELTA_MAX);
    const url = `${BASE_URL}/stations/map/nearby?latitude=${lat}&longitude=${lng}&latDelta=${latDelta}&lngDelta=${lngDelta}`;
    const res = http.get(url, { tags: { name: 'map_nearby' } });
    check(res, { 'status is 200': (r) => r.status === 200 });
}
