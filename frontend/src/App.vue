<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import MessageBox from './components/MessageBox.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const busy = ref(false)
const error = ref('')

async function logout() {
  if (busy.value) return
  busy.value = true
  error.value = ''
  try {
    await auth.logout()
    await router.replace('/login')
  } catch (failure) {
    error.value = failure.message
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <header>
    <h1>{{ route.meta.title || 'Студенты общежития' }}</h1>
    <nav v-if="auth.user" class="user-panel">
      <RouterLink to="/" class="button">Студенты</RouterLink>
      <RouterLink to="/profile">{{ auth.user.username }} ({{ auth.user.role }})</RouterLink>
      <button type="button" :disabled="busy" @click="logout">{{ busy ? 'Выход...' : 'Выйти' }}</button>
    </nav>
    <nav v-else class="user-panel">
      <RouterLink to="/login">Вход</RouterLink>
      <RouterLink to="/register">Регистрация</RouterLink>
    </nav>
  </header>
  <main>
    <MessageBox :text="error" />
    <p v-if="route.matched.length === 0">Загрузка приложения...</p>
    <RouterView v-else :key="route.path" />
  </main>
</template>
