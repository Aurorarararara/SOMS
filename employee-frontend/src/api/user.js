import request from '@/utils/request'

export const userApi = {
  // 获取用户信息
  getUserInfo() {
    return request.get('/auth/userinfo')
  },

  // 更新用户基本信息
  updateUserInfo(data) {
    return request.put('/user/info', data)
  },

  // 修改密码
  changePassword(data) {
    return request.post('/user/change-password', data)
  },

  // 上传头像
  uploadAvatar(file) {
    const formData = new FormData()
    formData.append('avatar', file)
    return request.post('/user/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },

  // 更新偏好设置
  updatePreferences(data) {
    return request.put('/user/preferences', data)
  },

  // 获取工作统计
  getWorkStats() {
    return request.get('/user/work-stats')
  }
}