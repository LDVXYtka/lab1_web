export default defineNuxtConfig({
  ssr: true,
  devtools: { enabled: false },
  modules: ['@pinia/nuxt'],
  css: ['~/assets/styles.css'],
  runtimeConfig: {
    backendUrl: 'http://localhost:8080'
  },
  app: {
    head: {
      htmlAttrs: { lang: 'ru' },
      link: [{ rel: 'icon', href: '/favicon.ico' }]
    }
  },
  nitro: { preset: 'node-server' },
  routeRules: {
    '/**': { headers: { 'Cache-Control': 'private, no-store' } }
  }
})
