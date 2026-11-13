export function createSession(api, auth, clearStudents, onAuthChange = () => {}) {
  let userVersion = 0
  let userPromise = null
  let logoutPromise = null

  function clearSession() {
    userVersion++
    userPromise = null
    api.resetSession()
    auth.clear()
    clearStudents()
  }

  async function loadUser(force = false) {
    if (logoutPromise) await logoutPromise.catch(() => {})
    if (auth.checked && !force) return auth.user
    if (userPromise) return userPromise
    const version = userVersion
    const pending = (async () => {
      try {
        const user = await api.request('GET', '/api/auth/me', undefined, { notifyExpired: false })
        if (version === userVersion) auth.setUser(user)
      } catch (error) {
        if (version !== userVersion) return auth.user
        if (error.status !== 401) throw error
        clearSession()
      }
      return auth.user
    })().finally(() => {
      if (userPromise === pending) userPromise = null
    })
    userPromise = pending
    return pending
  }

  async function loginUser(username, password, signal) {
    const version = ++userVersion
    userPromise = null
    await api.waitForRefresh()
    await api.withAuthLock(async () => {
      signal?.throwIfAborted()
      if (version !== userVersion) return
      api.resetSession()
      clearStudents()
      await api.request('POST', '/api/auth/login', { username, password }, { auth: false, signal })
      auth.checked = false
      api.resetCsrf()
      onAuthChange()
    }, signal)
    signal?.throwIfAborted()
    if (version === userVersion) await loadUser(true)
  }

  async function registerUser(user) {
    await api.request('POST', '/api/auth/register', user, { auth: false })
    api.resetCsrf()
  }

  async function logoutUser() {
    userVersion++
    userPromise = null
    const pending = api.logoutSession()
    logoutPromise = pending
    try {
      await pending
      clearSession()
      onAuthChange()
    } finally {
      if (logoutPromise === pending) logoutPromise = null
    }
  }

  return { clearSession, loadUser, loginUser, registerUser, logoutUser }
}
