<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import { deleteStudent } from '../api'
import { createStudentSearch } from '../studentSearch'
import { watchStudents } from '../studentEvents'
import MessageBox from '../components/MessageBox.vue'
import StudentFilters from '../components/StudentFilters.vue'
import StudentTable from '../components/StudentTable.vue'

const auth = useAuthStore()
const students = ref([])
const total = ref(0)
const page = ref(1)
const filters = ref({ sort: 'id', order: 'asc', size: '10' })
const loading = ref(true)
const deleting = ref(false)
const error = ref('')
const success = ref('')
const search = createStudentSearch()
const controller = new AbortController()
const pageCount = computed(() => Math.max(1, Math.ceil(total.value / Number(filters.value.size))))
let subscription
let stopEvents

function load(delay = 0) {
  const params = { page: page.value }
  for (const [name, value] of Object.entries(filters.value)) {
    const text = String(value).trim()
    if (text !== '') params[name] = text
  }
  search.changes.next({ params, delay })
}

function changeFilters(values) {
  filters.value = values
  page.value = 1
  load(3000)
}

function changePage(value) {
  page.value = value
  load()
}

async function remove(student) {
  if (deleting.value || !window.confirm('Удалить студента ' + student.name + '?')) return
  deleting.value = true
  error.value = ''
  success.value = ''
  try {
    await deleteStudent(student.id, controller.signal)
    if (controller.signal.aborted) return
    success.value = 'Студент удален'
    if (students.value.length === 1 && page.value > 1) page.value--
    load()
  } catch (failure) {
    if (!controller.signal.aborted) error.value = failure.message
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  subscription = search.results.subscribe(result => {
    loading.value = result.loading
    error.value = result.error
    if (result.data) {
      students.value = result.data.items
      total.value = result.data.total
      if (page.value > pageCount.value) {
        page.value = pageCount.value
        load()
      }
    }
  })
  load()
  stopEvents = watchStudents(() => search.refresh.next())
})

onUnmounted(() => {
  controller.abort()
  subscription.unsubscribe()
  search.changes.complete()
  search.refresh.complete()
  stopEvents()
})
</script>

<template>
  <section class="page">
    <MessageBox :text="error" />
    <MessageBox :text="success" success />
    <StudentFilters @change="changeFilters" />
    <div class="info-panel">
      <p>Студентов: {{ total }}</p>
      <RouterLink v-if="auth.isAdmin" to="/students/new" class="button">Добавить студента</RouterLink>
    </div>
    <p v-if="loading">Загрузка студентов...</p>
    <p v-if="deleting">Удаление студента...</p>
    <StudentTable :students="students" :is-admin="auth.isAdmin" :busy="deleting || loading" @delete="remove" />
    <p v-if="!loading && !error && students.length === 0">Студенты не найдены</p>
    <div class="pagination">
      <button type="button" :disabled="loading || page <= 1" @click="changePage(page - 1)">Назад</button>
      <span>Страница {{ page }} из {{ pageCount }}</span>
      <button type="button" :disabled="loading || page >= pageCount" @click="changePage(page + 1)">Вперед</button>
    </div>
  </section>
</template>

<style scoped>
.info-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  margin: 1rem 0;
}

.pagination {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 1rem;
  margin: 1rem 0;
}
</style>
