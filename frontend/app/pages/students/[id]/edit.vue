<script setup>
import { useAuthStore } from '~/stores/auth'

definePageMeta({ auth: true, admin: true, title: 'Редактирование студента' })

const route = useRoute()
const auth = useAuthStore()
const { $students } = useNuxtApp()
const id = String(route.params.id)
if (!/^[1-9]\d*$/.test(id) || !Number.isSafeInteger(Number(id))) {
  throw createError({ statusCode: 404, message: 'Студент не найден' })
}
const { data: student, error } = await useAsyncData(
  'students:edit:' + auth.user.id + ':' + id,
  (_nuxtApp, { signal }) => $students.getStudent(id, signal)
)
if (error.value) {
  const statusCode = error.value.statusCode || error.value.status || 500
  throw createError({
    statusCode,
    message: statusCode === 404 ? 'Студент не найден' : 'Не удалось загрузить студента'
  })
}
</script>

<template>
  <StudentEditor :student="student" :id="id" />
</template>
