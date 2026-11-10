import { request, resetCsrf, resetSession, logoutSession } from './api'
import { useAuthStore } from './stores/auth'

let userVersion = 0

export function clearSession() {
  userVersion++
  resetSession()
  useAuthStore().clear()
}

export async function loadUser(force = false) {
  const auth = useAuthStore()
  if (auth.checked && !force) return auth.user
  const version = userVersion
  try {
    const user = await request('GET', '/api/auth/me', undefined, { notifyExpired: false })
    if (version === userVersion) auth.setUser(user)
  } catch (error) {
    if (version !== userVersion) return auth.user
    if (error.status !== 401) throw error
    clearSession()
  }
  return auth.user
}

export async function loginUser(username, password) {
  const version = ++userVersion
  resetSession()
  await request('POST', '/api/auth/login', { username, password }, { auth: false })
  resetCsrf()
  if (version === userVersion) await loadUser(true)
}

export async function registerUser(user) {
  await request('POST', '/api/auth/register', user, { auth: false })
  resetCsrf()
}

export async function logoutUser() {
  userVersion++
  await logoutSession()
  clearSession()
}
