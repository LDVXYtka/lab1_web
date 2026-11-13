import { useAuthStore } from '~/stores/auth'
import { createStudents } from '~/utils/students'
import { watchStudents } from '~/utils/studentEvents'

export default defineNuxtPlugin(nuxtApp => {
  const auth = useAuthStore(nuxtApp.$pinia)
  const cache = useState('students-cache', () => ({}))
  const students = createStudents(nuxtApp.$api, cache, () => auth.user?.id ?? 'guest')
  const listeners = new Set()
  students.subscribe = callback => {
    listeners.add(callback)
    return () => listeners.delete(callback)
  }

  if (import.meta.client) {
    let stopEvents
    const stopWatch = watch(() => auth.user?.id, (id, previous) => {
      stopEvents?.()
      if (previous !== undefined) students.clear()
      if (id !== undefined) {
        stopEvents = watchStudents(nuxtApp.$api.request, changedId => {
          students.clear()
          for (const callback of listeners) callback(changedId)
        })
      }
    }, { immediate: true })
    nuxtApp.vueApp.onUnmount(() => {
      stopWatch()
      stopEvents?.()
      listeners.clear()
    })
  }

  return { provide: { students } }
})
