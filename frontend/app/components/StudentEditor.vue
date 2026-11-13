<script setup>
const props = defineProps({
  student: { type: Object, default: null },
  id: { type: String, default: '' }
})

const { $students } = useNuxtApp()
const router = useRouter()
const busy = ref(false)
const error = ref('')
const fields = ref({})
const controller = new AbortController()

onUnmounted(() => controller.abort())

async function save(value) {
  if (busy.value) return
  busy.value = true
  error.value = ''
  fields.value = {}
  try {
    const saved = await $students.saveStudent(props.id, value, controller.signal)
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
    <NuxtLink to="/" class="button">Назад к списку</NuxtLink>
    <MessageBox :text="error" />
    <StudentForm :student="student" :busy="busy" :backend-fields="fields" @save="save" />
  </section>
</template>

<style scoped>
.form-page {
  max-width: 65rem;
  margin: 0 auto;
}
</style>
