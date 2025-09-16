import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo } from '@/api/auth'  // 从auth.js导入getUserInfo
import { login, logout } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('employee_token') || '')
  const userInfo = ref({})
  const isLoading = ref(false)
  const lastFetch = ref(0)
  const CACHE_DURATION = 5 * 60 * 1000 // 5分钟缓存
  
  // 计算属性 - 优化版本
  const isLoggedIn = computed(() => {
    return !!token.value && token.value.length > 0
  })
  
  // 检查缓存是否有效
  const isCacheValid = () => {
    return Date.now() - lastFetch.value < CACHE_DURATION
  }
  
  // 登录 - 优化版本
  const loginAction = async (loginForm) => {
    if (isLoading.value) return
    
    isLoading.value = true
    try {
      const response = await login(loginForm)
      token.value = response.data.token
      userInfo.value = response.data.userInfo
      lastFetch.value = Date.now()
      localStorage.setItem('employee_token', token.value)
      return response
    } catch (error) {
      // 清理状态
      token.value = ''
      userInfo.value = {}
      localStorage.removeItem('employee_token')
      throw error
    } finally {
      isLoading.value = false
    }
  }
  
  // 登出 - 优化版本
  const logoutAction = async () => {
    try {
      if (token.value) {
        await logout()
      }
    } catch (error) {
      console.error('登出失败:', error)
    } finally {
      // 强制清理本地状态
      token.value = ''
      userInfo.value = {}
      lastFetch.value = 0
      localStorage.removeItem('employee_token')
    }
  }
  
  // 获取用户信息 - 优化版本
  const getUserInfoAction = async (forceRefresh = false) => {
    // 检查缓存
    if (!forceRefresh && userInfo.value.id && isCacheValid()) {
      return { data: userInfo.value }
    }
    
    if (isLoading.value) return
    
    isLoading.value = true
    try {
      const response = await getUserInfo()  // 使用从auth.js导入的getUserInfo
      userInfo.value = response.data
      lastFetch.value = Date.now()
      return response
    } catch (error) {
      // API调用失败，可能是token失效
      console.error('获取用户信息失败:', error)
      
      // 检查错误类型并提供适当的处理
      if (error.response?.status === 401) {
        // 401未授权，清除token
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('employee_token')
      } else if (error.response?.status === 403) {
        // 403禁止访问，可能是权限问题
        console.warn('用户信息访问被禁止，可能需要重新登录')
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('employee_token')
      } else if (error.response?.status === 500) {
        // 500服务器内部错误，提供一个默认的用户对象
        userInfo.value = {
          id: 0,
          username: 'guest',
          realName: '游客用户',
          email: '',
          phone: '',
          avatar: '',
          roles: ['user']
        }
      } else {
        // 其他错误，也提供默认用户信息以保证功能可用
        userInfo.value = {
          id: 0,
          username: 'guest',
          realName: '游客用户',
          email: '',
          phone: '',
          avatar: '',
          roles: ['user']
        }
      }
      throw error
    } finally {
      isLoading.value = false
    }
  }
  
  // 权限检查方法
  const hasRole = (role) => {
    if (!userInfo.value || !userInfo.value.role) return false
    return userInfo.value.role === role
  }
  
  const hasAnyRole = (roles) => {
    if (!userInfo.value || !userInfo.value.role) return false
    return Array.isArray(roles) ? roles.includes(userInfo.value.role) : roles === userInfo.value.role
  }
  
  const isAdmin = computed(() => {
    return hasRole('admin')
  })
  
  const isManager = computed(() => {
    return hasAnyRole(['admin', 'manager'])
  })

  return {
    token,
    userInfo,
    isLoggedIn,
    isLoading,
    loginAction,
    logoutAction,
    getUserInfoAction,
    isCacheValid,
    hasRole,
    hasAnyRole,
    isAdmin,
    isManager
  }
})