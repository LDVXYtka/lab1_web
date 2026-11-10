import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from './stores/auth'
import StudentsPage from './pages/StudentsPage.vue'
import StudentPage from './pages/StudentPage.vue'
import StudentEditPage from './pages/StudentEditPage.vue'
import LoginPage from './pages/LoginPage.vue'
import RegisterPage from './pages/RegisterPage.vue'
import ProfilePage from './pages/ProfilePage.vue'
import NotFoundPage from './pages/NotFoundPage.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: StudentsPage, meta: { auth: true, title: 'Студенты' } },
    { path: '/students/new', component: StudentEditPage, meta: { auth: true, admin: true, title: 'Добавление студента' } },
    { path: '/students/:id/edit', component: StudentEditPage, meta: { auth: true, admin: true, title: 'Редактирование студента' } },
    { path: '/students/:id', component: StudentPage, meta: { auth: true, title: 'Карточка студента' } },
    { path: '/login', component: LoginPage, meta: { title: 'Вход' } },
    { path: '/register', component: RegisterPage, meta: { title: 'Регистрация' } },
    { path: '/profile', component: ProfilePage, meta: { auth: true, title: 'Профиль' } },
    { path: '/:pathMatch(.*)*', component: NotFoundPage, meta: { title: 'Страница не найдена' } }
  ]
})

router.beforeEach(async to => {
  const auth = useAuthStore()
  try {
    await auth.loadUser()
  } catch (error) {
    auth.error = error.message
    if (to.meta.auth) {
      return '/login'
    }
  }
  if (to.meta.auth && !auth.user) {
    return '/login'
  }
  if (to.meta.admin && !auth.isAdmin) {
    return '/'
  }
  if (auth.user && ['/login', '/register'].includes(to.path)) {
    return '/'
  }
  document.title = to.meta.title + ' | Общежитие'
})

export default router
