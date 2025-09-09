import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { setLanguage as setI18nLanguage, getCurrentLanguage, getSupportedLanguages } from '@/i18n'

export const useLanguageStore = defineStore('language', () => {
  // 状态
  const currentLanguage = ref('zh-CN')
  const isInitialized = ref(false)
  const isLoading = ref(false)

  // 支持的语言列表
  const supportedLanguages = ref([
    { code: 'zh-CN', name: '中文', nativeName: '中文' },
    { code: 'en-US', name: 'English', nativeName: 'English' }
  ])

  // 计算属性
  const currentLanguageInfo = computed(() => {
    return supportedLanguages.value.find(lang => lang.code === currentLanguage.value) || supportedLanguages.value[0]
  })

  const isChineseLanguage = computed(() => {
    return currentLanguage.value.startsWith('zh')
  })

  const isEnglishLanguage = computed(() => {
    return currentLanguage.value.startsWith('en')
  })

  // 方法

  /**
   * 初始化语言设置
   */
  const initializeLanguage = () => {
    try {
      // 从本地存储获取语言设置
      const storedLanguage = localStorage.getItem('admin_language')
      
      if (storedLanguage && isSupportedLanguage(storedLanguage)) {
        currentLanguage.value = storedLanguage
      } else {
        // 使用浏览器语言或默认语言
        const browserLanguage = getBrowserLanguage()
        currentLanguage.value = browserLanguage
        saveLanguageToStorage(browserLanguage)
      }

      // 设置i18n语言
      setI18nLanguage(currentLanguage.value)
      
      isInitialized.value = true
    } catch (error) {
      console.error('Failed to initialize language:', error)
      // 使用默认语言
      currentLanguage.value = 'zh-CN'
      setI18nLanguage('zh-CN')
      isInitialized.value = true
    }
  }

  /**
   * 设置语言
   * @param {string} langCode - 语言代码
   */
  const setLanguage = async (langCode) => {
    if (!isSupportedLanguage(langCode)) {
      throw new Error(`Unsupported language: ${langCode}`)
    }

    if (currentLanguage.value === langCode) {
      return
    }

    try {
      isLoading.value = true

      // 更新当前语言
      currentLanguage.value = langCode

      // 设置i18n语言
      setI18nLanguage(langCode)

      // 保存到本地存储
      saveLanguageToStorage(langCode)

      // 设置HTML lang属性
      document.documentElement.lang = langCode

      // 触发语言变更事件
      emitLanguageChangeEvent(langCode)

    } catch (error) {
      console.error('Failed to set language:', error)
      throw error
    } finally {
      isLoading.value = false
    }
  }

  /**
   * 获取浏览器语言
   */
  const getBrowserLanguage = () => {
    const language = navigator.language || navigator.userLanguage
    
    // 检查是否支持浏览器语言
    if (isSupportedLanguage(language)) {
      return language
    }
    
    // 检查语言前缀
    const languagePrefix = language.split('-')[0]
    const matchedLanguage = supportedLanguages.value.find(lang => 
      lang.code.startsWith(languagePrefix)
    )
    
    return matchedLanguage ? matchedLanguage.code : 'zh-CN'
  }

  /**
   * 检查是否为支持的语言
   * @param {string} langCode - 语言代码
   */
  const isSupportedLanguage = (langCode) => {
    return supportedLanguages.value.some(lang => lang.code === langCode)
  }

  /**
   * 保存语言到本地存储
   * @param {string} langCode - 语言代码
   */
  const saveLanguageToStorage = (langCode) => {
    try {
      localStorage.setItem('admin_language', langCode)
    } catch (error) {
      console.error('Failed to save language to storage:', error)
    }
  }

  /**
   * 触发语言变更事件
   * @param {string} langCode - 语言代码
   */
  const emitLanguageChangeEvent = (langCode) => {
    try {
      const event = new CustomEvent('languageChanged', {
        detail: { language: langCode }
      })
      window.dispatchEvent(event)
    } catch (error) {
      console.error('Failed to emit language change event:', error)
    }
  }

  /**
   * 获取语言显示名称
   * @param {string} langCode - 语言代码
   */
  const getLanguageDisplayName = (langCode) => {
    const lang = supportedLanguages.value.find(l => l.code === langCode)
    return lang ? lang.name : langCode
  }

  /**
   * 获取语言本地名称
   * @param {string} langCode - 语言代码
   */
  const getLanguageNativeName = (langCode) => {
    const lang = supportedLanguages.value.find(l => l.code === langCode)
    return lang ? lang.nativeName : langCode
  }

  /**
   * 切换到下一个语言
   */
  const switchToNextLanguage = () => {
    const currentIndex = supportedLanguages.value.findIndex(lang => lang.code === currentLanguage.value)
    const nextIndex = (currentIndex + 1) % supportedLanguages.value.length
    const nextLanguage = supportedLanguages.value[nextIndex]
    setLanguage(nextLanguage.code)
  }

  /**
   * 重置语言设置
   */
  const resetLanguage = () => {
    const defaultLanguage = 'zh-CN'
    setLanguage(defaultLanguage)
  }

  /**
   * 清除语言存储
   */
  const clearLanguageStorage = () => {
    try {
      localStorage.removeItem('admin_language')
    } catch (error) {
      console.error('Failed to clear language storage:', error)
    }
  }

  /**
   * 添加支持的语言
   * @param {Object} languageInfo - 语言信息
   */
  const addSupportedLanguage = (languageInfo) => {
    if (!languageInfo.code || !languageInfo.name || !languageInfo.nativeName) {
      throw new Error('Invalid language info')
    }

    if (!isSupportedLanguage(languageInfo.code)) {
      supportedLanguages.value.push(languageInfo)
    }
  }

  /**
   * 移除支持的语言
   * @param {string} langCode - 语言代码
   */
  const removeSupportedLanguage = (langCode) => {
    const index = supportedLanguages.value.findIndex(lang => lang.code === langCode)
    if (index > -1 && supportedLanguages.value.length > 1) {
      supportedLanguages.value.splice(index, 1)
      
      // 如果移除的是当前语言，切换到第一个支持的语言
      if (currentLanguage.value === langCode) {
        setLanguage(supportedLanguages.value[0].code)
      }
    }
  }

  return {
    // 状态
    currentLanguage,
    isInitialized,
    isLoading,
    supportedLanguages,

    // 计算属性
    currentLanguageInfo,
    isChineseLanguage,
    isEnglishLanguage,

    // 方法
    initializeLanguage,
    setLanguage,
    getBrowserLanguage,
    isSupportedLanguage,
    getLanguageDisplayName,
    getLanguageNativeName,
    switchToNextLanguage,
    resetLanguage,
    clearLanguageStorage,
    addSupportedLanguage,
    removeSupportedLanguage
  }
})
