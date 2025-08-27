import request from '@/utils/request'

// 登录
export const login = (data) => {
  return request({
    url: '/auth/admin/login',
    method: 'post',
    data
  })
}

// 登出
export const logout = () => {
  return request({
    url: '/auth/admin/logout',
    method: 'post'
  })
}

// 获取用户信息
export const getUserInfo = () => {
  return request({
    url: '/auth/admin/userinfo',
    method: 'get'
  })
}

// 更新用户信息
export const updateUserInfo = (data) => {
  return request({
    url: '/auth/admin/profile',
    method: 'put',
    data
  })
}

// 修改密码
export const changePassword = (data) => {
  return request({
    url: '/auth/admin/password',
    method: 'put',
    data
  })
}