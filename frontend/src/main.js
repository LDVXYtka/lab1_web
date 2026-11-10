import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { onSessionExpired } from './api'
import { clearSession } from './session'
import './styles.css'

const app = createApp(App)
app.use(createPinia())
app.use(router)

onSessionExpired(() => {
  clearSession()
  router.replace({ path: '/login', query: { expired: '1' } })
})

app.mount('#app')
