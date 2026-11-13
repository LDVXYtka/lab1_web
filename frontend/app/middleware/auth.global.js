import { useAuthStore } from '~/stores/auth'

export default defineNuxtRouteMiddleware(async to => {
  const protectedPage = to.meta.auth
  const guestPage = to.meta.guest
  if (!protectedPage && !guestPage) return
  const { $session } = useNuxtApp()
  const auth = useAuthStore()
  try {
    await $session.loadUser()
  } catch (error) {
    throw createError({ statusCode: error.status || 503, message: error.message })
  }
  if (protectedPage && !auth.user) return navigateTo('/login', { replace: true })
  if (to.meta.admin && !auth.isAdmin) return navigateTo('/', { replace: true })
  if (guestPage && auth.user) return navigateTo('/', { replace: true })
})
