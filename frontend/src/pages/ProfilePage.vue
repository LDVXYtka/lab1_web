<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { loadUser } from '../session'
import MessageBox from '../components/MessageBox.vue'

const auth = useAuthStore()
const router = useRouter()
const loading = ref(true)
const error = ref('')
let active = true

onUnmounted(() => { active = false })

onMounted(async () => {
  try {
    await loadUser(true)
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

<style scoped>
.profile-page {
  max-width: 65rem;
  margin: 0 auto;
}

dl {
  padding: 1.5rem;
  background: #fff;
  border: 1px solid #aaa;
  border-radius: 0.2rem;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1.5rem;
}

dt {
  margin-bottom: 0.3rem;
  color: #606060;
}

dd {
  margin: 0;
  overflow-wrap: anywhere;
}

@media (max-width: 600px) {
  dl {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
