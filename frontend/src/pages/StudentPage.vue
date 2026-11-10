<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getStudent, deleteStudent } from '../api'
import { watchStudents } from '../studentEvents'
import MessageBox from '../components/MessageBox.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const student = ref(null)
const loading = ref(true)
const deleting = ref(false)
const error = ref('')
const controller = new AbortController()
let loadController
let stopEvents

async function load() {
  loadController?.abort()
  const current = new AbortController()
  loadController = current
  loading.value = true
  error.value = ''
  try {
    const data = await getStudent(route.params.id, current.signal)
    if (!current.signal.aborted) student.value = data
  } catch (failure) {
    if (!current.signal.aborted) {
      error.value = failure.message
      if (failure.status === 404) student.value = null
    }
  } finally {
    if (!current.signal.aborted) loading.value = false
  }
}

onMounted(() => {
  load()
  stopEvents = watchStudents(id => {
    if (id === undefined || id === Number(route.params.id)) load()
  })
})

onUnmounted(() => {
  controller.abort()
  loadController?.abort()
  stopEvents()
})

async function remove() {
  if (deleting.value || !window.confirm('Удалить студента ' + student.value.name + '?')) return
  deleting.value = true
  error.value = ''
  try {
    await deleteStudent(student.value.id, controller.signal)
    if (!controller.signal.aborted) await router.push('/')
  } catch (failure) {
    if (!controller.signal.aborted) error.value = failure.message
  } finally {
    deleting.value = false
  }
}
</script>

<template>
  <section class="page">
    <RouterLink to="/" class="button">Назад к списку</RouterLink>
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
        <RouterLink class="button" :to="'/students/' + student.id + '/edit'">Изменить</RouterLink>
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
