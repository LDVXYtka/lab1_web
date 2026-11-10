<script setup>
defineProps({
  text: { type: String, default: '' },
  success: { type: Boolean, default: false }
})
</script>

<template>
  <p v-if="text" class="message" :class="success ? 'success' : 'error'">{{ text }}</p>
</template>

<style scoped>
.message {
  padding: 0.7rem;
  border: 1px solid #aaa;
  border-radius: 0.2rem;
  background: #fff;
  overflow-wrap: anywhere;
}

.message.error {
  border-color: #a31f18;
  color: #a31f18;
}

.message.success {
  color: #333;
}
</style>
