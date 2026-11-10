<script setup>
defineProps({
  text: { type: String, default: '' },
  success: { type: Boolean, default: false }
})
</script>

<template>
  <p v-if="text" class="message" :class="success ? 'success' : 'error'">{{ text }}</p>
</template>
