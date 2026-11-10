let csrfToken = null
let csrfPromise = null
let refreshPromise = null
let sessionVersion = 0
let loggingOut = false
let sessionExpired = () => {}

export function onSessionExpired(handler) {
  sessionExpired = handler
}

export function resetCsrf() {
  csrfToken = null
}

export function resetSession() {
  sessionVersion++
  resetCsrf()
}

async function fetchResponse(url, options) {
  try {
    return await fetch(url, options)
  } catch (error) {
    if (error.name === 'AbortError' || options.signal?.aborted) {
      throw error
    }
    throw new Error('Сервер недоступен. Проверьте подключение и запуск приложения')
  }
}

async function readResponse(response) {
  if (response.status === 204) {
    return null
  }
  const text = await response.text()
  if (!text && response.ok) {
    return null
  }
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
  if (csrfToken) {
    return csrfToken
  }
  if (!csrfPromise) {
    csrfPromise = fetchResponse('/api/auth/csrf', { method: 'GET', cache: 'no-store' })
      .then(readResponse)
      .then(token => {
        csrfToken = token
        return token
      })
      .finally(() => {
        csrfPromise = null
      })
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
  return fetchResponse(url, options)
}

async function refreshSession() {
  if (!refreshPromise) {
    refreshPromise = send('POST', '/api/auth/refresh')
      .then(readResponse)
      .then(() => resetCsrf())
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

export async function request(method, url, data, options = {}) {
  const version = sessionVersion
  let response = await send(method, url, data, options.signal)
  if (response.status === 401 && options.auth !== false && version === sessionVersion && !loggingOut) {
    try {
      await refreshSession()
      options.signal?.throwIfAborted()
      if (version !== sessionVersion || loggingOut) return readResponse(response)
      response = await send(method, url, data, options.signal)
    } catch (error) {
      options.signal?.throwIfAborted()
      if (error.status === 401 && options.notifyExpired !== false && version === sessionVersion && !loggingOut) {
        sessionExpired()
      }
      throw error
    }
  }
  if (response.status === 401 && options.auth !== false && options.notifyExpired !== false && version === sessionVersion && !loggingOut) {
    sessionExpired()
  }
  return readResponse(response)
}

export async function logoutSession() {
  loggingOut = true
  resetSession()
  try {
    if (refreshPromise) await refreshPromise.catch(() => {})
    await request('POST', '/api/auth/logout', undefined, { auth: false })
  } finally {
    loggingOut = false
  }
}

export function getStudents(params, signal) {
  const filters = Object.keys(params).filter(name => !['page', 'size', 'sort', 'order'].includes(name))
  if (filters.length > 3) {
    const data = { ...params }
    for (const name of ['id', 'dormitory', 'page', 'size']) {
      if (data[name] !== undefined) {
        data[name] = Number(data[name])
      }
    }
    if (data.international !== undefined) {
      data.international = data.international === 'true'
    }
    return request('QUERY', '/api/requests', data, { signal })
  }
  return request('GET', '/api/requests?' + new URLSearchParams(params), undefined, { signal })
}

export function getStudent(id, signal) {
  return request('GET', '/api/requests/' + id, undefined, { signal })
}

export function saveStudent(id, student, signal) {
  if (id) {
    return request('PUT', '/api/requests/' + id, student, { signal })
  }
  return request('POST', '/api/requests', student, { signal })
}

export function deleteStudent(id, signal) {
  return request('DELETE', '/api/requests/' + id, undefined, { signal })
}
