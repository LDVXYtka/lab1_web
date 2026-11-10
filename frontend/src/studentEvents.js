import { request } from './api'

export function watchStudents(onChange) {
  const controller = new AbortController()
  let events
  let retry

  function connect() {
    if (controller.signal.aborted) return
    events = new EventSource('/api/requests/events')
    events.addEventListener('ready', () => onChange())
    events.addEventListener('students-changed', event => onChange(Number(event.data)))
    events.onerror = () => {
      events.close()
      retry = setTimeout(reconnect, 1000)
    }
  }

  async function reconnect() {
    try {
      await request('GET', '/api/auth/me', undefined, { signal: controller.signal })
      connect()
    } catch {
      if (!controller.signal.aborted) retry = setTimeout(reconnect, 3000)
    }
  }

  connect()
  return () => {
    controller.abort()
    clearTimeout(retry)
    events.close()
  }
}
