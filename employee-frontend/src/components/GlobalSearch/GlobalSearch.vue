<template>
  <div class="global-search">
    <!-- 搜索触发按钮 -->
    <div class="search-trigger" @click="showSearchDialog = true">
      <el-icon class="search-icon"><Search /></el-icon>
      <span class="search-text">{{ $t('search.placeholder') }}</span>
      <span class="search-shortcut">{{ isMac ? '⌘K' : 'Ctrl+K' }}</span>
    </div>

    <!-- 搜索对话框 -->
    <el-dialog
      v-model="showSearchDialog"
      :title="$t('search.title')"
      width="600px"
      :show-close="false"
      :close-on-click-modal="true"
      :close-on-press-escape="true"
      class="search-dialog"
      @close="handleClose"
    >
      <div class="search-container">
        <!-- 搜索输入框 -->
        <div class="search-input-wrapper">
          <el-input
            ref="searchInputRef"
            v-model="searchQuery"
            :placeholder="$t('search.inputPlaceholder')"
            size="large"
            clearable
            @input="handleSearch"
            @keydown="handleKeydown"
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>

        <!-- 搜索结果 -->
        <div class="search-results" v-if="searchResults.length > 0">
          <div class="results-header">
            <span class="results-count">
              {{ $t('search.resultsCount', { count: searchResults.length }) }}
            </span>
          </div>
          
          <div class="results-list">
            <div
              v-for="(result, index) in searchResults"
              :key="result.id"
              :class="[
                'result-item',
                { 'active': selectedIndex === index }
              ]"
              @click="handleResultClick(result)"
              @mouseenter="selectedIndex = index"
            >
              <div class="result-icon">
                <el-icon>
                  <component :is="result.icon" />
                </el-icon>
              </div>
              <div class="result-content">
                <div class="result-title" v-html="result.highlightedTitle"></div>
                <div class="result-description">{{ result.description }}</div>
                <div class="result-path">{{ result.path }}</div>
              </div>
              <div class="result-category">
                <el-tag size="small" type="info">{{ result.categoryLabel }}</el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="search-empty" v-else-if="searchQuery && !isSearching">
          <el-empty :description="$t('search.noResults')" />
        </div>

        <!-- 搜索历史 -->
        <div class="search-history" v-else-if="searchHistory.length > 0">
          <div class="history-header">
            <span class="history-title">{{ $t('search.recentSearches') }}</span>
            <el-button text size="small" @click="clearHistory">
              {{ $t('search.clearHistory') }}
            </el-button>
          </div>
          <div class="history-list">
            <div
              v-for="(item, index) in searchHistory"
              :key="index"
              class="history-item"
              @click="handleHistoryClick(item)"
            >
              <el-icon><Clock /></el-icon>
              <span class="history-text">{{ item }}</span>
            </div>
          </div>
        </div>

        <!-- 快捷提示 -->
        <div class="search-tips" v-else>
          <div class="tips-title">{{ $t('search.quickTips') }}</div>
          <div class="tips-list">
            <div class="tip-item">
              <kbd>↑</kbd><kbd>↓</kbd> {{ $t('search.navigateTip') }}
            </div>
            <div class="tip-item">
              <kbd>Enter</kbd> {{ $t('search.selectTip') }}
            </div>
            <div class="tip-item">
              <kbd>Esc</kbd> {{ $t('search.closeTip') }}
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Search, Clock } from '@element-plus/icons-vue'
import { SearchEngine, debounce, formatSearchResult } from '@/utils/searchUtils'
import { employeeSearchConfig } from '@/config/searchConfig'

const router = useRouter()
const { t } = useI18n()

// 响应式数据
const showSearchDialog = ref(false)
const searchQuery = ref('')
const searchResults = ref([])
const selectedIndex = ref(0)
const isSearching = ref(false)
const searchHistory = ref([])
const searchInputRef = ref(null)

// 搜索引擎实例
const searchEngine = new SearchEngine(employeeSearchConfig)

// 计算属性
const isMac = computed(() => {
  return navigator.platform.toUpperCase().indexOf('MAC') >= 0
})

// 防抖搜索函数
const debouncedSearch = debounce((query) => {
  if (!query.trim()) {
    searchResults.value = []
    return
  }

  isSearching.value = true
  
  try {
    const results = searchEngine.search(query, {
      limit: 8,
      fuzzy: true,
      minScore: 0.1
    })
    
    searchResults.value = results.map(result => 
      formatSearchResult(result, query, t)
    )
  } catch (error) {
    console.error('Search error:', error)
    searchResults.value = []
  } finally {
    isSearching.value = false
    selectedIndex.value = 0
  }
}, 300)

// 方法
const handleSearch = (query) => {
  debouncedSearch(query)
}

const handleKeydown = (event) => {
  switch (event.key) {
    case 'ArrowDown':
      event.preventDefault()
      if (selectedIndex.value < searchResults.value.length - 1) {
        selectedIndex.value++
      }
      break
    case 'ArrowUp':
      event.preventDefault()
      if (selectedIndex.value > 0) {
        selectedIndex.value--
      }
      break
    case 'Enter':
      event.preventDefault()
      if (searchResults.value[selectedIndex.value]) {
        handleResultClick(searchResults.value[selectedIndex.value])
      }
      break
    case 'Escape':
      event.preventDefault()
      showSearchDialog.value = false
      break
  }
}

const handleResultClick = (result) => {
  // 添加到搜索历史
  addToHistory(searchQuery.value)
  
  // 跳转到目标页面
  router.push(result.path)
  
  // 关闭搜索对话框
  showSearchDialog.value = false
  
  // 清空搜索
  searchQuery.value = ''
  searchResults.value = []
}

const handleHistoryClick = (query) => {
  searchQuery.value = query
  handleSearch(query)
}

const addToHistory = (query) => {
  if (!query.trim()) return
  
  const history = searchHistory.value.filter(item => item !== query)
  history.unshift(query)
  searchHistory.value = history.slice(0, 10) // 最多保存10条历史
  
  // 保存到本地存储
  localStorage.setItem('employee-search-history', JSON.stringify(searchHistory.value))
}

const clearHistory = () => {
  searchHistory.value = []
  localStorage.removeItem('employee-search-history')
}

const handleClose = () => {
  searchQuery.value = ''
  searchResults.value = []
  selectedIndex.value = 0
}

// 键盘快捷键处理
const handleGlobalKeydown = (event) => {
  if ((event.ctrlKey || event.metaKey) && event.key === 'k') {
    event.preventDefault()
    showSearchDialog.value = true
  }
}

// 监听搜索对话框显示状态
watch(showSearchDialog, async (newVal) => {
  if (newVal) {
    await nextTick()
    searchInputRef.value?.focus()
  }
})

// 生命周期
onMounted(() => {
  // 绑定全局键盘事件
  document.addEventListener('keydown', handleGlobalKeydown)
  
  // 加载搜索历史
  const savedHistory = localStorage.getItem('employee-search-history')
  if (savedHistory) {
    try {
      searchHistory.value = JSON.parse(savedHistory)
    } catch (error) {
      console.error('Failed to load search history:', error)
    }
  }
})

onUnmounted(() => {
  // 移除全局键盘事件
  document.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<style scoped>
.global-search {
  position: relative;
}

.search-trigger {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  background: var(--el-bg-color-page);
  border: 1px solid var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 200px;
}

.search-trigger:hover {
  border-color: var(--el-color-primary);
  background: var(--el-color-primary-light-9);
}

.search-icon {
  color: var(--el-text-color-placeholder);
  margin-right: 8px;
}

.search-text {
  flex: 1;
  color: var(--el-text-color-placeholder);
  font-size: 14px;
}

.search-shortcut {
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  background: var(--el-bg-color);
  padding: 2px 6px;
  border-radius: 4px;
  border: 1px solid var(--el-border-color-lighter);
}

:deep(.search-dialog) {
  .el-dialog__header {
    padding: 16px 20px 0;
  }
  
  .el-dialog__body {
    padding: 16px 20px 20px;
  }
}

.search-container {
  min-height: 300px;
}

.search-input-wrapper {
  margin-bottom: 16px;
}

.search-input {
  font-size: 16px;
}

.search-results {
  max-height: 400px;
  overflow-y: auto;
}

.results-header {
  padding: 8px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  margin-bottom: 8px;
}

.results-count {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.result-item {
  display: flex;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.result-item:hover,
.result-item.active {
  background: var(--el-color-primary-light-9);
}

.result-icon {
  margin-right: 12px;
  color: var(--el-color-primary);
}

.result-content {
  flex: 1;
  min-width: 0;
}

.result-title {
  font-weight: 500;
  margin-bottom: 4px;
  color: var(--el-text-color-primary);
}

.result-title :deep(mark) {
  background: var(--el-color-warning-light-7);
  color: var(--el-color-warning-dark-2);
  padding: 0 2px;
  border-radius: 2px;
}

.result-description {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-path {
  font-size: 11px;
  color: var(--el-text-color-placeholder);
  font-family: monospace;
}

.result-category {
  margin-left: 12px;
}

.search-empty {
  padding: 40px 0;
  text-align: center;
}

.search-history {
  padding: 16px 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.history-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.history-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;
}

.history-item:hover {
  background: var(--el-bg-color-page);
}

.history-item .el-icon {
  margin-right: 8px;
  color: var(--el-text-color-placeholder);
}

.history-text {
  color: var(--el-text-color-regular);
}

.search-tips {
  padding: 20px 0;
  text-align: center;
}

.tips-title {
  font-size: 14px;
  color: var(--el-text-color-secondary);
  margin-bottom: 16px;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.tip-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
}

.tip-item kbd {
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color);
  border-radius: 3px;
  padding: 2px 6px;
  font-size: 11px;
  font-family: monospace;
}
</style>
