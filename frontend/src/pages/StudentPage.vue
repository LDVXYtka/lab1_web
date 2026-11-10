<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { getStudent, deleteStudent } from '../api'
import MessageBox from '../components/MessageBox.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const student = ref(null)
const loading = ref(true)
const deleting = ref(false)
const error = ref('')
const controller = new AbortController()

onMounted(async () => {
  try {
    student.value = await getStudent(route.params.id, controller.signal)
  } catch (failure) {
    if (!controller.signal.aborted) error.value = failure.message
  } finally {
    loading.value = false
  }
})

onUnmounted(() => controller.abort())

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
