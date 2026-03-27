import { UserConfig, PERSONA_PRESETS, ACTION_LABELS, ALL_ACTIONS, ACTION_PREREQUISITES } from '../types'

interface Props {
  index: number
  user: UserConfig
  onChange: (user: UserConfig) => void
  onRemove: () => void
  disabled: boolean
}

export default function UserSlot({ index, user, onChange, onRemove, disabled }: Props) {
  const isCustom = !PERSONA_PRESETS.some(p => p.name === user.personaName)

  function selectPreset(name: string) {
    if (name === 'custom') {
      onChange({ ...user, personaName: '', personaDescription: '' })
    } else {
      const preset = PERSONA_PRESETS.find(p => p.name === name)
      if (preset) onChange({ ...user, personaName: preset.name, personaDescription: preset.description })
    }
  }

  function addAction(action: string) {
    const required = ACTION_PREREQUISITES[action] ?? []
    const current = user.actions
    const toAdd: string[] = []

    for (const prereq of required) {
      const accumulated = [...current, ...toAdd]
      const alreadyPresent = accumulated.includes(prereq)
      // SEARCH_STATION은 VIEW_MAP으로 대체 가능
      const stationSatisfied = prereq === 'SEARCH_STATION' && accumulated.includes('VIEW_MAP')
      if (!alreadyPresent && !stationSatisfied) {
        toAdd.push(prereq)
      }
    }

    onChange({ ...user, actions: [...current, ...toAdd, action] })
  }

  function removeAction(idx: number) {
    onChange({ ...user, actions: user.actions.filter((_, i) => i !== idx) })
  }

  return (
    <div className="user-slot">
      <div className="slot-header">
        <span className="slot-title">사용자 {index + 1}</span>
        <button className="btn-remove" onClick={onRemove} disabled={disabled}>✕</button>
      </div>

      <div className="slot-body">
        <label>
          <span>페르소나 선택</span>
          <select
            value={isCustom ? 'custom' : user.personaName}
            disabled={disabled}
            onChange={e => selectPreset(e.target.value)}
          >
            {PERSONA_PRESETS.map(p => (
              <option key={p.name} value={p.name}>{p.name}</option>
            ))}
            <option value="custom">직접 입력</option>
          </select>
        </label>

        {isCustom && (
          <>
            <label>
              <span>페르소나 이름</span>
              <input
                type="text"
                value={user.personaName}
                disabled={disabled}
                placeholder="예: 대학생"
                onChange={e => onChange({ ...user, personaName: e.target.value })}
              />
            </label>
            <label>
              <span>페르소나 설명</span>
              <input
                type="text"
                value={user.personaDescription}
                disabled={disabled}
                placeholder="예: 자전거 타며 학교 통학"
                onChange={e => onChange({ ...user, personaDescription: e.target.value })}
              />
            </label>
          </>
        )}

        {!isCustom && (
          <p className="persona-desc">{user.personaDescription}</p>
        )}

        <label>
          <span>사용자 수</span>
          <input
            type="number" min={1} max={100} step={1}
            value={user.count}
            disabled={disabled}
            onChange={e => onChange({ ...user, count: Number(e.target.value) })}
          />
        </label>

        <div className="action-section">
          <span>행동 시퀀스</span>
          <div className="action-sequence">
            {user.actions.map((action, i) => (
              <span key={i} className="action-chip">
                {ACTION_LABELS[action]}
                {!disabled && (
                  <button onClick={() => removeAction(i)}>✕</button>
                )}
              </span>
            ))}
            {user.actions.length === 0 && (
              <span className="empty-hint">행동을 추가하세요</span>
            )}
          </div>
          <div className="action-buttons">
            {ALL_ACTIONS.map(action => (
              <button
                key={action}
                className="btn-action"
                disabled={disabled}
                onClick={() => addAction(action)}
              >
                + {ACTION_LABELS[action]}
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
