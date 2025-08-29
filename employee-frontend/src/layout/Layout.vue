<template>
  <div class="employee-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'collapsed': isCollapse }">
      <div class="sidebar-header">
        <div class="logo" v-if="!isCollapse">
          <el-icon size="32" color="#667eea"><OfficeBuilding /></el-icon>
          <span class="logo-text">智慧办公</span>
        </div>
        <div class="logo-mini" v-else>
          <el-icon size="28" color="#667eea"><OfficeBuilding /></el-icon>
        </div>
      </div>
      
      <div class="sidebar-menu">
        <el-menu
          :default-active="$route.path"
          :collapse="isCollapse"
          :unique-opened="true"
          router
          class="employee-menu"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <template #title>{{ $t('nav.dashboard') }}</template>
          </el-menu-item>

          <el-sub-menu index="/attendance">
            <template #title>
              <el-icon><Clock /></el-icon>
              <span>{{ $t('nav.attendance') }}</span>
            </template>
            <el-menu-item index="/attendance">{{ $t('nav.attendance') }}</el-menu-item>
            <el-menu-item index="/attendance/records">{{ $t('nav.attendanceRecords') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/leave">
            <template #title>
              <el-icon><Calendar /></el-icon>
              <span>{{ $t('nav.leave') }}</span>
            </template>
            <el-menu-item index="/leave">{{ $t('nav.leave') }}</el-menu-item>
            <el-menu-item index="/leave/records">{{ $t('nav.leaveRecords') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/collaborative">
            <template #title>
              <el-icon><EditPen /></el-icon>
              <span>{{ $t('nav.collaborative') }}</span>
            </template>
            <el-menu-item index="/collaborative">{{ $t('nav.collaborative') }}</el-menu-item>
            <el-menu-item index="/collaborative/richtext">{{ $t('nav.richtext') }}</el-menu-item>
            <el-menu-item index="/collaborative/markdown">{{ $t('nav.markdown') }}</el-menu-item>
            <el-menu-item index="/collaborative/code">{{ $t('nav.code') }}</el-menu-item>
            <el-menu-item index="/collaborative/table">{{ $t('nav.table') }}</el-menu-item>
            <el-menu-item index="/collaborative/demo">{{ $t('nav.collaborativeDemo') }}</el-menu-item>
          </el-sub-menu>

          <el-sub-menu index="/tasks">
            <template #title>
              <el-icon><Notebook /></el-icon>
              <span>{{ $t('nav.tasks') }}</span>
            </template>
            <el-menu-item index="/tasks">{{ $t('nav.tasks') }}</el-menu-item>
            <el-menu-item index="/tasks/create">{{ $t('nav.createTask') }}</el-menu-item>
          </el-sub-menu>

          <el-menu-item index="/schedule">
            <el-icon><Calendar /></el-icon>
            <template #title>{{ $t('nav.schedule') }}</template>
          </el-menu-item>

          <el-menu-item index="/notifications">
            <el-icon><Bell /></el-icon>
            <template #title>{{ $t('nav.notifications') }}</template>
          </el-menu-item>

          <el-menu-item index="/profile">
            <el-icon><User /></el-icon>
            <template #title>{{ $t('nav.profile') }}</template>
          </el-menu-item>
        </el-menu>
      </div>
    </div>

    
    <!-- 主要内容区域 -->
    <div class="main-container">
      <!-- 顶部导航 -->
      <div class="header">
        <div class="header-left">
          <el-button 
            text 
            @click="toggleCollapse"
            class="sidebar-toggle"
          >
            <el-icon size="20">
              <Expand v-if="isCollapse" />
              <Fold v-else />
            </el-icon>
          </el-button>
          
          <EnhancedBreadcrumb />
        </div>
        
        <div class="header-right">
          <!-- 快捷操作 -->
          <div class="header-actions">
            <el-tooltip content="全屏切换" placement="bottom">
              <el-button text @click="toggleFullscreen">
                <el-icon size="18"><FullScreen /></el-icon>
              </el-button>
            </el-tooltip>

            <el-tooltip content="刷新页面" placement="bottom">
              <el-button text @click="refreshPage">
                <el-icon size="18"><Refresh /></el-icon>
              </el-button>
            </el-tooltip>

            <!-- 消息通知 -->
            <el-popover
              placement="bottom"
              :width="300"
              trigger="click"
            >
              <template #reference>
                <el-badge :value="unreadCount" :hidden="unreadCount === 0">
                  <el-button text class="notification-btn">
                    <el-icon size="18"><Bell /></el-icon>
                  </el-button>
                </el-badge>
              </template>
              <div class="notification-panel">
                <div class="notification-header">
                  <span>通知消息</span>
                  <el-button text size="small" @click="markAllRead">全部已读</el-button>
                </div>
                <el-scrollbar max-height="200px">
                  <div v-if="notifications.length === 0" class="no-notification">
                    暂无新消息
                  </div>
                  <div
                    v-for="notification in notifications"
                    :key="notification.id"
                    class="notification-item"
                    :class="{ 'is-read': notification.isRead }"
                  >
                    <div class="notification-content">
                      <p class="notification-title">{{ notification.title }}</p>
                      <p class="notification-time">{{ notification.time }}</p>
                    </div>
                  </div>
                </el-scrollbar>
              </div>
            </el-popover>

            <!-- 语言切换 -->
            <LanguageSwitcher />
          </div>

          <!-- 用户信息 -->
          <el-dropdown class="user-dropdown" @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="36">
                <el-icon><User /></el-icon>
              </el-avatar>
              <div class="user-details" v-if="!isCollapse">
                <div class="user-name">{{ userInfo.realName }}</div>
                <div class="user-role">{{ $t('user.employee') }}</div>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  {{ $t('user.profile') }}
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  {{ $t('user.settings') }}
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  {{ $t('user.logout') }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 主要内容 -->
      <div class="content">
        <router-view v-slot="{ Component, route }">
          <transition name="page-transition" mode="out-in" appear>
            <keep-alive :include="['Dashboard', 'Profile', 'Attendance']">
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  OfficeBuilding, Odometer, Clock, Timer, Document, Calendar, EditPen,
  Notebook, Bell, User, Expand, Fold, FullScreen, Refresh, ArrowDown,
  Setting, SwitchButton
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useDebounce, useThrottle } from '@/utils/debounce'
import EnhancedBreadcrumb from '@/components/EnhancedBreadcrumb.vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'

const route = useRoute()
const router = useRouter()
const { t: $t } = useI18n()
const userStore = useUserStore()

const isCollapse = ref(false)
const unreadCount = ref(5)

const userInfo = computed(() => userStore.userInfo)

// 原面包屑导航逻辑已移至 EnhancedBreadcrumb 组件

// 模拟通知数据
const notifications = ref([
  { id: 1, title: '系统维护通知', time: '2小时前', isRead: false },
  { id: 2, title: '请假申请已通过', time: '1天前', isRead: false },
  { id: 3, title: '新公告发布', time: '2天前', isRead: true },
])

const toggleCollapse = useDebounce(() => {
  isCollapse.value = !isCollapse.value
}, 150)

const toggleFullscreen = useDebounce(() => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}, 300)

const refreshPage = useDebounce(() => {
  window.location.reload()
}, 1000)

const markAllRead = useDebounce(() => {
  notifications.value.forEach(item => {
    item.isRead = true
  })
  unreadCount.value = 0
  ElMessage.success('已标记全部为已读')
}, 300)

const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'settings':
      ElMessage.info('设置功能开发中...')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        
        await userStore.logoutAction()
        ElMessage.success('已退出登录')
        router.push('/login')
      } catch (error) {
        if (error !== 'cancel') {
          console.error('退出登录失败:', error)
        }
      }
      break
  }
}

// 监听路由变化更新未读消息数量
watch(() => route.path, () => {
  // 这里可以根据路由变化加载对应的通知
})

onMounted(async () => {
  // 获取用户信息
  if (!userInfo.value.id) {
    try {
      await userStore.getUserInfoAction()
    } catch (error) {
      console.error('获取用户信息失败:', error)
      router.push('/login')
    }
  }
})
</script>

<style scoped>
.employee-layout {
  display: flex;
  min-height: 100vh;
  background: #f5f7fa;
}

/* 侧边栏 */
.sidebar {
  width: 260px;
  background: white;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: all 0.3s ease;
  position: relative;
  z-index: 100;
}

.sidebar.collapsed {
  width: 68px;
}

.sidebar-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-text {
  font-size: 20px;
  font-weight: 600;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.logo-mini {
  display: flex;
  align-items: center;
  justify-content: center;
}

.sidebar-menu {
  height: calc(100vh - 64px);
  overflow-y: auto;
}

.employee-menu {
  border: none;
  padding: 12px 0;
}

:deep(.employee-menu .el-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
}

:deep(.employee-menu .el-sub-menu) {
  margin: 4px 12px;
}

:deep(.employee-menu .el-sub-menu .el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  border-radius: 8px;
}

:deep(.employee-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

:deep(.employee-menu .el-menu-item:hover) {
  background: #f0f2ff;
  color: #667eea;
}

/* 主要容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

/* 顶部导航 */
.header {
  height: 64px;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  z-index: 50;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.sidebar-toggle {
  padding: 8px;
  border-radius: 6px;
}

.sidebar-toggle:hover {
  background: #f0f2ff;
  color: #667eea;
}

.breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-actions .el-button {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-actions .el-button:hover {
  background: #f0f2ff;
  color: #667eea;
}

.notification-btn {
  position: relative;
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-btn:hover {
  background: #f0f2ff;
  color: #667eea;
}

/* 用户信息 */
.user-dropdown {
  cursor: pointer;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 6px 12px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.user-info:hover {
  background: #f0f2ff;
}

.user-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.user-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
  line-height: 1.2;
}

.user-role {
  font-size: 12px;
  color: #666;
  line-height: 1.2;
}

.dropdown-icon {
  color: #999;
  transition: all 0.3s ease;
}

.user-info:hover .dropdown-icon {
  color: #667eea;
}

/* 通知面板样式 */
.notification-panel {
  padding: 0;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  font-weight: 600;
}

.no-notification {
  text-align: center;
  padding: 20px;
  color: #999;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f9f9f9;
  cursor: pointer;
  transition: background 0.3s ease;
}

.notification-item:hover {
  background: #f5f5f5;
}

.notification-item.is-read {
  opacity: 0.6;
}

.notification-title {
  margin: 0 0 4px 0;
  font-size: 14px;
  color: #333;
}

.notification-time {
  margin: 0;
  font-size: 12px;
  color: #999;
}

/* 主要内容 */
.content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

/* 页面切换动画优化 - GPU加速 */
.page-transition-enter-active,
.page-transition-leave-active {
  transition: all 0.2s cubic-bezier(0.23, 1, 0.32, 1);
  will-change: opacity, transform;
}

.page-transition-enter-from {
  opacity: 0;
  transform: translate3d(8px, 0, 0);
}

.page-transition-leave-to {
  opacity: 0;
  transform: translate3d(-8px, 0, 0);
}

/* 减少动画时的重绘 */
.page-transition-enter-active *,
.page-transition-leave-active * {
  backface-visibility: hidden;
  perspective: 1000px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    position: fixed;
    left: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    transform: translateX(-100%);
  }
  
  .sidebar.collapsed {
    transform: translateX(0);
    width: 100%;
  }
  
  .main-container {
    width: 100%;
  }
  
  .header {
    padding: 0 16px;
  }
  
  .header-left {
    gap: 12px;
  }
  
  .header-right {
    gap: 12px;
  }
  
  .user-details {
    display: none;
  }
  
  .content {
    padding: 16px;
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>