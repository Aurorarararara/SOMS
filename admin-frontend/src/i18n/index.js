import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

// 获取浏览器语言
const getBrowserLanguage = () => {
  const language = navigator.language || navigator.userLanguage
  if (language.startsWith('zh')) {
    return 'zh-CN'
  }
  return 'en-US'
}

// 获取存储的语言设置
const getStoredLanguage = () => {
  return localStorage.getItem('admin_language') || getBrowserLanguage()
}

// 创建i18n实例
const i18n = createI18n({
  legacy: false, // 使用 Composition API 模式
  locale: getStoredLanguage(), // 设置默认语言
  fallbackLocale: 'zh-CN', // 设置回退语言
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  },
  // 全局注入 $t 函数
  globalInjection: true,
  // 在生产环境中禁用警告
  silentTranslationWarn: process.env.NODE_ENV === 'production',
  // 缺失翻译时的处理
  missingWarn: process.env.NODE_ENV !== 'production',
  fallbackWarn: process.env.NODE_ENV !== 'production'
})

// 设置语言
export const setLanguage = (locale) => {
  i18n.global.locale.value = locale
  localStorage.setItem('admin_language', locale)
  
  // 设置HTML lang属性
  document.documentElement.lang = locale
  
  // 设置Element Plus语言
  setElementPlusLanguage(locale)
}

// 设置Element Plus语言
const setElementPlusLanguage = (locale) => {
  // 这里可以根据需要设置Element Plus的语言
  // Element Plus会自动根据i18n的locale设置语言
}

// 获取当前语言
export const getCurrentLanguage = () => {
  return i18n.global.locale.value
}

// 获取支持的语言列表
export const getSupportedLanguages = () => {
  return [
    { code: 'zh-CN', name: '中文', nativeName: '中文' },
    { code: 'en-US', name: 'English', nativeName: 'English' }
  ]
}

// 初始化时设置HTML lang属性
document.documentElement.lang = getStoredLanguage()

export default i18n
