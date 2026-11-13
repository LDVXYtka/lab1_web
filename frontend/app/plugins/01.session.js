import { appendResponseHeader } from 'h3'
import { useAuthStore } from '~/stores/auth'
import { createApi } from '~/utils/api'
import { createSession } from '~/utils/session'

export default defineNuxtPlugin(nuxtApp => {
  const auth = useAuthStore(nuxtApp.$pinia)
  const event = useRequestEvent()
  const config = useRuntimeConfig()
  const cookies = new Map()
  const authOwner = event?.context.authOwner

  if (import.meta.server) {
    for (const part of (event.node.req.headers.cookie || '').split(';')) {
      const index = part.indexOf('=')
      if (index > 0) cookies.set(part.slice(0, index).trim(), part.slice(index + 1).trim())
    }
  }

  async function fetchResponse(path, options) {
    if (import.meta.client) return fetch(path, options)
    const headers = new Headers(options.headers)
    headers.set('cookie', [...cookies].map(([name, value]) => name + '=' + value).join('; '))
    const response = path === '/api/auth/refresh'
      ? await event.context.forwardAuth('refresh', { ...options, headers })
      : await fetch(config.backendUrl + path, { ...options, headers })
    for (const cookie of response.headers.getSetCookie()) {
      appendResponseHeader(event, 'set-cookie', cookie)
      const pair = cookie.split(';', 1)[0]
      const index = pair.indexOf('=')
      const name = pair.slice(0, index)
      if (/max-age=0(?:;|$)/i.test(cookie)) cookies.delete(name)
      else cookies.set(name, pair.slice(index + 1))
    }
    return response
  }

  const withAuthLock = (action, signal) => {
    if (import.meta.client && navigator.locks) {
      return navigator.locks.request('lab5-session', { signal }, action)
    }
    return action()
  }
  const api = createApi(fetchResponse, withAuthLock)
  let notifyAuthChange = () => {}
  if (import.meta.client) {
    const key = 'lab5-session-change'
    const checkSession = event => {
      if (event?.type === 'storage' && event.key !== key) return
      try {
        const value = event?.type === 'storage' ? event.newValue : localStorage.getItem(key)
        const changedAt = Number(value?.split(':')[0])
        if (changedAt >= Math.floor(performance.timeOrigin)) window.location.reload()
      } catch {}
    }
    notifyAuthChange = () => {
      try {
        localStorage.setItem(key, Date.now() + ':' + Math.random())
      } catch {}
    }
    window.addEventListener('storage', checkSession)
    window.addEventListener('pageshow', checkSession)
    checkSession()
    nuxtApp.vueApp.onUnmount(() => {
      window.removeEventListener('storage', checkSession)
      window.removeEventListener('pageshow', checkSession)
    })
  }
  const session = createSession(api, auth, () => {
    nuxtApp.$students?.clear()
    if (import.meta.client) nuxtApp.runWithContext(() => clearNuxtData())
  }, notifyAuthChange)

  api.onSessionExpired(() => {
    session.clearSession()
    if (import.meta.client) {
      nuxtApp.runWithContext(() => navigateTo({ path: '/login', query: { expired: '1' } }, { replace: true }))
    }
  })

  if (import.meta.server) {
    nuxtApp.hook('app:rendered', async () => {
      let expired = false
      for (const original of authOwner.entries) {
        const state = original.state
        let pending
        do {
          pending = state.queue
          await pending
        } while (state.queue !== pending)
        const entry = state.current || original
        const response = entry.response
        if (!state.active) expired = true
        if (entry !== original && state.active && response?.status === 204) {
          const current = event.node.res.getHeader('set-cookie') || []
          const values = Array.isArray(current) ? current : [current]
          event.node.res.setHeader('set-cookie', [
            ...values.filter(value => !/^lab5(?:Access|Refresh)Token=/.test(value)),
            ...response.headers.getSetCookie()
          ])
        }
      }
      if (expired) {
        event.node.res.removeHeader('set-cookie')
        event.node.res.statusCode = 302
        event.node.res.setHeader('location', '/login')
      }
    })
  }

  return { provide: { api, session } }
})
