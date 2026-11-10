<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import MessageBox from '../components/MessageBox.vue'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(true)
const error = ref('')
let active = true

onUnmounted(() => { active = false })

onMounted(async () => {
  try {
    await auth.loadUser(true)
    if (active && !auth.user) await router.replace('/login')
  } catch (failure) {
    error.value = failure.message
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="page profile-page">
    <MessageBox :text="error" />
    <p v-if="loading">Загрузка профиля...</p>
    <dl v-else-if="auth.user">
      <div><dt>ID</dt><dd>{{ auth.user.id }}</dd></div>
      <div><dt>Имя пользователя</dt><dd>{{ auth.user.username }}</dd></div>
      <div><dt>Электронная почта</dt><dd>{{ auth.user.email }}</dd></div>
      <div><dt>Роль</dt><dd>{{ auth.user.role }}</dd></div>
    </dl>
    <RouterLink to="/" class="button">Назад к списку</RouterLink>
  </section>
</template>
