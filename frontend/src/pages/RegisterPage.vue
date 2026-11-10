<script setup>
import { onUnmounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { registerUser } from '../session'
import { validateRegister } from '../validation'
import MessageBox from '../components/MessageBox.vue'

const router = useRouter()
const form = reactive({ username: '', email: '', password: '' })
const busy = ref(false)
const error = ref('')
const fields = ref({})
let active = true

onUnmounted(() => { active = false })

async function submit() {
  if (busy.value) return
  error.value = ''
  fields.value = validateRegister(form)
  if (Object.keys(fields.value).length > 0) return
  busy.value = true
  try {
    await registerUser({ username: form.username.trim(), email: form.email.trim(), password: form.password })
    if (active) await router.push({ path: '/login', query: { registered: '1' } })
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
    <form @submit.prevent="submit">
      <fieldset :disabled="busy">
        <div class="form-element">
          <label for="username">Имя пользователя</label>
          <input id="username" v-model="form.username" name="username" autocomplete="username" minlength="3" maxlength="32" pattern="[A-Za-z0-9_]{3,32}" required>
          <p class="hint">От 3 до 32 латинских букв, цифр или знаков _</p>
          <p v-if="fields.username" class="field-error">{{ fields.username }}</p>
        </div>
        <div class="form-element">
          <label for="email">Электронная почта</label>
          <input id="email" v-model="form.email" name="email" type="email" autocomplete="email" maxlength="254" required>
          <p v-if="fields.email" class="field-error">{{ fields.email }}</p>
        </div>
        <div class="form-element">
          <label for="password">Пароль</label>
          <input id="password" v-model="form.password" name="password" type="password" autocomplete="new-password" minlength="8" maxlength="72" required>
          <p class="hint">От 8 до 72 латинских букв, цифр или знаков, без пробелов</p>
          <p v-if="fields.password" class="field-error">{{ fields.password }}</p>
        </div>
        <button type="submit">{{ busy ? 'Регистрация...' : 'Зарегистрироваться' }}</button>
      </fieldset>
    </form>
    <p>Уже есть аккаунт? <RouterLink to="/login">Войти</RouterLink></p>
  </section>
</template>

<style scoped>
.auth-page {
  max-width: 30rem;
  margin: 0 auto;
}

.hint {
  margin: 0.3rem 0 0;
  color: #606060;
  font-size: 0.9rem;
}
</style>
