import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, logout, getUserInfo } from '@/api/auth'
import { userApi } from '@/api/user'

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
      const response = await userApi.getUserInfo()
      userInfo.value = response.data
      lastFetch.value = Date.now()
      return response
    } catch (error) {
      // API调用失败，可能是token失效
      if (error.response?.status === 401) {
        token.value = ''
        userInfo.value = {}
        localStorage.removeItem('employee_token')
      }
      throw error
    } finally {
      isLoading.value = false
    }
  }
  
  return {
    token,
    userInfo,
    isLoggedIn,
    isLoading,
    loginAction,
    logoutAction,
    getUserInfoAction,
    isCacheValid
  }
})