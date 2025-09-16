/**
 * 认证工具函数
 * 用于处理token的获取、设置和清除
 */

// Token存储的key
const TOKEN_KEY = 'employee_token'

/**
 * 获取存储的token
 * @returns {string} token值，如果不存在则返回空字符串
 */
export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 设置token到本地存储
 * @param {string} token - 要存储的token
 */
export function setToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 清除存储的token
 */
export function removeToken() {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查是否已登录（token是否存在且不为空）
 * @returns {boolean} 是否已登录
 */
export function isLoggedIn() {
  const token = getToken()
  return !!token && token.length > 0
}

/**
 * 获取Authorization请求头
 * @returns {object} 包含Authorization头的对象
 */
export function getAuthHeaders() {
  const token = getToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}