<script setup>
import { useAuthStore } from '~/stores/auth'

definePageMeta({ auth: true, title: 'Карточка студента' })

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const nuxtApp = useNuxtApp()
const { $students } = nuxtApp
const id = String(route.params.id)
if (!/^[1-9]\d*$/.test(id) || !Number.isSafeInteger(Number(id))) {
  throw createError({ statusCode: 404, message: 'Студент не найден' })
}
const { data: student, error: initialError } = await useAsyncData(
  'students:card:' + auth.user.id + ':' + id,
  (_nuxtApp, { signal }) => $students.getStudent(id, signal)
)
if (initialError.value) {
  const statusCode = initialError.value.statusCode || initialError.value.status || 500
  throw createError({
    statusCode,
    message: statusCode === 404 ? 'Студент не найден' : 'Не удалось загрузить студента'
  })
}

const loading = ref(false)
const deleting = ref(false)
const error = ref('')
const controller = new AbortController()
let loadController
let stopEvents
let pendingChange = false

async function load(message = '') {
  loadController?.abort()
  const current = new AbortController()
  loadController = current
  loading.value = true
  error.value = message
  try {
    const data = await $students.getStudent(id, current.signal)
    if (!current.signal.aborted) student.value = data
  } catch (failure) {
    if (!current.signal.aborted) {
      error.value = failure.message
      if (failure.status === 404) {
        student.value = null
        nuxtApp.runWithContext(() => showError({ statusCode: 404, message: 'Студент не найден' }))
      }
    }
  } finally {
    if (loadController === current) loading.value = false
  }
}

onMounted(() => {
  load()
  stopEvents = $students.subscribe(changedId => {
    if (changedId !== undefined && changedId !== Number(id)) return
    if (deleting.value) pendingChange = true
    else load()
  })
})

onUnmounted(() => {
  controller.abort()
  loadController?.abort()
  stopEvents?.()
})

async function remove() {
  if (deleting.value || !window.confirm('Удалить студента ' + student.value.name + '?')) return
  loadController?.abort()
  loading.value = false
  deleting.value = true
  pendingChange = false
  error.value = ''
  try {
    await $students.deleteStudent(student.value.id, controller.signal)
    if (!controller.signal.aborted) await router.push('/')
  } catch (failure) {
    if (!controller.signal.aborted) {
      error.value = failure.message
      deleting.value = false
      if (pendingChange || failure.status === 404) {
        pendingChange = false
        load(failure.message)
      }
    }
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <section class="page">
    <NuxtLink to="/" class="button">Назад к списку</NuxtLink>
    <MessageBox :text="error" />
    <p v-if="loading">Загрузка карточки...</p>
    <template v-if="student">
      <dl>
        <div><dt>ID записи</dt><dd>{{ student.id }}</dd></div>
        <div><dt>ФИО</dt><dd>{{ student.name }}</dd></div>
        <div><dt>Группа</dt><dd>{{ student.group }}</dd></div>
        <div><dt>ИСУ</dt><dd>{{ student.isu }}</dd></div>
        <div><dt>Общежитие</dt><dd>{{ student.dormitory }}</dd></div>
        <div><dt>Комната</dt><dd>{{ student.room }}</dd></div>
        <div><dt>Дата заселения</dt><dd>{{ student.term.split('-').reverse().join('.') }}</dd></div>
        <div><dt>Иностранец</dt><dd>{{ student.international ? 'Да' : 'Нет' }}</dd></div>
        <div class="wide"><dt>Заметки</dt><dd class="notes">{{ student.note || 'Нет заметок' }}</dd></div>
      </dl>
      <div v-if="auth.isAdmin" class="actions">
        <NuxtLink class="button" :to="'/students/' + student.id + '/edit'">Изменить</NuxtLink>
        <button type="button" :disabled="deleting" @click="remove">{{ deleting ? 'Удаление...' : 'Удалить' }}</button>
      </div>
    </template>
  </section>
</template>

<style scoped>
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

.wide {
  grid-column: 1 / -1;
}

.notes {
  white-space: pre-wrap;
}

.actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.5rem;
}

@media (max-width: 600px) {
  dl {
    grid-template-columns: minmax(0, 1fr);
  }
}
</style>
