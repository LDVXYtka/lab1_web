import { createHash } from 'node:crypto'

const refreshes = new Map()

function refreshKey(url, token) {
  return createHash('sha256').update(url + ':' + token).digest('hex')
}

function createState() {
  return { active: true, current: null, queue: Promise.resolve(), owners: new Set(), keys: new Set() }
}

function run(state, action) {
  const pending = state.queue.then(action)
  state.queue = pending.catch(() => {})
  return pending
}

function track(state, owner) {
  clearTimeout(state.timer)
  if (state.owners.has(owner)) return
  state.owners.add(owner)
  function finished() {
    if (!state.owners.delete(owner) || state.owners.size) return
    state.timer = setTimeout(async () => {
      const pending = state.queue
      await pending
      if (state.owners.size || state.queue !== pending) return
      for (const key of state.keys) {
        if (refreshes.get(key)?.state === state) refreshes.delete(key)
      }
    }, 10000).unref()
  }
  owner.response.once('finish', finished)
  owner.response.once('close', finished)
  if (owner.response.writableEnded || owner.response.destroyed) finished()
}

export async function forwardAuth(event, operation, options) {
  if (!['login', 'logout', 'refresh'].includes(operation)) throw createError({ statusCode: 404 })
  const url = useRuntimeConfig(event).backendUrl
  const headers = Object.fromEntries(new Headers(options.headers))
  const cookie = headers.cookie || ''
  const token = cookie.split(';').map(part => part.trim()).find(part => part.startsWith('lab5RefreshToken='))?.slice('lab5RefreshToken='.length)
  const key = token ? refreshKey(url, token) : null
  const body = options.body
  const send = async action => {
    try {
      return await fetch(url + '/api/auth/' + action, { method: 'POST', headers, body })
    } catch {
      throw createError({ statusCode: 503, message: 'Сервер недоступен. Попробуйте повторить запрос позже' })
    }
  }
  let entry = key ? refreshes.get(key) : null
  const owner = event.context.authOwner
  if (!entry && key && operation !== 'refresh') {
    entry = { state: createState() }
    entry.state.keys.add(key)
    refreshes.set(key, entry)
  }

  if (operation === 'refresh' && key) {
    let previous
    if (entry && !entry.promise) {
      previous = entry
      entry = null
    } else if (entry && owner.startedAt > entry.finishedAt) {
      if (token === entry.token) {
        previous = entry
        entry = null
      }
      else return send(operation)
    }
    if (entry) {
      track(entry.state, owner)
      const checked = await send('csrf')
      if (checked.status !== 204) return checked
    } else {
      const state = previous?.state || createState()
      entry = { state, finishedAt: Infinity, token: null }
      state.keys.add(key)
      track(state, owner)
      function finished() {
        entry.promise.finally(() => {
          if (entry.finishedAt === Infinity) entry.finishedAt = performance.now()
        }).catch(() => {})
      }
      function failed() {
        if (refreshes.get(key) !== entry) return
        if (previous) refreshes.set(key, previous)
        else refreshes.delete(key)
      }
      entry.promise = run(state, async () => {
        if (!state.active) {
          headers.cookie = headers.cookie.replace(/(^|;\s*)lab5RefreshToken=[^;]*/, '$1lab5RefreshToken=')
        }
        const response = await send('refresh')
        entry.response = response
        const cookie = response.headers.getSetCookie().find(value => value.startsWith('lab5RefreshToken='))
        entry.token = cookie?.split(';', 1)[0].slice('lab5RefreshToken='.length)
        if (response.status === 204 && entry.token) {
          state.current = entry
          const nextKey = refreshKey(url, entry.token)
          state.keys.add(nextKey)
          refreshes.set(nextKey, entry)
        } else failed()
        return response
      }).catch(error => {
        failed()
        throw error
      })
      refreshes.set(key, entry)
      owner.response.once('finish', finished)
      owner.response.once('close', finished)
      if (owner.response.writableEnded || owner.response.destroyed) finished()
    }
    const response = await entry.promise
    if (response.status !== 204) return response.clone()
    owner.entries?.push(entry)
    const state = entry.state
    let pending
    do {
      pending = state.queue
      await pending
    } while (state.queue !== pending)
    if (state.active) return state.current.response.clone()
    headers.cookie = headers.cookie.replace(/(^|;\s*)lab5RefreshToken=[^;]*/, '$1lab5RefreshToken=')
    return send(operation)
  }

  if (entry) {
    const state = entry.state
    track(state, owner)
    if (operation === 'logout') {
      const checked = await send('csrf')
      if (checked.status !== 204) return checked
    }
    return run(state, async () => {
      if (operation === 'logout') {
        headers.cookie = headers.cookie.replace(/(^|;\s*)lab5RefreshToken=[^;]*/, '$1lab5RefreshToken=' + (state.current?.token || token))
      }
      const response = await send(operation)
      if (response.status === 204) state.active = false
      return response
    })
  }
  return send(operation)
}
