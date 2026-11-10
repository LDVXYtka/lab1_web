<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from './stores/auth'
import { logoutUser } from './session'
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
    await logoutUser()
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

<style scoped>
header {
  padding: 1rem 3vw;
  border-bottom: 1px solid #ccc;
  background: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}

h1 {
  margin: 0;
  font-size: 1.5rem;
}

.user-panel {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

main {
  margin: 3vh auto;
  padding: 0 3vw;
}

@media (max-width: 600px) {
  main {
    margin: 20px auto;
    padding: 0 12px;
  }

  header {
    padding: 1rem 12px;
  }
}
</style>
