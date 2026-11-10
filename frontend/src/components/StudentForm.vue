<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { validateStudent } from '../validation'

const props = defineProps({
  student: { type: Object, default: null },
  busy: { type: Boolean, default: false },
  backendFields: { type: Object, default: () => ({}) }
})
const emit = defineEmits(['save'])
const form = reactive({ name: '', group: '', isu: '', dormitory: '', room: '', term: '', international: false, note: '' })
const errors = ref({})
const submitted = ref(false)
const fieldErrors = computed(() => ({ ...props.backendFields, ...errors.value }))
const maxTerm = new Date().getFullYear() + '-12-31'

watch(() => props.student, (student) => {
  if (student) {
    for (const field of Object.keys(form)) form[field] = student[field] ?? ''
  }
}, { immediate: true })

function getStudentData() {
  return {
    name: form.name.trim().replace(/\s+/g, ' '),
    group: form.group.trim().toUpperCase(),
    isu: form.isu.trim(),
    dormitory: form.dormitory === '' ? null : Number(form.dormitory),
    room: form.room.trim(),
    term: form.term,
    international: form.international,
    note: form.note.trim()
  }
}

watch(form, () => {
  if (submitted.value) errors.value = validateStudent(getStudentData())
})

function submit(event) {
  if (props.busy) return
  submitted.value = true
  const student = getStudentData()
  form.name = student.name
  form.group = student.group
  errors.value = validateStudent(student)
  for (const element of event.currentTarget.elements) {
    if (element.name && !element.checkValidity() && !errors.value[element.name]) {
      errors.value[element.name] = element.validationMessage
    }
  }
  const firstError = Object.keys(errors.value)[0]
  if (firstError) {
    event.currentTarget.elements.namedItem(firstError)?.focus()
    return
  }
  emit('save', student)
}
</script>

<template>
  <form novalidate @submit.prevent="submit">
    <fieldset :disabled="busy">
      <div class="form-element">
        <label for="name">ФИО*</label>
        <input id="name" v-model="form.name" name="name" type="text" placeholder="Иванов Иван Иванович" maxlength="100" required>
        <p v-if="fieldErrors.name" class="field-error">{{ fieldErrors.name }}</p>
      </div>
      <div class="form-row">
        <div class="form-element">
          <label for="group">Группа*</label>
          <input id="group" v-model="form.group" name="group" type="text" placeholder="M3301" maxlength="5" pattern="[A-Za-z][34][1-4][0-9]{2}" required>
          <p v-if="fieldErrors.group" class="field-error">{{ fieldErrors.group }}</p>
        </div>
        <div class="form-element">
          <label for="isu">ИСУ*</label>
          <input id="isu" v-model="form.isu" name="isu" type="text" placeholder="412345" inputmode="numeric" maxlength="6" pattern="[1-9][0-9]{5}" required>
          <p v-if="fieldErrors.isu" class="field-error">{{ fieldErrors.isu }}</p>
        </div>
      </div>
      <div class="form-row">
        <div class="form-element">
          <label for="dormitory">Номер общежития*</label>
          <input id="dormitory" v-model="form.dormitory" name="dormitory" type="number" placeholder="1" min="1" max="8" step="1" required>
          <p v-if="fieldErrors.dormitory" class="field-error">{{ fieldErrors.dormitory }}</p>
        </div>
        <div class="form-element">
          <label for="room">Комната*</label>
          <input id="room" v-model="form.room" name="room" type="text" placeholder="1203" inputmode="numeric" maxlength="4" pattern="(?:[1-9]|1[0-9]|20)(?:0[1-9]|[1-9][0-9])" required>
          <p v-if="fieldErrors.room" class="field-error">{{ fieldErrors.room }}</p>
        </div>
      </div>
      <div class="form-row">
        <div class="form-element">
          <label for="term">Дата заселения*</label>
          <input id="term" v-model="form.term" name="term" type="date" min="1970-01-01" :max="maxTerm" required>
          <p v-if="fieldErrors.term" class="field-error">{{ fieldErrors.term }}</p>
        </div>
        <div class="form-element">
          <label for="international">Иностранец</label>
          <input id="international" v-model="form.international" name="international" type="checkbox">
        </div>
      </div>
      <div class="form-element">
        <label for="note">Заметки</label>
        <textarea id="note" v-model="form.note" name="note" rows="5" maxlength="2000"></textarea>
        <p v-if="fieldErrors.note" class="field-error">{{ fieldErrors.note }}</p>
      </div>
      <button type="submit">{{ busy ? 'Сохранение...' : 'Сохранить' }}</button>
    </fieldset>
  </form>
</template>
