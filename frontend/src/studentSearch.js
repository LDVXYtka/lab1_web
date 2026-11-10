import { Observable, Subject, timer, switchMap, map, catchError, of, startWith } from 'rxjs'
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
  const results = changes.pipe(
    switchMap(params => timer(300).pipe(
      switchMap(() => loadStudents(params)),
      map(data => ({ data, loading: false, error: '' })),
      catchError(error => of({ data: null, loading: false, error: error.message })),
      startWith({ data: null, loading: true, error: '' })
    ))
  )
  return { changes, results }
}
