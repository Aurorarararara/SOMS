import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/',
    redirect: '/admin'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/admin',
    redirect: '/admin/dashboard',
    component: () => import('@/layout/Layout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '/admin/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '综合仪表盘' }
      },
      {
        path: '/admin/employees',
        name: 'Employees',
        component: () => import('@/views/employees/Employees.vue'),
        meta: { title: '员工管理' }
      },
      {
        path: '/admin/departments',
        name: 'Departments',
        component: () => import('@/views/departments/Departments.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: '/admin/organization/chart',
        name: 'OrganizationChart',
        component: () => import('@/views/organization/OrganizationChart.vue'),
        meta: { title: '组织架构图' }
      },
      {
        path: '/admin/roles',
        name: 'Roles',
        component: () => import('@/views/roles/Roles.vue'),
        meta: { title: '角色管理' }
      },
      // 考勤管理子路由
      {
        path: '/admin/attendance/records',
        name: 'AttendanceRecords',
        component: () => import('@/views/attendance/AttendanceRecords.vue'),
        meta: { title: '考勤记录' }
      },
      {
        path: '/admin/attendance/rules',
        name: 'AttendanceRules',
        component: () => import('@/views/attendance/AttendanceRules.vue'),
        meta: { title: '考勤规则' }
      },
      {
        path: '/admin/attendance/statistics',
        name: 'AttendanceStatistics',
        component: () => import('@/views/attendance/AttendanceStatistics.vue'),
        meta: { title: '考勤统计' }
      },
      // 请假管理子路由
      {
        path: '/admin/leave/applications',
        name: 'LeaveApplications',
        component: () => import('@/views/leave/LeaveApplications.vue'),
        meta: { title: '请假申请' }
      },
      {
        path: '/admin/leave/approval',
        name: 'LeaveApproval',
        component: () => import('@/views/leave/LeaveApproval.vue'),
        meta: { title: '审批管理' }
      },
      {
        path: '/admin/leave/statistics',
        name: 'LeaveStatistics',
        component: () => import('@/views/leave/LeaveStatistics.vue'),
        meta: { title: '请假统计' }
      },
      // 报销管理
      {
        path: '/admin/expense/applications',
        name: 'ExpenseApplications',
        component: () => import('@/views/expense/ExpenseApplications.vue'),
        meta: { title: '报销申请' }
      },
      {
        path: '/admin/expense/approval',
        name: 'ExpenseApproval',
        component: () => import('@/views/expense/ExpenseApproval.vue'),
        meta: { title: '报销审批' }
      },
      // 公告管理
      {
        path: '/admin/announcements',
        name: 'Announcements',
        component: () => import('@/views/announcements/Announcements.vue'),
        meta: { title: '公告管理' }
      },
      // 审批流程管理
      {
        path: '/admin/workflow',
        name: 'WorkflowManagement',
        component: () => import('@/views/workflow/WorkflowManagement.vue'),
        meta: { title: '审批流程管理' }
      },
      {
        path: '/admin/workflow/my-tasks',
        name: 'MyTasks',
        component: () => import('@/views/workflow/MyTasks.vue'),
        meta: { title: '我的待办任务' }
      },
      {
        path: '/admin/workflow/my-initiated',
        name: 'MyInitiated',
        component: () => import('@/views/workflow/MyInitiated.vue'),
        meta: { title: '我发起的流程' }
      },
      // 数据报表子路由
      {
        path: '/admin/reports/attendance',
        name: 'ReportsAttendance',
        component: () => import('@/views/reports/ReportsAttendance.vue'),
        meta: { title: '考勤报表' }
      },
      {
        path: '/admin/reports/leave',
        name: 'ReportsLeave',
        component: () => import('@/views/reports/ReportsLeave.vue'),
        meta: { title: '请假报表' }
      },
      {
        path: '/admin/reports/performance',
        name: 'ReportsPerformance',
        component: () => import('@/views/reports/ReportsPerformance.vue'),
        meta: { title: '绩效报表' }
      },
      // 系统设置子路由
      {
        path: '/admin/system/config',
        name: 'SystemConfig',
        component: () => import('@/views/system/SystemConfig.vue'),
        meta: { title: '系统配置' }
      },
      {
        path: '/admin/system/logs',
        name: 'SystemLogs',
        component: () => import('@/views/system/SystemLogs.vue'),
        meta: { title: '操作日志' }
      },
      {
        path: '/admin/system/backup',
        name: 'SystemBackup',
        component: () => import('@/views/system/SystemBackup.vue'),
        meta: { title: '数据备份' }
      },
      {
        path: '/admin/profile',
        name: 'Profile',
        component: () => import('@/views/profile/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: '/admin/chat',
        name: 'ChatRoom',
        component: () => import('@/views/chat/ChatRoom.vue'),
        meta: { title: '聊天室' }
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
      next('/admin/dashboard')
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