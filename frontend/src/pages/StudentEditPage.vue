<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getStudent, saveStudent } from '../api'
import MessageBox from '../components/MessageBox.vue'
import StudentForm from '../components/StudentForm.vue'

const route = useRoute()
const router = useRouter()
const student = ref(null)
const loading = ref(Boolean(route.params.id))
const busy = ref(false)
const error = ref('')
const fields = ref({})
const controller = new AbortController()

onMounted(async () => {
  if (!route.params.id) return
  try {
    student.value = await getStudent(route.params.id, controller.signal)
  } catch (failure) {
    if (!controller.signal.aborted) error.value = failure.message
  } finally {
    loading.value = false
  }
})

onUnmounted(() => controller.abort())

async function save(value) {
  if (busy.value) return
  busy.value = true
  error.value = ''
  fields.value = {}
  try {
    const saved = await saveStudent(route.params.id, value, controller.signal)
    if (!controller.signal.aborted) await router.push('/students/' + saved.id)
  } catch (failure) {
    if (controller.signal.aborted) return
    error.value = failure.message
    fields.value = failure.fields || {}
    if (failure.status === 403) await router.replace('/')
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="page form-page">
    <RouterLink to="/" class="button">Назад к списку</RouterLink>
    <MessageBox :text="error" />
    <p v-if="loading">Загрузка студента...</p>
    <StudentForm v-else-if="!route.params.id || student" :student="student" :busy="busy" :backend-fields="fields" @save="save" />
  </section>
</template>

<style scoped>
.form-page {
  max-width: 65rem;
  margin: 0 auto;
}
</style>
