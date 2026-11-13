export function createStudents(api, cache, userId) {
  let version = 0

  function clear() {
    version++
    cache.value = {}
  }

  async function read(key, load, signal) {
    signal?.throwIfAborted()
    const fullKey = userId() + ':' + key
    const saved = cache.value[fullKey]
    if (saved && saved.expires > Date.now()) return saved.data
    const current = version
    const data = await load()
    signal?.throwIfAborted()
    if (current === version) {
      const keys = Object.keys(cache.value)
      if (keys.length >= 50) delete cache.value[keys[0]]
      cache.value[fullKey] = { data, expires: Date.now() + 30000 }
    }
    return data
  }

  function getStudents(params, signal) {
    const sorted = Object.fromEntries(Object.entries(params).sort(([a], [b]) => a.localeCompare(b)))
    return read('list:' + JSON.stringify(sorted), () => {
      const filters = Object.keys(params).filter(name => !['page', 'size', 'sort', 'order'].includes(name))
      const query = new URLSearchParams(params).toString()
      if (filters.length > 3 || query.length > 4000) {
        const data = { ...params }
        for (const name of ['id', 'dormitory', 'page', 'size']) {
          if (data[name] !== undefined) data[name] = Number(data[name])
        }
        if (data.international !== undefined) data.international = data.international === 'true'
        return api.request('QUERY', '/api/requests', data, { signal })
      }
      return api.request('GET', '/api/requests?' + query, undefined, { signal })
    }, signal)
  }

  function getStudent(id, signal) {
    return read('student:' + id, () => api.request('GET', '/api/requests/' + id, undefined, { signal }), signal)
  }

  async function saveStudent(id, student, signal) {
    const saved = await api.request(id ? 'PUT' : 'POST', '/api/requests' + (id ? '/' + id : ''), student, { signal })
    clear()
    return saved
  }

  async function deleteStudent(id, signal) {
    await api.request('DELETE', '/api/requests/' + id, undefined, { signal })
    clear()
  }

  return { clear, getStudents, getStudent, saveStudent, deleteStudent }
}
