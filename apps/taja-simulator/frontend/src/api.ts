import { SimulationRequest, SimulationStatus } from './types'

const BASE_URL = 'http://localhost:8081/api/simulations'

export async function startSimulation(request: SimulationRequest): Promise<void> {
  const res = await fetch(`${BASE_URL}/start`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
  if (!res.ok) {
    const text = await res.text()
    throw new Error(text || '시뮬레이션 시작 실패')
  }
}

export async function stopSimulation(): Promise<void> {
  await fetch(`${BASE_URL}/stop`, { method: 'POST' })
}

export async function fetchStatus(): Promise<SimulationStatus> {
  const res = await fetch(`${BASE_URL}/status`)
  return res.json()
}

export function createLogEventSource(): EventSource {
  return new EventSource(`${BASE_URL}/logs`)
}
