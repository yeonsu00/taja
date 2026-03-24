import { useState, useEffect, useRef } from 'react'
import { UserConfig, SimulationStatus, PERSONA_PRESETS } from './types'
import { startSimulation, stopSimulation, fetchStatus, createLogEventSource } from './api'
import GlobalSettings from './components/GlobalSettings'
import UserSlot from './components/UserSlot'
import MonitoringPanel from './components/MonitoringPanel'

const DEFAULT_USER: UserConfig = {
  personaName: PERSONA_PRESETS[0].name,
  personaDescription: PERSONA_PRESETS[0].description,
  actions: ['SIGNUP', 'SEARCH_STATION', 'JOIN_BOARD', 'CREATE_POST'],
}

const DEFAULT_STATUS: SimulationStatus = {
  running: false,
  totalUsers: 0,
  activeUsers: 0,
  completedActions: 0,
  successCount: 0,
  failureCount: 0,
}

export default function App() {
  const [delayMinMs, setDelayMinMs] = useState(1000)
  const [delayMaxMs, setDelayMaxMs] = useState(5000)
  const [useAiContent, setUseAiContent] = useState(false)
  const [users, setUsers] = useState<UserConfig[]>([{ ...DEFAULT_USER }])
  const [status, setStatus] = useState<SimulationStatus>(DEFAULT_STATUS)
  const [logs, setLogs] = useState<string[]>([])
  const [error, setError] = useState<string | null>(null)

  const esRef = useRef<EventSource | null>(null)

  // 상태 폴링
  useEffect(() => {
    const poll = setInterval(async () => {
      try {
        const s = await fetchStatus()
        setStatus(s)
      } catch {
        // 서버 미실행 시 무시
      }
    }, 2000)
    return () => clearInterval(poll)
  }, [])

  // SSE 로그 스트림
  useEffect(() => {
    const es = createLogEventSource()
    esRef.current = es
    es.onmessage = (e) => {
      setLogs(prev => [...prev.slice(-199), e.data])
    }
    es.onerror = () => es.close()
    return () => es.close()
  }, [])

  function handleSettingChange(field: string, value: number | boolean) {
    if (field === 'delayMinMs') setDelayMinMs(value as number)
    if (field === 'delayMaxMs') setDelayMaxMs(value as number)
    if (field === 'useAiContent') setUseAiContent(value as boolean)
  }

  function addUser() {
    setUsers(prev => [...prev, { ...DEFAULT_USER }])
  }

  function updateUser(index: number, user: UserConfig) {
    setUsers(prev => prev.map((u, i) => i === index ? user : u))
  }

  function removeUser(index: number) {
    setUsers(prev => prev.filter((_, i) => i !== index))
  }

  async function handleStart() {
    setError(null)
    setLogs([])
    try {
      await startSimulation({ delayMinMs, delayMaxMs, useAiContent, users })
      const s = await fetchStatus()
      setStatus(s)
    } catch (e) {
      setError(e instanceof Error ? e.message : '오류가 발생했습니다.')
    }
  }

  async function handleStop() {
    await stopSimulation()
    const s = await fetchStatus()
    setStatus(s)
  }

  return (
    <div className="app">
      <header className="app-header">
        <h1>Taja Simulator</h1>
        <span className="header-sub">가상 사용자 시뮬레이션 관리자</span>
      </header>

      <main className="app-body">
        <div className="left-panel">
          <GlobalSettings
            delayMinMs={delayMinMs}
            delayMaxMs={delayMaxMs}
            useAiContent={useAiContent}
            onChange={handleSettingChange}
            disabled={status.running}
          />

          <section className="card">
            <div className="card-title-row">
              <h2 className="card-title">사용자 슬롯</h2>
              <button
                className="btn-add"
                onClick={addUser}
                disabled={status.running}
              >
                + 사용자 추가
              </button>
            </div>

            {users.length === 0 && (
              <p className="empty-hint">사용자를 추가해 주세요.</p>
            )}

            <div className="slots">
              {users.map((user, i) => (
                <UserSlot
                  key={i}
                  index={i}
                  user={user}
                  onChange={(u) => updateUser(i, u)}
                  onRemove={() => removeUser(i)}
                  disabled={status.running}
                />
              ))}
            </div>
          </section>
        </div>

        <div className="right-panel">
          <MonitoringPanel
            status={status}
            logs={logs}
            onStart={handleStart}
            onStop={handleStop}
            error={error}
          />
        </div>
      </main>
    </div>
  )
}
