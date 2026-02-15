import http from 'k6/http';
import { check, sleep } from 'k6';

// ============================================================
// 설정
// ============================================================
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const STATION_IDS = __ENV.STATION_IDS
    ? __ENV.STATION_IDS.split(',').map(Number)
    : [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
const VUS = Number(__ENV.VUS || 80);
const DURATION = __ENV.DURATION || '3m';
const INTERVAL = Number(__ENV.INTERVAL || 0.2);

// ============================================================
// 테스트 시나리오
// ============================================================
export const options = {
    summaryTrendStats: ['avg', 'min', 'max', 'p(90)', 'p(95)', 'p(99)'],
    scenarios: {
        load_test: {
            executor: 'constant-vus',
            vus: VUS,
            duration: DURATION,
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<3000', 'p(99)<5000'],
        http_req_failed: ['rate<0.02'],
    },
};

// ============================================================
// 테스트 실행
// ============================================================
export default function () {
    const stationId = STATION_IDS[Math.floor(Math.random() * STATION_IDS.length)];
    const url = `${BASE_URL}/stations/${stationId}`;

    const res = http.get(url, {
        headers: { 'Content-Type': 'application/json' },
        tags: { name: 'station_detail' },
    });

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response has data': (r) => {
            try {
                const body = JSON.parse(r.body);
                return body.code === 'SUCCESS' && body.data !== null;
            } catch (e) {
                return false;
            }
        },
    });

    sleep(INTERVAL);
}

// ============================================================
// 결과 출력
// ============================================================
export function handleSummary(data) {
    const duration = data.metrics.http_req_duration;
    if (!duration) {
        return { stdout: '\n❌ 테스트 결과가 없습니다. 서버 연결을 확인해주세요.\n' };
    }

    const vals = duration.values;
    const totalReqs = data.metrics.http_reqs ? data.metrics.http_reqs.values.count : 0;
    const failRate = data.metrics.http_req_failed ? data.metrics.http_req_failed.values.rate : 0;
    const failCount = Math.round(totalReqs * failRate);
    const successRate = ((1 - failRate) * 100).toFixed(2);

    const p90 = vals['p(90)'] ? vals['p(90)'].toFixed(2) : 'N/A';
    const p95 = vals['p(95)'] ? vals['p(95)'].toFixed(2) : 'N/A';
    const p99 = vals['p(99)'] ? vals['p(99)'].toFixed(2) : 'N/A';

    const vus = VUS;
    const dur = DURATION;
    const interval = INTERVAL;

    const p95Pass = vals['p(95)'] && vals['p(95)'] < 3000;
    const failPass = failRate < 0.02;

    const output = `
======================================================================
대여소 상세 조회 - 부하 테스트 결과
API: GET /stations/{stationId}
VUs: ${vus} | 시간: ${dur} | 간격: ${interval}s
======================================================================

📊 응답 시간 (ms)
  최소: ${vals.min.toFixed(2)}
  평균: ${vals.avg.toFixed(2)}
  최대: ${vals.max.toFixed(2)}
  p90:  ${p90}
  p95:  ${p95}
  p99:  ${p99}

📈 요청 통계
  총 요청: ${totalReqs}
  실패: ${failCount}
  성공률: ${successRate}%

✅ 목표 달성
  p95 < 3000ms: ${p95Pass ? '✓ 통과' : '✗ 실패'} (${p95}ms)
  실패율 < 2%:  ${failPass ? '✓ 통과' : '✗ 실패'} (${(failRate * 100).toFixed(2)}%)
`;

    return {
        stdout: output,
    };
}
