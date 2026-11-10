<script setup>
defineProps({
  students: { type: Array, required: true },
  isAdmin: { type: Boolean, default: false },
  busy: { type: Boolean, default: false }
})
defineEmits(['delete'])

function formatDate(date) {
  return date.split('-').reverse().join('.')
}
</script>

<template>
  <div class="table-scroll">
    <table>
      <thead>
        <tr>
          <th>ФИО</th><th>Группа</th><th>ИСУ</th><th>Общежитие</th>
          <th>Комната</th><th>Дата заселения</th><th>Иностранец</th><th>Действия</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="student in students" :key="student.id">
          <td>{{ student.name }}</td>
          <td>{{ student.group }}</td>
          <td>{{ student.isu }}</td>
          <td>{{ student.dormitory }}</td>
          <td>{{ student.room }}</td>
          <td>{{ formatDate(student.term) }}</td>
          <td>{{ student.international ? 'Да' : 'Нет' }}</td>
          <td class="actions">
            <RouterLink class="button" :to="'/students/' + student.id">Подробнее</RouterLink>
            <template v-if="isAdmin">
              <RouterLink class="button" :to="'/students/' + student.id + '/edit'">Изменить</RouterLink>
              <button type="button" :disabled="busy" @click="$emit('delete', student)">Удалить</button>
            </template>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>
