import request from '@/utils/request'

// 登录
export const login = (data) => {
  return request({
    url: '/auth/login',  // 修复：移除多余的/api前缀
    method: 'post',
    data
  })
}

// 登出
export const logout = () => {
  return request({
    url: '/auth/logout',  // 修复：移除多余的/api前缀
    method: 'post'
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/auth/userinfo',  // 修复：移除多余的/api前缀
    method: 'get'
  })
}