<template>
  <div class="language-switcher">
    <el-dropdown 
      trigger="click" 
      @command="handleLanguageChange"
      placement="bottom-end"
    >
      <el-button text class="language-btn">
        <el-icon class="language-icon">
          <Setting />
        </el-icon>
        <span class="language-text">{{ currentLanguageDisplay }}</span>
        <el-icon class="dropdown-icon">
          <ArrowDown />
        </el-icon>
      </el-button>
      
      <template #dropdown>
        <el-dropdown-menu class="language-dropdown">
          <div class="dropdown-header">
            <el-icon><Setting /></el-icon>
            <span>{{ $t('language.switch') }}</span>
          </div>
          
          <el-dropdown-item
            v-for="lang in supportedLanguages"
            :key="lang.code"
            :command="lang.code"
            :class="{ 'is-active': currentLanguage === lang.code }"
            class="language-item"
          >
            <div class="language-option">
              <div class="language-info">
                <span class="language-name">{{ lang.name }}</span>
                <span class="language-native">{{ lang.nativeName }}</span>
              </div>
              <el-icon v-if="currentLanguage === lang.code" class="check-icon">
                <Check />
              </el-icon>
            </div>
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useLanguageStore } from '@/stores/language'
import { ElMessage } from 'element-plus'
import {
  Setting,
  ArrowDown,
  Check
} from '@element-plus/icons-vue'

const { locale } = useI18n()
const languageStore = useLanguageStore()

// 计算属性
const currentLanguage = computed(() => languageStore.currentLanguage)

const currentLanguageDisplay = computed(() => {
  const lang = supportedLanguages.value.find(l => l.code === currentLanguage.value)
  return lang ? lang.nativeName : 'Language'
})

const supportedLanguages = computed(() => languageStore.supportedLanguages)

// 方法
const handleLanguageChange = (langCode) => {
  if (langCode === currentLanguage.value) {
    return
  }
  
  try {
    languageStore.setLanguage(langCode)
    
    // 显示切换成功消息
    const langName = supportedLanguages.value.find(l => l.code === langCode)?.name || langCode
    ElMessage.success({
      message: `${langName === '中文' ? '语言已切换为中文' : 'Language switched to English'}`,
      duration: 2000
    })
  } catch (error) {
    console.error('Language switch failed:', error)
    ElMessage.error('Language switch failed')
  }
}

// 组件挂载时初始化
onMounted(() => {
  // 确保语言store已初始化
  if (!languageStore.isInitialized) {
    languageStore.initializeLanguage()
  }
})
</script>

<style scoped>
.language-switcher {
  display: inline-block;
}

.language-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  border-radius: 6px;
  transition: all 0.2s ease;
  color: var(--el-text-color-primary);
  font-size: 14px;
}

.language-btn:hover {
  background-color: var(--el-fill-color-light);
  color: var(--el-color-primary);
}

.language-icon {
  font-size: 16px;
}

.language-text {
  font-weight: 500;
  white-space: nowrap;
}

.dropdown-icon {
  font-size: 12px;
  transition: transform 0.2s ease;
}

.language-btn:hover .dropdown-icon {
  transform: rotate(180deg);
}

/* 下拉菜单样式 */
.language-dropdown {
  min-width: 180px;
  padding: 8px 0;
}

.dropdown-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  font-size: 12px;
  font-weight: 600;
  color: var(--el-text-color-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin-bottom: 4px;
}

.language-item {
  padding: 0;
  margin: 2px 8px;
  border-radius: 4px;
  transition: all 0.2s ease;
}

.language-item:hover {
  background-color: var(--el-fill-color-light);
}

.language-item.is-active {
  background-color: var(--el-color-primary-light-9);
  color: var(--el-color-primary);
}

.language-option {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  width: 100%;
}

.language-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.language-name {
  font-size: 14px;
  font-weight: 500;
  line-height: 1.2;
}

.language-native {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  line-height: 1.2;
}

.check-icon {
  font-size: 14px;
  color: var(--el-color-primary);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .language-text {
    display: none;
  }
  
  .language-btn {
    padding: 8px;
  }
}

/* 深色模式适配 */
@media (prefers-color-scheme: dark) {
  .language-btn:hover {
    background-color: var(--el-fill-color-dark);
  }
  
  .language-item:hover {
    background-color: var(--el-fill-color-dark);
  }
  
  .language-item.is-active {
    background-color: var(--el-color-primary-dark-2);
  }
}

/* 动画效果 */
.language-dropdown {
  animation: fadeInDown 0.2s ease-out;
}

@keyframes fadeInDown {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 高对比度模式 */
@media (prefers-contrast: high) {
  .language-btn {
    border: 1px solid var(--el-border-color);
  }
  
  .language-item.is-active {
    border: 1px solid var(--el-color-primary);
  }
}
</style>
