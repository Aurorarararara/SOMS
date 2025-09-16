<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'collapsed': sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo" v-if="!sidebarCollapsed">
          <el-icon size="32" color="#667eea"><Setting /></el-icon>
          <span class="logo-text">管理平台</span>
        </div>
        <div class="logo-mini" v-else>
          <el-icon size="28" color="#667eea"><Setting /></el-icon>
        </div>
      </div>
      
      <div class="sidebar-menu">
        <el-menu
          :default-active="currentRoute"
          :collapse="sidebarCollapsed"
          :unique-opened="true"
          router
          class="admin-menu"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataBoard /></el-icon>
            <template #title>{{ $t('nav.dashboard') }}</template>
          </el-menu-item>
          
          <el-sub-menu index="/admin/user">
            <template #title>
              <el-icon><User /></el-icon>
              <span>{{ $t('nav.employees') }}</span>
            </template>
            <el-menu-item index="/admin/employees">{{ $t('nav.employees') }}</el-menu-item>
            <el-menu-item index="/admin/departments">{{ $t('nav.departments') }}</el-menu-item>
            <el-menu-item index="/admin/roles">{{ $t('nav.roles') }}</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/admin/attendance">
            <template #title>
              <el-icon><Clock /></el-icon>
              <span>{{ $t('nav.attendanceRecords') }}</span>
            </template>
            <el-menu-item index="/admin/attendance/records">{{ $t('nav.attendanceRecords') }}</el-menu-item>
            <el-menu-item index="/admin/attendance/rules">{{ $t('nav.attendanceRules') }}</el-menu-item>
            <el-menu-item index="/admin/attendance/statistics">{{ $t('nav.attendanceStatistics') }}</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/admin/leave">
            <template #title>
              <el-icon><DocumentRemove /></el-icon>
              <span>{{ $t('nav.leaveApplications') }}</span>
            </template>
            <el-menu-item index="/admin/leave/applications">{{ $t('nav.leaveApplications') }}</el-menu-item>
            <el-menu-item index="/admin/leave/approval">{{ $t('nav.leaveApproval') }}</el-menu-item>
            <el-menu-item index="/admin/leave/statistics">{{ $t('nav.leaveStatistics') }}</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/admin/expense">
            <template #title>
              <el-icon><Money /></el-icon>
              <span>{{ $t('nav.expenseManagement') }}</span>
            </template>
            <el-menu-item index="/admin/expense/applications">{{ $t('nav.expenseApplications') }}</el-menu-item>
            <el-menu-item index="/admin/expense/approval">{{ $t('nav.expenseApproval') }}</el-menu-item>
          </el-sub-menu>
          
          <el-menu-item index="/admin/announcements">
            <el-icon><Bell /></el-icon>
            <template #title>{{ $t('nav.announcements') }}</template>
          </el-menu-item>

          <el-menu-item index="/admin/workflow">
            <el-icon><Operation /></el-icon>
            <template #title>{{ $t('nav.workflow') }}</template>
          </el-menu-item>
          
          <el-sub-menu index="/admin/reports">
            <template #title>
              <el-icon><DataAnalysis /></el-icon>
              <span>{{ $t('nav.attendanceReports') }}</span>
            </template>
            <el-menu-item index="/admin/reports/attendance">{{ $t('nav.attendanceReports') }}</el-menu-item>
            <el-menu-item index="/admin/reports/leave">{{ $t('nav.leaveReports') }}</el-menu-item>
            <el-menu-item index="/admin/reports/performance">{{ $t('nav.performanceReports') }}</el-menu-item>
          </el-sub-menu>
          
          <el-sub-menu index="/admin/system">
            <template #title>
              <el-icon><Setting /></el-icon>
              <span>{{ $t('nav.systemConfig') }}</span>
            </template>
            <el-menu-item index="/admin/system/config">{{ $t('nav.systemConfig') }}</el-menu-item>
            <el-menu-item index="/admin/system/logs">{{ $t('nav.operationLogs') }}</el-menu-item>
            <el-menu-item index="/admin/system/backup">{{ $t('nav.dataBackup') }}</el-menu-item>
          </el-sub-menu>

          <!-- 添加组织架构图菜单项 -->
          <el-menu-item index="/admin/organization/chart">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>{{ $t('nav.organizationChart') }}</template>
          </el-menu-item>

          <el-menu-item index="/admin/chat">
            <el-icon><ChatDotRound /></el-icon>
            <template #title>聊天室</template>
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
            @click="toggleSidebar"
            class="sidebar-toggle"
          >
            <el-icon size="20"><Menu /></el-icon>
          </el-button>
          
          <EnhancedBreadcrumb />
        </div>
        
        <div class="header-center">
          <!-- 全局搜索 -->
          <GlobalSearch />
        </div>

        <div class="header-right">
          <!-- 快捷操作 -->
          <div class="header-actions">
            <el-tooltip :content="$t('layout.refreshPage')" placement="bottom">
              <el-button text @click="refreshPage">
                <el-icon size="18"><Refresh /></el-icon>
              </el-button>
            </el-tooltip>
            
            <el-tooltip :content="$t('layout.toggleFullscreen')" placement="bottom">
              <el-button text @click="toggleFullscreen">
                <el-icon size="18"><FullScreen /></el-icon>
              </el-button>
            </el-tooltip>
            
            <!-- 通知 -->
            <el-dropdown trigger="click">
              <el-button text class="notification-btn">
                <el-icon size="18"><Bell /></el-icon>
                <el-badge :value="notificationCount" :hidden="notificationCount === 0" />
              </el-button>
              <template #dropdown>
                <el-dropdown-menu class="notification-dropdown">
                  <div class="notification-header">
                    <span>通知消息</span>
                    <el-button text size="small" @click="markAllRead">全部已读</el-button>
                  </div>
                  <div class="notification-list">
                    <div 
                      v-for="item in notifications" 
                      :key="item.id"
                      class="notification-item"
                      @click="handleNotificationClick(item)"
                    >
                      <div class="notification-content">
                        <div class="notification-title">{{ item.title }}</div>
                        <div class="notification-time">{{ formatTime(item.time) }}</div>
                      </div>
                      <div class="notification-status" v-if="!item.read"></div>
                    </div>
                    <div v-if="notifications.length === 0" class="no-notifications">
                      暂无通知
                    </div>
                  </div>
                  <div class="notification-footer">
                    <el-button text type="primary" @click="viewAllNotifications">查看全部</el-button>
                  </div>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <!-- 语言切换 -->
            <LanguageSwitcher />
          </div>

          <!-- 用户信息 -->
          <el-dropdown trigger="click" class="user-dropdown">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.user?.avatar">
                {{ userStore.user?.name?.charAt(0) }}
              </el-avatar>
              <div class="user-details" v-if="!sidebarCollapsed">
                <div class="user-name">{{ userStore.user?.name }}</div>
                <div class="user-role">{{ userStore.user?.role?.name }}</div>
              </div>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="$router.push('/admin/profile')">
                  <el-icon><User /></el-icon>
                  {{ $t('user.profile') }}
                </el-dropdown-item>
                <el-dropdown-item @click="$router.push('/admin/settings')">
                  <el-icon><Setting /></el-icon>
                  {{ $t('user.settings') }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">
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
            <keep-alive :include="['Dashboard', 'Profile']">
              <component :is="Component" :key="route.fullPath" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting, DataBoard, User, Clock, DocumentRemove, Bell, DataAnalysis,
  Menu, Refresh, FullScreen, ArrowDown, SwitchButton, Operation,
  OfficeBuilding, ChatDotRound, Money
} from '@element-plus/icons-vue'

import { useUserStore } from '@/stores/user'
import { useDebounce, useThrottle } from '@/utils/debounce'
import EnhancedBreadcrumb from '@/components/EnhancedBreadcrumb.vue'
import LanguageSwitcher from '@/components/LanguageSwitcher.vue'
import GlobalSearch from '@/components/GlobalSearch/GlobalSearch.vue'

const route = useRoute()
const router = useRouter()
const { t: $t } = useI18n()
const userStore = useUserStore()

// 响应式数据
const sidebarCollapsed = ref(false)
const notificationCount = ref(3)
const notifications = ref([
  { id: 1, title: '新员工入职申请待审批', time: '2024-01-15 10:30', read: false },
  { id: 2, title: '系统维护通知', time: '2024-01-15 09:15', read: false },
  { id: 3, title: '月度考勤报表已生成', time: '2024-01-14 16:45', read: true }
])

// 计算属性
const currentRoute = computed(() => route.path)

// 原面包屑导航逻辑已移至 EnhancedBreadcrumb 组件

// 方法 - 添加防抖优化
const toggleSidebar = useDebounce(() => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}, 150)

const refreshPage = useDebounce(() => {
  location.reload()
}, 1000)

const toggleFullscreen = useDebounce(() => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
  }
}, 300)

const handleNotificationClick = useDebounce((notification) => {
  notification.read = true
  notificationCount.value = notifications.value.filter(n => !n.read).length
  ElMessage.info(`点击了通知: ${notification.title}`)
}, 200)

const markAllRead = useDebounce(() => {
  notifications.value.forEach(n => n.read = true)
  notificationCount.value = 0
  ElMessage.success('所有通知已标记为已读')
}, 300)

const viewAllNotifications = useDebounce(() => {
  router.push('/admin/notifications')
}, 200)

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确认退出登录吗？', '退出确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
        
    await userStore.logout()
    ElMessage.success('已安全退出')
    router.push('/admin/login')
  } catch (error) {
    // 用户取消操作
  }
}

// 生命周期
onMounted(() => {
  // 初始化通知数量
  notificationCount.value = notifications.value.filter(n => !n.read).length
})

// 暴露给模板

</script>

<style scoped>
.admin-layout {
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

.admin-menu {
  border: none;
  padding: 12px 0;
}

:deep(.admin-menu .el-menu-item) {
  margin: 4px 12px;
  border-radius: 8px;
  height: 48px;
  line-height: 48px;
}

:deep(.admin-menu .el-sub-menu) {
  margin: 4px 12px;
}

:deep(.admin-menu .el-sub-menu .el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
  border-radius: 8px;
}

:deep(.admin-menu .el-menu-item.is-active) {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
}

:deep(.admin-menu .el-menu-item:hover) {
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

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  max-width: 400px;
  margin: 0 20px;
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

/* 通知下拉菜单 */
.notification-dropdown {
  width: 320px;
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

.notification-list {
  max-height: 300px;
  overflow-y: auto;
}

.notification-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f8f9fa;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 14px;
  color: #1a1a1a;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-status {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #f56565;
}

.no-notifications {
  text-align: center;
  padding: 40px 16px;
  color: #999;
  font-size: 14px;
}

.notification-footer {
  padding: 12px 16px;
  border-top: 1px solid #f0f0f0;
  text-align: center;
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