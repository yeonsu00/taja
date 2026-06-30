import http from 'k6/http';
import { check } from 'k6';

// ============================================================
// 타자 조회 API breakpoint 부하 테스트 (조건 A)
//
// 주 대상: GET /stations/map/nearby  (Redis Geospatial + 줌 클러스터링)
//   - latDelta/lngDelta < 0.03  → 개별 대여소 목록 (반경/geo 조회)
//   - latDelta/lngDelta >= 0.03 → 10x10 그리드 클러스터링 (줌아웃)
//   MODE 로 두 모드를 섞거나(radius/cluster/mixed) 선택한다.
//
// 보조 대상: GET /stations/{stationId}  (ENDPOINT=station_detail 일 때)
//
// 실행 예시(조건 A, Prometheus remote-write):
//   K6_PROMETHEUS_RW_SERVER_URL=http://localhost:9090/api/v1/write \
//   K6_PROMETHEUS_RW_TREND_STATS="p(95),p(99),avg,max,min" \
//   k6 run -o experimental-prometheus-rw k6/breakpoint-load-test.js
// ============================================================

// ---------- 파라미터 (전부 env 로 오버라이드 가능) ----------
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ENDPOINT = __ENV.ENDPOINT || 'map_nearby';   // map_nearby | station_detail
const MODE = __ENV.MODE || 'mixed';                // mixed | radius | cluster (map_nearby 전용)

// 서울 대략 bounding box (좁은 1~10 캐시친화 회피 — 넓게 랜덤)
const LAT_MIN = Number(__ENV.LAT_MIN || 37.45);
const LAT_MAX = Number(__ENV.LAT_MAX || 37.65);
const LNG_MIN = Number(__ENV.LNG_MIN || 126.85);
const LNG_MAX = Number(__ENV.LNG_MAX || 127.10);

// delta 범위 (모드별)
const RADIUS_DELTA_MIN = Number(__ENV.RADIUS_DELTA_MIN || 0.005);
const RADIUS_DELTA_MAX = Number(__ENV.RADIUS_DELTA_MAX || 0.02);   // < 0.03
const CLUSTER_DELTA_MIN = Number(__ENV.CLUSTER_DELTA_MIN || 0.05);
const CLUSTER_DELTA_MAX = Number(__ENV.CLUSTER_DELTA_MAX || 0.2);  // >= 0.03

// station_detail 용 stationId 범위 (넓게)
const STATION_ID_MIN = Number(__ENV.STATION_ID_MIN || 1);
const STATION_ID_MAX = Number(__ENV.STATION_ID_MAX || 3000);

// VU 풀 — 가드레일: 예상 VU 의 2~3배로 넉넉히 (dropped 가 서버 한계인지 VU 부족인지 구분 위해)
const PRE_ALLOCATED_VUS = Number(__ENV.PRE_ALLOCATED_VUS || 300);
const MAX_VUS = Number(__ENV.MAX_VUS || 3000);

// breakpoint 계단: 0 → 50 → 100 → 250 → 500 → 1000 → PEAK
const STAGE_SECONDS = __ENV.STAGE_SECONDS || '45s';
const PEAK_RPS = Number(__ENV.PEAK_RPS || 2000);

// ---------- 유틸 ----------
function rand(min, max) {
    return Math.random() * (max - min) + min;
}
function randInt(min, max) {
    return Math.floor(Math.random() * (max - min + 1)) + min;
}
function pickDelta() {
    let useCluster;
    if (MODE === 'radius') useCluster = false;
    else if (MODE === 'cluster') useCluster = true;
    else useCluster = Math.random() < 0.5; // mixed
    return useCluster
        ? rand(CLUSTER_DELTA_MIN, CLUSTER_DELTA_MAX)
        : rand(RADIUS_DELTA_MIN, RADIUS_DELTA_MAX);
}

function buildRequest() {
    if (ENDPOINT === 'station_detail') {
        const id = randInt(STATION_ID_MIN, STATION_ID_MAX);
        return { url: `${BASE_URL}/stations/${id}`, name: 'station_detail' };
    }
    // map_nearby (기본)
    const lat = rand(LAT_MIN, LAT_MAX);
    const lng = rand(LNG_MIN, LNG_MAX);
    const latDelta = pickDelta();
    const lngDelta = pickDelta();
    const url = `${BASE_URL}/stations/map/nearby?latitude=${lat}&longitude=${lng}&latDelta=${latDelta}&lngDelta=${lngDelta}`;
    return { url, name: 'map_nearby' };
}

// ============================================================
// 옵션
// ============================================================
export const options = {
    discardResponseBodies: true,
    summaryTrendStats: ['avg', 'min', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        breakpoint: {
            executor: 'ramping-arrival-rate',
            startRate: 0,
            timeUnit: '1s',
            preAllocatedVUs: PRE_ALLOCATED_VUS,
            maxVUs: MAX_VUS,
            stages: [
                { target: 50, duration: STAGE_SECONDS },
                { target: 100, duration: STAGE_SECONDS },
                { target: 250, duration: STAGE_SECONDS },
                { target: 500, duration: STAGE_SECONDS },
                { target: 1000, duration: STAGE_SECONDS },
                { target: PEAK_RPS, duration: STAGE_SECONDS },
            ],
        },
    },
    thresholds: {
        // 한계 탐색이 목적이므로 abortOnFail 없이 통과/실패만 기록한다.
        http_req_duration: ['p(99)<500'],
        http_req_failed: ['rate<0.01'],
        dropped_iterations: ['rate<0.01'],
    },
};

// ============================================================
// setup() — 캐시 워밍업 (필수)
//   map_nearby: 좌표 그리드 + 두 delta 모드를 1회씩 호출해 Redis geo/hash 워밍
//   station_detail: stationId 범위를 샘플링해 1회씩 호출
// ============================================================
export function setup() {
    let warmCount = 0;

    if (ENDPOINT === 'station_detail') {
        // 범위가 넓으면 전수 호출은 과하므로 촘촘히 샘플링(최대 ~600개)
        const step = Math.max(1, Math.floor((STATION_ID_MAX - STATION_ID_MIN) / 600));
        for (let id = STATION_ID_MIN; id <= STATION_ID_MAX; id += step) {
            http.get(`${BASE_URL}/stations/${id}`);
            warmCount++;
        }
    } else {
        // 좌표를 격자(약 10x10)로 훑으며 radius/cluster 양쪽 delta 워밍
        const GRID = 10;
        for (let i = 0; i < GRID; i++) {
            for (let j = 0; j < GRID; j++) {
                const lat = LAT_MIN + ((LAT_MAX - LAT_MIN) * i) / (GRID - 1);
                const lng = LNG_MIN + ((LNG_MAX - LNG_MIN) * j) / (GRID - 1);
                const radD = (RADIUS_DELTA_MIN + RADIUS_DELTA_MAX) / 2;
                const cluD = (CLUSTER_DELTA_MIN + CLUSTER_DELTA_MAX) / 2;
                http.get(`${BASE_URL}/stations/map/nearby?latitude=${lat}&longitude=${lng}&latDelta=${radD}&lngDelta=${radD}`);
                http.get(`${BASE_URL}/stations/map/nearby?latitude=${lat}&longitude=${lng}&latDelta=${cluD}&lngDelta=${cluD}`);
                warmCount += 2;
            }
        }
    }

    console.log(`[setup] 캐시 워밍업 완료: ${warmCount}건 호출 (endpoint=${ENDPOINT}, mode=${MODE})`);
    return { warmedAt: new Date().toISOString() };
}

// ============================================================
// 기본 시나리오
// ============================================================
export default function () {
    const req = buildRequest();
    const res = http.get(req.url, { tags: { name: req.name } });
    check(res, {
        'status is 200': (r) => r.status === 200,
    });
}
