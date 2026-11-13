import { forwardAuth } from '../utils/auth'

export default defineNitroPlugin(app => {
  app.hooks.hook('request', event => {
    event.context.authOwner = { startedAt: performance.now(), response: event.node.res, entries: [] }
    event.context.forwardAuth = (operation, options) => forwardAuth(event, operation, options)
  })
})
