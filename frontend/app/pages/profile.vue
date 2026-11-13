<script setup>
import { useAuthStore } from '~/stores/auth'

definePageMeta({ auth: true, title: 'Профиль' })

const auth = useAuthStore()
const { $session } = useNuxtApp()
const { error, status, refresh } = await useAsyncData('profile', async () => {
  try {
    const user = await $session.loadUser(import.meta.client)
    if (!user) throw createError({ statusCode: 401, message: 'Нужно войти в аккаунт' })
    return user
  } catch (failure) {
    throw createError({ statusCode: failure.statusCode || failure.status || 503, message: failure.message })
  }
})

if (error.value?.statusCode === 401) await navigateTo('/login', { replace: true })
if (import.meta.client) {
  watch(() => auth.user, user => {
    if (!user) navigateTo('/login', { replace: true })
  }, { immediate: true })
}
if (import.meta.server && error.value) setResponseStatus(useRequestEvent(), error.value.statusCode || 503)
</script>

<template>
  <section class="page profile-page">
    <MessageBox :text="error?.message" />
    <button v-if="error" type="button" @click="refresh">Повторить загрузку</button>
    <p v-if="status === 'pending'">Загрузка профиля...</p>
    <dl v-else-if="auth.user">
      <div><dt>ID</dt><dd>{{ auth.user.id }}</dd></div>
      <div><dt>Имя пользователя</dt><dd>{{ auth.user.username }}</dd></div>
      <div><dt>Электронная почта</dt><dd>{{ auth.user.email }}</dd></div>
      <div><dt>Роль</dt><dd>{{ auth.user.role }}</dd></div>
    </dl>
    <NuxtLink to="/" class="button">Назад к списку</NuxtLink>
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
