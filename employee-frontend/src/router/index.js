import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/dashboard',
    component: () => import('@/layout/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '个人工作台' }
      },
      {
        path: '/attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/Attendance.vue'),
        meta: { title: '考勤管理' }
      },
      {
        path: '/attendance/records',
        name: 'AttendanceRecords',
        component: () => import('@/views/attendance/AttendanceRecords.vue'),
        meta: { title: '考勤记录' }
      },
      {
        path: '/leave',
        name: 'Leave',
        component: () => import('@/views/leave/Leave.vue'),
        meta: { title: '请假管理' }
      },
      {
        path: '/leave/records',
        name: 'LeaveRecords',
        component: () => import('@/views/leave/LeaveRecords.vue'),
        meta: { title: '请假记录' }
      },
      {
        path: '/schedule',
        name: 'Schedule',
        component: () => import('@/views/schedule/Schedule.vue'),
        meta: { title: '日程管理' }
      },
      {
        path: '/notifications',
        name: 'Notifications',
        component: () => import('@/views/notifications/Notifications.vue'),
        meta: { title: '公告通知' }
      },
      {
        path: '/notifications/:id',
        name: 'NotificationDetail',
        component: () => import('@/views/notifications/NotificationDetail.vue'),
        meta: { title: '通知详情' }
      },
      {
        path: '/notifications/settings',
        name: 'NotificationSettings',
        component: () => import('@/views/notifications/NotificationSettings.vue'),
        meta: { title: '通知设置' }
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: '/collaborative',
        name: 'Collaborative',
        component: () => import('@/views/collaborative/CollaborativeIndex.vue'),
        meta: { title: '协同编辑' }
      },
      {
        path: '/collaborative/demo',
        name: 'CollaborativeDemo',
        component: () => import('@/views/collaborative/CollaborativeDemo.vue'),
        meta: { title: '协同编辑功能演示' }
      },
      {
        path: '/collaborative/richtext/:id?',
        name: 'RichTextEditor',
        component: () => import('@/views/collaborative/RichTextEditor.vue'),
        meta: { title: '富文本编辑器' }
      },
      {
        path: '/collaborative/markdown/:id?',
        name: 'MarkdownEditor',
        component: () => import('@/views/collaborative/MarkdownEditor.vue'),
        meta: { title: 'Markdown编辑器' }
      },
      {
        path: '/collaborative/code/:id?',
        name: 'CodeEditor',
        component: () => import('@/views/collaborative/CodeEditor.vue'),
        meta: { title: '代码编辑器' }
      },
      {
        path: '/collaborative/table/:id?',
        name: 'TableEditor',
        component: () => import('@/views/collaborative/TableEditor.vue'),
        meta: { title: '表格编辑器' }
      },
      {
        path: '/tasks',
        name: 'Tasks',
        component: () => import('@/views/tasks/TaskManagement.vue'),
        meta: { title: '任务管理' }
      },
      {
        path: '/tasks/create',
        name: 'TaskCreate',
        component: () => import('@/views/tasks/TaskCreate.vue'),
        meta: { title: '创建任务' }
      },
      {
        path: '/tasks/:id',
        name: 'TaskDetail',
        component: () => import('@/views/tasks/TaskDetail.vue'),
        meta: { title: '任务详情' }
      },
      {
        path: '/tasks/statistics',
        name: 'TaskStatistics',
        component: () => import('@/views/tasks/TaskStatistics.vue'),
        meta: { title: '任务统计' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 性能优化：缓存用户登录状态
let cachedAuthStatus = null
let lastAuthCheck = 0
const AUTH_CACHE_DURATION = 5000 // 5秒缓存

// 优化后的路由守卫
router.beforeEach(async (to, from, next) => {
  try {
    const userStore = useUserStore()
    const now = Date.now()
    
    // 使用缓存的认证状态，减少重复计算
    let isLoggedIn
    if (cachedAuthStatus !== null && (now - lastAuthCheck) < AUTH_CACHE_DURATION) {
      isLoggedIn = cachedAuthStatus
    } else {
      isLoggedIn = userStore.isLoggedIn
      cachedAuthStatus = isLoggedIn
      lastAuthCheck = now
    }
    
    // 需要认证但未登录
    if (to.meta.requiresAuth && !isLoggedIn) {
      // 清除缓存状态
      cachedAuthStatus = null
      next('/login')
      return
    }
    
    // 已登录但访问登录页面
    if (to.path === '/login' && isLoggedIn) {
      next('/dashboard')
      return
    }
    
    // 正常路由继续
    next()
  } catch (error) {
    console.error('路由守卫错误:', error)
    // 出错时清除缓存并重定向到登录
    cachedAuthStatus = null
    if (to.path !== '/login') {
      next('/login')
    } else {
      next()
    }
  }
})

export default router