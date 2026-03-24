interface Props {
  durationSeconds: number
  delayMinMs: number
  delayMaxMs: number
  useAiContent: boolean
  onChange: (field: string, value: number | boolean) => void
  disabled: boolean
}

export default function GlobalSettings({
  durationSeconds, delayMinMs, delayMaxMs, useAiContent, onChange, disabled
}: Props) {
  return (
    <section className="card">
      <h2 className="card-title">글로벌 설정</h2>
      <div className="form-grid">
        <label>
          <span>시뮬레이션 시간 (초)</span>
          <input
            type="number" min={10} max={3600}
            value={durationSeconds}
            disabled={disabled}
            onChange={e => onChange('durationSeconds', Number(e.target.value))}
          />
        </label>
        <label>
          <span>최소 딜레이 (ms)</span>
          <input
            type="number" min={0} max={10000} step={100}
            value={delayMinMs}
            disabled={disabled}
            onChange={e => onChange('delayMinMs', Number(e.target.value))}
          />
        </label>
        <label>
          <span>최대 딜레이 (ms)</span>
          <input
            type="number" min={0} max={10000} step={100}
            value={delayMaxMs}
            disabled={disabled}
            onChange={e => onChange('delayMaxMs', Number(e.target.value))}
          />
        </label>
        <label className="toggle-label">
          <span>AI 콘텐츠 생성</span>
          <div className="toggle-wrap">
            <input
              type="checkbox"
              checked={useAiContent}
              disabled={disabled}
              onChange={e => onChange('useAiContent', e.target.checked)}
            />
            <span className="toggle-hint">
              {useAiContent ? 'Claude API 사용 (비용 발생)' : '고정 템플릿 사용 (무료)'}
            </span>
          </div>
        </label>
      </div>
    </section>
  )
}
