<script setup>
import { onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { validateLogin } from '../validation'
import MessageBox from '../components/MessageBox.vue'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const form = reactive({ username: '', password: '' })
const busy = ref(false)
const error = ref(auth.error)
const fields = ref({})
let active = true

onUnmounted(() => { active = false })
auth.error = ''

async function submit() {
  if (busy.value) return
  error.value = ''
  fields.value = validateLogin(form)
  if (Object.keys(fields.value).length > 0) return
  busy.value = true
  try {
    await auth.login(form.username.trim(), form.password)
    if (active) await router.replace('/')
  } catch (failure) {
    error.value = failure.message
    fields.value = failure.fields || {}
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="page auth-page">
    <MessageBox :text="error" />
    <MessageBox v-if="route.query.expired" text="Сессия истекла. Войдите снова" />
    <MessageBox v-if="route.query.registered" text="Регистрация завершена. Теперь можно войти" success />
    <form @submit.prevent="submit">
      <fieldset :disabled="busy">
        <div class="form-element">
          <label for="username">Имя пользователя</label>
          <input id="username" v-model="form.username" name="username" autocomplete="username" maxlength="32" required>
          <p v-if="fields.username" class="field-error">{{ fields.username }}</p>
        </div>
        <div class="form-element">
          <label for="password">Пароль</label>
          <input id="password" v-model="form.password" name="password" type="password" autocomplete="current-password" maxlength="72" required>
          <p v-if="fields.password" class="field-error">{{ fields.password }}</p>
        </div>
        <button type="submit">{{ busy ? 'Вход...' : 'Войти' }}</button>
      </fieldset>
    </form>
    <p>Нет аккаунта? <RouterLink to="/register">Зарегистрироваться</RouterLink></p>
  </section>
</template>
