import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import i18n from './i18n'
import App from './App.vue'
import 'element-plus/dist/index.css'
import './style/index.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(i18n)

// 初始化语言设置
app.mount('#app')

// 在应用挂载后初始化语言store
import { useLanguageStore } from '@/stores/language'
const languageStore = useLanguageStore()
languageStore.initializeLanguage()