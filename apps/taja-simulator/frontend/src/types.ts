export const PERSONA_PRESETS = [
  { name: '출퇴근러', description: '매일 아침 따릉이로 출근. 짧고 실용적인 글 선호.' },
  { name: '자전거 동호인', description: '주말 장거리 라이딩. 코스 정보 공유.' },
  { name: '관광객', description: '서울 여행 중. 명소 근처 역 관심.' },
  { name: '동네 주민', description: '일상적인 단거리 이동. 편안한 말투.' },
] as const

export const ACTION_LABELS: Record<string, string> = {
  SIGNUP: '회원가입',
  SEARCH_STATION: '역 검색',
  VIEW_MAP: '지도 조회',
  JOIN_BOARD: '게시판 참여',
  CREATE_POST: '게시글 작성',
  CREATE_COMMENT: '댓글 작성',
  LIKE_POST: '좋아요',
  ADD_FAVORITE: '즐겨찾기 추가',
}

export const ALL_ACTIONS = Object.keys(ACTION_LABELS)

export interface UserConfig {
  personaName: string
  personaDescription: string
  actions: string[]
}

export interface SimulationRequest {
  delayMinMs: number
  delayMaxMs: number
  useAiContent: boolean
  users: UserConfig[]
}

export interface SimulationStatus {
  running: boolean
  totalUsers: number
  activeUsers: number
  completedActions: number
  successCount: number
  failureCount: number
}
