<script setup>
defineProps({ error: Object })
</script>

<template>
  <header>
    <h1>{{ error.statusCode === 404 ? 'Страница не найдена' : 'Не удалось открыть страницу' }}</h1>
  </header>
  <main>
    <section class="page">
      <p>{{ error.statusCode === 404 ? 'Такой страницы или студента нет' : error.message || 'Попробуйте повторить запрос позже' }}</p>
      <a href="/" class="button">На главную</a>
    </section>
  </main>
</template>

<style scoped>
header {
  padding: 1rem 3vw;
  border-bottom: 1px solid #ccc;
  background: #fff;
}

h1 {
  margin: 0;
  font-size: 1.5rem;
}

main {
  margin: 3vh auto;
  padding: 0 3vw;
}
</style>
