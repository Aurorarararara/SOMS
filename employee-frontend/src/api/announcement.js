import request from '@/utils/request'

export const announcementApi = {
  // 获取公告列表
  getList(params) {
    return request.get('/announcements', { params })
  },

  // 获取最近公告
  getRecentList(limit = 5) {
    return request.get(`/announcements/recent?limit=${limit}`)
  },

  // 获取公告详情
  getDetail(id) {
    return request.get(`/announcements/${id}`)
  },

  // 标记为已读
  markAsRead(id) {
    return request.post(`/announcements/${id}/read`)
  },

  // 标记为未读
  markAsUnread(id) {
    return request.post(`/announcements/${id}/unread`)
  },

  // 全部标记为已读
  markAllAsRead() {
    return request.post('/announcements/read-all')
  },

  // 删除通知
  deleteNotification(id) {
    return request.delete(`/announcements/${id}`)
  },

  // 获取未读数量
  getUnreadCount() {
    return request.get('/announcements/unread-count')
  }
}