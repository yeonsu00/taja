import { SimulationStatus } from '../types'

interface Props {
  status: SimulationStatus
  logs: string[]
  onStart: () => void
  onStop: () => void
  error: string | null
}

export default function MonitoringPanel({ status, logs, onStart, onStop, error }: Props) {
  const successRate = status.completedActions > 0
    ? Math.round((status.successCount / status.completedActions) * 100)
    : 0

  return (
    <section className="monitoring">
      <h2 className="card-title">모니터링</h2>

      <div className="status-grid">
        <div className="status-item">
          <span className="status-label">상태</span>
          <span className={`status-badge ${status.running ? 'running' : 'stopped'}`}>
            {status.running ? '● 실행 중' : '● 대기'}
          </span>
        </div>
        <div className="status-item">
          <span className="status-label">활성 사용자</span>
          <span className="status-value">{status.activeUsers} / {status.totalUsers}</span>
        </div>
        <div className="status-item">
          <span className="status-label">완료된 행동</span>
          <span className="status-value">{status.completedActions}</span>
        </div>
        <div className="status-item">
          <span className="status-label">성공</span>
          <span className="status-value success">{status.successCount}</span>
        </div>
        <div className="status-item">
          <span className="status-label">실패</span>
          <span className="status-value failure">{status.failureCount}</span>
        </div>
        <div className="status-item">
          <span className="status-label">성공률</span>
          <span className="status-value">{successRate}%</span>
        </div>
      </div>

      <div className="control-buttons">
        <button
          className="btn-start"
          onClick={onStart}
          disabled={status.running}
        >
          시작
        </button>
        <button
          className="btn-stop"
          onClick={onStop}
          disabled={!status.running}
        >
          중지
        </button>
      </div>

      {error && <p className="error-msg">{error}</p>}

      <div className="log-section">
        <div className="log-header">
          <span>실시간 로그</span>
          <span className="log-count">{logs.length}줄</span>
        </div>
        <div className="log-box">
          {logs.length === 0
            ? <span className="empty-hint">시뮬레이션을 시작하면 로그가 표시됩니다.</span>
            : [...logs].reverse().map((log, i) => (
              <div key={i} className="log-line">{log}</div>
            ))
          }
        </div>
      </div>
    </section>
  )
}
