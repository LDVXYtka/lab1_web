export function createApi(fetchResponse, withAuthLock = action => action()) {
  let csrfToken = null
  let csrfPromise = null
  let csrfVersion = 0
  let refreshPromise = null
  let sessionVersion = 0
  let loggingOut = false
  let sessionExpired = () => {}

  function onSessionExpired(handler) {
    sessionExpired = handler
  }

  function resetCsrf() {
    csrfVersion++
    csrfToken = null
    csrfPromise = null
  }

  function resetSession() {
    sessionVersion++
    resetCsrf()
  }

  async function fetchResponseSafe(url, options) {
    try {
      return await fetchResponse(url, { cache: 'no-store', ...options })
    } catch (error) {
      if (error.name === 'AbortError' || options.signal?.aborted) throw error
      const failure = new Error('Сервер недоступен. Проверьте подключение и запуск приложения')
      failure.status = 503
      throw failure
    }
  }

  async function readResponse(response) {
    if (response.status === 204) return null
    const text = await response.text()
    if (!text && response.ok) return null
    let body
    try {
      body = JSON.parse(text)
    } catch {
      const error = new Error(response.status >= 500
        ? 'Ошибка сервера. Попробуйте повторить запрос позже'
        : 'Сервер вернул некорректный ответ')
      error.status = response.status
      throw error
    }
    if (!response.ok) {
      const error = new Error(body.message || 'Не удалось выполнить запрос')
      error.status = response.status
      error.fields = body.fields || {}
      throw error
    }
    return body
  }

  async function getCsrf() {
    if (csrfToken) return csrfToken
    if (!csrfPromise) {
      const version = csrfVersion
      const pending = fetchResponseSafe('/api/auth/csrf', { method: 'GET' })
        .then(readResponse)
        .then(token => {
          if (version === csrfVersion) csrfToken = token
          return token
        })
        .finally(() => {
          if (csrfPromise === pending) csrfPromise = null
        })
      csrfPromise = pending
    }
    return csrfPromise
  }

  async function send(method, url, data, signal) {
    const headers = {}
    if (!['GET', 'HEAD', 'OPTIONS'].includes(method)) {
      const csrf = await getCsrf()
      headers[csrf.headerName] = csrf.token
    }
    signal?.throwIfAborted()
    const options = { method, headers, signal }
    if (data !== undefined) {
      headers['Content-Type'] = 'application/json'
      options.body = JSON.stringify(data)
    }
    return fetchResponseSafe(url, options)
  }

  async function refreshSession(version) {
    if (!refreshPromise) {
      refreshPromise = withAuthLock(async () => {
        if (version !== sessionVersion || loggingOut) return
        const current = await send('GET', '/api/auth/me')
        if (current.status !== 401) {
          await readResponse(current)
        } else {
          const refreshed = await send('POST', '/api/auth/refresh')
          if (refreshed.status === 401) {
            await readResponse(await send('GET', '/api/auth/me'))
          } else {
            await readResponse(refreshed)
          }
        }
        resetCsrf()
      })
        .finally(() => { refreshPromise = null })
    }
    return refreshPromise
  }

  async function request(method, url, data, options = {}) {
    const version = sessionVersion
    try {
      let response = await send(method, url, data, options.signal)
      if (response.status === 401 && options.auth !== false && version === sessionVersion && !loggingOut) {
        await refreshSession(version)
        options.signal?.throwIfAborted()
        if (version === sessionVersion && !loggingOut) {
          response = await send(method, url, data, options.signal)
        }
      }
      return await readResponse(response)
    } catch (error) {
      options.signal?.throwIfAborted()
      if (error.status === 401 && options.auth !== false && options.notifyExpired !== false && version === sessionVersion && !loggingOut) {
        sessionExpired()
      }
      throw error
    }
  }

  async function waitForRefresh() {
    if (refreshPromise) await refreshPromise.catch(() => {})
  }

  async function logoutSession() {
    loggingOut = true
    resetSession()
    try {
      await waitForRefresh()
      await withAuthLock(() => request('POST', '/api/auth/logout', undefined, { auth: false }))
    } finally {
      loggingOut = false
    }
  }

  return { request, resetCsrf, resetSession, logoutSession, onSessionExpired, waitForRefresh, withAuthLock }
}
