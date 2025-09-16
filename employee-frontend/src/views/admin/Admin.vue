<template>
  <div class="admin-layout">
    <!-- 侧边栏 -->
    <div class="admin-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <div class="sidebar-header">
        <div class="logo">
          <el-icon><Setting /></el-icon>
          <span v-show="!sidebarCollapsed" class="logo-text">管理中心</span>
        </div>
        <el-button
          text
          class="collapse-btn"
          @click="toggleSidebar"
        >
          <el-icon><Expand v-if="sidebarCollapsed" /><Fold v-else /></el-icon>
        </el-button>
      </div>
      
      <el-menu
        :default-active="activeMenu"
        class="admin-menu"
        :collapse="sidebarCollapsed"
        @select="handleMenuSelect"
      >
        <el-menu-item index="dashboard">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        
        <el-sub-menu index="expense">
          <template #title>
            <el-icon><Money /></el-icon>
            <span>报销管理</span>
          </template>
          <el-menu-item index="expense-approval">
            <el-icon><DocumentChecked /></el-icon>
            <span>审批管理</span>
          </el-menu-item>
          <el-menu-item index="expense-statistics">
            <el-icon><DataAnalysis /></el-icon>
            <span>统计分析</span>
          </el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="user">
          <template #title>
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </template>
          <el-menu-item index="user-list">
            <el-icon><UserFilled /></el-icon>
            <span>用户列表</span>
          </el-menu-item>
          <el-menu-item index="role-management">
            <el-icon><Key /></el-icon>
            <span>角色权限</span>
          </el-menu-item>
        </el-sub-menu>
        
        <el-sub-menu index="system">
          <template #title>
            <el-icon><Tools /></el-icon>
            <span>系统设置</span>
          </template>
          <el-menu-item index="system-config">
            <el-icon><Setting /></el-icon>
            <span>系统配置</span>
          </el-menu-item>
          <el-menu-item index="audit-log">
            <el-icon><Document /></el-icon>
            <span>操作日志</span>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>
    
    <!-- 主内容区 -->
    <div class="admin-main" :class="{ expanded: sidebarCollapsed }">
      <!-- 顶部导航 -->
      <div class="admin-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/admin' }">管理中心</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageTitle">{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :size="32" :src="userInfo.avatar">
                <el-icon><User /></el-icon>
              </el-avatar>
              <span class="username">{{ userInfo.name }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人资料
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>
                  设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      
      <!-- 内容区域 -->
      <div class="admin-content">
        <!-- 仪表盘 -->
        <div v-if="activeMenu === 'dashboard'" class="content-page">
          <AdminDashboard @navigate="handleNavigate" />
        </div>
        
        <!-- 报销审批 -->
        <div v-else-if="activeMenu === 'expense-approval'" class="content-page">
          <ExpenseApproval />
        </div>
        
        <!-- 报销统计 -->
        <div v-else-if="activeMenu === 'expense-statistics'" class="content-page">
          <ExpenseStatistics />
        </div>
        
        <!-- 其他页面占位 -->
        <div v-else class="content-page">
          <el-result
            icon="info"
            title="功能开发中"
            :sub-title="`${currentPageTitle} 功能正在开发中，敬请期待`"
          >
            <template #extra>
              <el-button type="primary" @click="activeMenu = 'dashboard'">返回仪表盘</el-button>
            </template>
          </el-result>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Setting,
  Expand,
  Fold,
  DataBoard,
  Money,
  DocumentChecked,
  DataAnalysis,
  User,
  UserFilled,
  Key,
  Tools,
  Document,
  ArrowDown,
  SwitchButton
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import AdminDashboard from './components/AdminDashboard.vue'
import ExpenseApproval from './ExpenseApproval.vue'
import ExpenseStatistics from './ExpenseStatistics.vue'
import AdminDashboard from './components/AdminDashboard.vue'

// 路由和状态管理
const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const sidebarCollapsed = ref(false)
const activeMenu = ref('dashboard')

// 用户信息
const userInfo = reactive({
  name: '管理员',
  avatar: '',
  role: 'admin'
})

// 页面标题映射
const pageTitleMap = {
  dashboard: '仪表盘',
  'expense-approval': '报销审批',
  'expense-statistics': '报销统计',
  'user-list': '用户列表',
  'role-management': '角色权限',
  'system-config': '系统配置',
  'audit-log': '操作日志'
}

// 计算属性
const currentPageTitle = computed(() => {
  return pageTitleMap[activeMenu.value] || ''
})

// 生命周期
onMounted(() => {
  loadUserInfo()
  // 从路由参数获取初始页面
  const routeQuery = router.currentRoute.value.query
  if (routeQuery.page && pageTitleMap[routeQuery.page]) {
    activeMenu.value = routeQuery.page
  }
})

// 方法
const loadUserInfo = () => {
  const user = userStore.userInfo
  if (user) {
    Object.assign(userInfo, {
      name: user.name || '管理员',
      avatar: user.avatar || '',
      role: user.role || 'admin'
    })
  }
}

const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

const handleMenuSelect = (index) => {
  activeMenu.value = index
  
  // 更新路由查询参数
  router.push({
    path: '/admin',
    query: { page: index }
  })
}

const handleNavigate = (menuKey, params = {}) => {
  activeMenu.value = menuKey
  // 如果有参数，可以在这里处理
  if (params.applicationId) {
    // 可以通过事件总线或其他方式传递参数给目标组件
    console.log('Navigate to', menuKey, 'with params:', params)
  }
}

const handleUserCommand = async (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人资料功能开发中')
      break
    case 'settings':
      ElMessage.info('设置功能开发中')
      break
    case 'logout':
      await handleLogout()
      break
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '退出确认',
      {
        type: 'warning'
      }
    )
    
    // 清除用户信息
    await userStore.logout()
    
    // 跳转到登录页
    router.push('/login')
    
    ElMessage.success('已退出登录')
  } catch (error) {
    // 用户取消退出
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  height: 100vh;
  background: #f0f2f5;
}

.admin-sidebar {
  width: 250px;
  background: white;
  border-right: 1px solid #e4e7ed;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
}

.admin-sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  border-bottom: 1px solid #e4e7ed;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.logo-text {
  transition: opacity 0.3s ease;
}

.collapse-btn {
  padding: 8px;
  border-radius: 4px;
}

.admin-menu {
  flex: 1;
  border: none;
  overflow-y: auto;
}

.admin-menu :deep(.el-menu-item),
.admin-menu :deep(.el-sub-menu__title) {
  height: 48px;
  line-height: 48px;
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: #ecf5ff;
  color: #409eff;
}

.admin-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  transition: margin-left 0.3s ease;
}

.admin-main.expanded {
  margin-left: 0;
}

.admin-header {
  height: 60px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}

.header-left {
  flex: 1;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 8px 12px;
  border-radius: 6px;
  transition: background-color 0.3s ease;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}

.dropdown-icon {
  font-size: 12px;
  color: #909399;
  transition: transform 0.3s ease;
}

.admin-content {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.content-page {
  flex: 1;
  overflow: auto;
  background: #f0f2f5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .admin-sidebar {
    position: fixed;
    left: 0;
    top: 0;
    z-index: 1000;
    height: 100vh;
    transform: translateX(-100%);
    transition: transform 0.3s ease;
  }
  
  .admin-sidebar.show {
    transform: translateX(0);
  }
  
  .admin-main {
    margin-left: 0;
  }
  
  .admin-header {
    padding: 0 16px;
  }
  
  .header-left {
    display: none;
  }
}

/* 滚动条样式 */
.admin-menu::-webkit-scrollbar {
  width: 4px;
}

.admin-menu::-webkit-scrollbar-track {
  background: transparent;
}

.admin-menu::-webkit-scrollbar-thumb {
  background: #dcdfe6;
  border-radius: 2px;
}

.admin-menu::-webkit-scrollbar-thumb:hover {
  background: #c0c4cc;
}

.content-page::-webkit-scrollbar {
  width: 6px;
}

.content-page::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.content-page::-webkit-scrollbar-thumb {
  background: #c0c4cc;
  border-radius: 3px;
}

.content-page::-webkit-scrollbar-thumb:hover {
  background: #a8abb2;
}
</style>