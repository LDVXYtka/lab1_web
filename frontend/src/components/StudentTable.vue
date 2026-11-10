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

<style scoped>
.table-scroll {
  width: 100%;
  max-height: 65vh;
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
  border: 1px solid #ccc;
}

th,
td {
  padding: 0.7rem;
  border-right: 1px solid #d7d7d7;
  border-bottom: 1px solid #d7d7d7;
  text-align: left;
  vertical-align: top;
}

th {
  position: sticky;
  top: 0;
  background: #e5e5e5;
  white-space: nowrap;
}

th:first-child,
td:first-child {
  min-width: 15rem;
  max-width: 22rem;
  overflow-wrap: anywhere;
}

th:last-child,
td:last-child {
  border-right: 0;
  min-width: 13rem;
}

tbody tr:nth-child(even) {
  background: #eaeaea;
}

tbody tr:last-child td {
  border-bottom: 0;
}

td.actions {
  display: table-cell;
}

td.actions .button,
td.actions button {
  margin: 0 0.3rem 0.3rem 0;
}
</style>
