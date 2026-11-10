import { Observable, Subject, timer, switchMap, map, catchError, of, merge, startWith } from 'rxjs'
import { getStudents } from './api'

function loadStudents(params) {
  return new Observable(subscriber => {
    const controller = new AbortController()
    getStudents(params, controller.signal)
      .then(data => {
        subscriber.next(data)
        subscriber.complete()
      })
      .catch(error => {
        if (!controller.signal.aborted) {
          subscriber.error(error)
        }
      })
    return () => controller.abort()
  })
}

export function createStudentSearch() {
  const changes = new Subject()
  const refresh = new Subject()
  const results = changes.pipe(
    switchMap(({ params, delay = 0 }) => timer(delay).pipe(
      switchMap(() => merge(of(null), refresh).pipe(
        switchMap(() => loadStudents(params).pipe(
          map(data => ({ data, loading: false, error: '' })),
          catchError(error => of({ data: null, loading: false, error: error.message })),
          startWith({ data: null, loading: true, error: '' })
        ))
      )),
      startWith({ data: null, loading: true, error: '' })
    ))
  )
  return { changes, refresh, results }
}
