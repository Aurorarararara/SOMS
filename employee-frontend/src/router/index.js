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
        component: () => import('@/views/notifications/Notifications.vue'),
        meta: { title: '通知详情' }
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
        path: '/chat',
        name: 'ChatRoom',
        component: () => import('@/views/chat/ChatRoom.vue'),
        meta: { title: '群聊' }
      },
      {
        path: '/directchat',
        name: 'DirectChat',
        component: () => import('@/views/chat/DirectChat.vue'),
        meta: { title: '一对一聊天' }
      },
      {
        path: '/ai-assistant',
        name: 'AIAssistant',
        component: () => import('@/views/chat/AIAssistant.vue'),
        meta: { title: 'AI助手' }
      },
      {
        path: '/meeting',
        name: 'Meeting',
        component: () => import('@/views/meeting/MeetingList.vue'),
        meta: { title: '在线会议' }
      },
      {
        path: '/meeting/create',
        name: 'CreateMeeting',
        component: () => import('@/views/meeting/CreateMeeting.vue'),
        meta: { title: '创建会议' }
      },
      {
        path: '/meeting/join',
        name: 'JoinMeeting',
        component: () => import('@/views/meeting/JoinMeeting.vue'),
        meta: { title: '加入会议' }
      },
      {
        path: '/meeting/room/:id',
        name: 'MeetingRoom',
        component: () => import('@/views/meeting/MeetingRoom.vue'),
        meta: { title: '会议室' }
      },
      {
        path: '/expense',
        name: 'Expense',
        component: () => import('@/views/expense/Expense.vue'),
        meta: { title: '报销申请', requiresAuth: true }
      },
      {
        path: '/expense/create',
        name: 'ExpenseCreate',
        component: () => import('@/views/expense/ExpenseCreate.vue'),
        meta: { title: '创建报销', requiresAuth: true }
      },
      {
        path: '/admin',
        name: 'Admin',
        component: () => import('@/views/admin/Admin.vue'),
        meta: { 
          title: '管理后台', 
          requiresAuth: true,
          requiresRole: ['admin', 'manager']
        }
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
    
    // 检查角色权限
    if (to.meta.requiresRole && isLoggedIn) {
      const userRole = userStore.userInfo?.role
      const requiredRoles = Array.isArray(to.meta.requiresRole) ? to.meta.requiresRole : [to.meta.requiresRole]
      
      if (!userRole || !requiredRoles.includes(userRole)) {
        // 权限不足，重定向到首页或显示错误页面
        next('/dashboard')
        return
      }
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