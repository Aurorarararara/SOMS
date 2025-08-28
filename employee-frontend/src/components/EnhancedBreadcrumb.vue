<template>
  <div class="enhanced-breadcrumb">
    <!-- 主面包屑导航 -->
    <div class="breadcrumb-main">
      <el-breadcrumb separator="/" class="breadcrumb-nav">
        <el-breadcrumb-item 
          v-for="(item, index) in breadcrumbItems" 
          :key="item.path"
          :to="item.path"
          class="breadcrumb-item"
        >
          <div class="breadcrumb-content">
            <el-icon v-if="item.icon" class="breadcrumb-icon">
              <Clock v-if="item.icon === 'Clock'" />
              <Calendar v-else-if="item.icon === 'Calendar'" />
              <Bell v-else-if="item.icon === 'Bell'" />
              <User v-else-if="item.icon === 'User'" />
              <Edit v-else-if="item.icon === 'Edit'" />
              <List v-else-if="item.icon === 'List'" />
              <Odometer v-else-if="item.icon === 'Odometer'" />
              <House v-else-if="item.icon === 'House'" />
              <Document v-else />
            </el-icon>
            <span class="breadcrumb-text">{{ item.title }}</span>
          </div>
        </el-breadcrumb-item>
      </el-breadcrumb>

      <!-- 操作按钮组 -->
      <div class="breadcrumb-actions">
        <!-- 收藏按钮 -->
        <el-tooltip :content="isCurrentFavorite ? '取消收藏' : '收藏此页面'" placement="bottom">
          <el-button 
            text 
            :type="isCurrentFavorite ? 'warning' : 'default'"
            @click="toggleFavorite"
            class="action-btn"
          >
            <el-icon>
              <StarFilled v-if="isCurrentFavorite" />
              <Star v-else />
            </el-icon>
          </el-button>
        </el-tooltip>

        <!-- 历史记录按钮 -->
        <el-popover
          placement="bottom-end"
          :width="320"
          trigger="click"
        >
          <template #reference>
            <div>
              <el-tooltip content="访问历史" placement="bottom">
                <el-button text class="action-btn">
                  <el-icon><Clock /></el-icon>
                </el-button>
              </el-tooltip>
            </div>
          </template>
          
          <div class="history-panel">
            <div class="panel-header">
              <span class="panel-title">最近访问</span>
              <el-button text size="small" @click="clearHistory">清空</el-button>
            </div>
            
            <div class="history-list" v-if="recentRoutes.length > 0">
              <div 
                v-for="route in recentRoutes" 
                :key="route.path + route.timestamp"
                class="history-item"
                @click="navigateTo(route.path)"
              >
                <el-icon class="history-icon">
                  <Clock v-if="route.icon === 'Clock'" />
                  <Calendar v-else-if="route.icon === 'Calendar'" />
                  <Bell v-else-if="route.icon === 'Bell'" />
                  <User v-else-if="route.icon === 'User'" />
                  <Edit v-else-if="route.icon === 'Edit'" />
                  <List v-else-if="route.icon === 'List'" />
                  <Odometer v-else-if="route.icon === 'Odometer'" />
                  <Document v-else />
                </el-icon>
                <div class="history-content">
                  <div class="history-title">{{ route.title }}</div>
                  <div class="history-time">{{ formatTime(route.timestamp) }}</div>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <el-icon><DocumentRemove /></el-icon>
              <span>暂无访问记录</span>
            </div>
          </div>
        </el-popover>

        <!-- 收藏夹按钮 -->
        <el-popover
          placement="bottom-end"
          :width="320"
          trigger="click"
        >
          <template #reference>
            <div>
              <el-tooltip content="收藏夹" placement="bottom">
                <el-button text class="action-btn">
                  <el-icon><Collection /></el-icon>
                  <el-badge
                    v-if="favoriteRoutes.length > 0"
                    :value="favoriteRoutes.length"
                    :max="99"
                    class="favorite-badge"
                  />
                </el-button>
              </el-tooltip>
            </div>
          </template>
          
          <div class="favorites-panel">
            <div class="panel-header">
              <span class="panel-title">我的收藏</span>
              <div class="panel-actions">
                <el-button text size="small" @click="openFavoriteManager">管理</el-button>
                <el-button text size="small" @click="clearFavorites">清空</el-button>
              </div>
            </div>
            
            <div class="favorites-content" v-if="Object.keys(favoritesByCategory).length > 0">
              <div 
                v-for="(routes, category) in favoritesByCategory" 
                :key="category"
                class="favorite-category"
              >
                <div class="category-title">{{ category }}</div>
                <div class="category-items">
                  <div 
                    v-for="route in routes" 
                    :key="route.path"
                    class="favorite-item"
                  >
                    <div class="favorite-main" @click="navigateTo(route.path)">
                      <el-icon class="favorite-icon">
                        <Clock v-if="route.icon === 'Clock'" />
                        <Calendar v-else-if="route.icon === 'Calendar'" />
                        <Bell v-else-if="route.icon === 'Bell'" />
                        <User v-else-if="route.icon === 'User'" />
                        <Edit v-else-if="route.icon === 'Edit'" />
                        <List v-else-if="route.icon === 'List'" />
                        <Odometer v-else-if="route.icon === 'Odometer'" />
                        <Document v-else />
                      </el-icon>
                      <span class="favorite-title">{{ route.title }}</span>
                    </div>
                    <el-button 
                      text 
                      size="small" 
                      @click="removeFromFavorites(route.path)"
                      class="remove-btn"
                    >
                      <el-icon><Close /></el-icon>
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
            
            <div v-else class="empty-state">
              <el-icon><Star /></el-icon>
              <span>暂无收藏页面</span>
            </div>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- 收藏管理对话框 -->
    <FavoriteManager v-model="favoriteManagerVisible" />
  </div>
</template>

<script setup>
import { computed, watch, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useBreadcrumbStore } from '@/stores/breadcrumb'
import { ElMessage } from 'element-plus'
import FavoriteManager from './FavoriteManager.vue'
import {
  Star,
  StarFilled,
  Clock,
  Collection,
  Close,
  DocumentRemove,
  House,
  Odometer,
  Calendar,
  Bell,
  User,
  Edit,
  List,
  Plus,
  Document,
  Folder,
  Right,
  Delete,
  Download,
  Upload
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const breadcrumbStore = useBreadcrumbStore()

// 响应式数据
const favoriteManagerVisible = ref(false)

// 计算属性
const breadcrumbItems = computed(() => {
  return breadcrumbStore.generateBreadcrumb(route.path)
})

const isCurrentFavorite = computed(() => {
  return breadcrumbStore.isFavorite(route.path)
})

const recentRoutes = computed(() => {
  return breadcrumbStore.recentRoutes
})

const favoriteRoutes = computed(() => {
  return breadcrumbStore.favoriteRoutes
})

const favoritesByCategory = computed(() => {
  return breadcrumbStore.favoritesByCategory
})

// 方法
const toggleFavorite = () => {
  if (isCurrentFavorite.value) {
    const success = breadcrumbStore.removeFromFavorites(route.path)
    if (success) {
      ElMessage.success('已取消收藏')
    }
  } else {
    const success = breadcrumbStore.addToFavorites(route)
    if (success) {
      ElMessage.success('已添加到收藏')
    } else {
      ElMessage.warning('该页面已在收藏夹中')
    }
  }
}

const navigateTo = (path) => {
  if (path !== route.path) {
    router.push(path)
  }
}

const clearHistory = () => {
  breadcrumbStore.clearHistory()
  ElMessage.success('已清空访问历史')
}

const clearFavorites = () => {
  breadcrumbStore.clearFavorites()
  ElMessage.success('已清空收藏夹')
}

const openFavoriteManager = () => {
  favoriteManagerVisible.value = true
}

const removeFromFavorites = (path) => {
  const success = breadcrumbStore.removeFromFavorites(path)
  if (success) {
    ElMessage.success('已移除收藏')
  }
}

const formatTime = (timestamp) => {
  const now = Date.now()
  const diff = now - timestamp
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return new Date(timestamp).toLocaleDateString()
}

// 监听路由变化，添加到历史记录
watch(() => route.path, (newPath) => {
  if (newPath && newPath !== '/login') {
    breadcrumbStore.addToHistory(route)
  }
}, { immediate: true })

// 组件挂载时添加当前路由到历史记录
onMounted(() => {
  if (route.path && route.path !== '/login') {
    breadcrumbStore.addToHistory(route)
  }
})
</script>

<style scoped>
.enhanced-breadcrumb {
  display: flex;
  align-items: center;
  height: 100%;
}

.breadcrumb-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.breadcrumb-nav {
  flex: 1;
}

.breadcrumb-item {
  font-size: 14px;
  transition: all 0.2s ease;
}

.breadcrumb-item:hover {
  transform: translateY(-1px);
}

.breadcrumb-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.breadcrumb-icon {
  font-size: 14px;
}

.breadcrumb-text {
  font-weight: 500;
}

.breadcrumb-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-left: 16px;
}

.action-btn {
  padding: 8px;
  border-radius: 6px;
  transition: all 0.2s;
}

.action-btn:hover {
  background-color: var(--el-fill-color-light);
}

.favorite-badge {
  position: absolute;
  top: -2px;
  right: -2px;
}

/* 弹窗样式 */
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--el-border-color-light);
  margin-bottom: 12px;
}

.panel-title {
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.panel-actions {
  display: flex;
  gap: 8px;
}

/* 历史记录样式 */
.history-list {
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  display: flex;
  align-items: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.history-item:hover {
  background-color: var(--el-fill-color-light);
}

.history-icon {
  font-size: 16px;
  color: var(--el-color-primary);
  margin-right: 12px;
}

.history-content {
  flex: 1;
}

.history-title {
  font-size: 14px;
  color: var(--el-text-color-primary);
  margin-bottom: 2px;
}

.history-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

/* 收藏夹样式 */
.favorites-content {
  max-height: 400px;
  overflow-y: auto;
}

.favorite-category {
  margin-bottom: 16px;
}

.category-title {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  font-weight: 600;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.favorite-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.2s;
}

.favorite-item:hover {
  background-color: var(--el-fill-color-light);
}

.favorite-main {
  display: flex;
  align-items: center;
  flex: 1;
  cursor: pointer;
}

.favorite-icon {
  font-size: 16px;
  color: var(--el-color-primary);
  margin-right: 12px;
}

.favorite-title {
  font-size: 14px;
  color: var(--el-text-color-primary);
}

.remove-btn {
  opacity: 0;
  transition: opacity 0.2s;
}

.favorite-item:hover .remove-btn {
  opacity: 1;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 32px 16px;
  color: var(--el-text-color-secondary);
  font-size: 14px;
}

.empty-state .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
  opacity: 0.5;
}
</style>
