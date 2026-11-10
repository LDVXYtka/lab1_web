<script setup>
import { reactive, watch } from 'vue'

const emit = defineEmits(['change'])
const defaults = {
  name: '', group: '', isu: '', dormitory: '', room: '',
  term_from: '', term_to: '', international: '', note: '', id: '',
  sort: 'id', order: 'asc', size: '10'
}
const filters = reactive({ ...defaults })

watch(filters, () => emit('change', { ...filters }))

function reset() {
  Object.assign(filters, defaults)
}
</script>

<template>
  <form class="filters" @submit.prevent="emit('change', { ...filters })">
    <div class="form-element">
      <label for="filter-name">Поиск по ФИО</label>
      <input id="filter-name" v-model="filters.name" type="search" placeholder="Часть ФИО" maxlength="100">
    </div>
    <div class="form-element">
      <label for="filter-group">Группа</label>
      <input id="filter-group" v-model="filters.group" placeholder="M3301" maxlength="5">
    </div>
    <div class="form-element">
      <label for="filter-isu">ИСУ</label>
      <input id="filter-isu" v-model="filters.isu" placeholder="412345" inputmode="numeric" maxlength="6">
    </div>
    <div class="form-element">
      <label for="filter-dormitory">Общежитие</label>
      <select id="filter-dormitory" v-model="filters.dormitory">
        <option value="">Любое</option>
        <option v-for="number in 8" :key="number" :value="String(number)">{{ number }}</option>
      </select>
    </div>
    <div class="form-element">
      <label for="filter-room">Комната</label>
      <input id="filter-room" v-model="filters.room" placeholder="1203" inputmode="numeric" maxlength="4">
    </div>
    <div class="form-element">
      <label for="filter-term-from">Заселен с</label>
      <input id="filter-term-from" v-model="filters.term_from" type="date">
    </div>
    <div class="form-element">
      <label for="filter-term-to">Заселен по</label>
      <input id="filter-term-to" v-model="filters.term_to" type="date">
    </div>
    <div class="form-element">
      <label for="filter-international">Иностранец</label>
      <select id="filter-international" v-model="filters.international">
        <option value="">Все</option>
        <option value="true">Да</option>
        <option value="false">Нет</option>
      </select>
    </div>
    <div class="form-element">
      <label for="filter-note">Заметки</label>
      <input id="filter-note" v-model="filters.note" placeholder="Часть заметки" maxlength="2000">
    </div>
    <div class="form-element">
      <label for="filter-id">ID записи</label>
      <input id="filter-id" v-model="filters.id" type="number" min="1" step="1" placeholder="Любой">
    </div>
    <div class="form-element">
      <label for="filter-sort">Сортировать по</label>
      <select id="filter-sort" v-model="filters.sort">
        <option value="id">Порядку добавления</option>
        <option value="name">ФИО</option>
        <option value="group">Группе</option>
        <option value="isu">ИСУ</option>
        <option value="dormitory">Общежитию</option>
        <option value="room">Комнате</option>
        <option value="term">Дате заселения</option>
        <option value="international">Иностранному статусу</option>
        <option value="note">Заметкам</option>
      </select>
    </div>
    <div class="form-element">
      <label for="filter-order">Порядок</label>
      <select id="filter-order" v-model="filters.order">
        <option value="asc">По возрастанию</option>
        <option value="desc">По убыванию</option>
      </select>
    </div>
    <div class="form-element">
      <label for="filter-size">Строк на странице</label>
      <select id="filter-size" v-model="filters.size">
        <option value="5">5</option>
        <option value="10">10</option>
        <option value="20">20</option>
      </select>
    </div>
    <div class="filter-buttons">
      <button type="submit">Найти</button>
      <button type="button" @click="reset">Сбросить</button>
    </div>
  </form>
</template>
