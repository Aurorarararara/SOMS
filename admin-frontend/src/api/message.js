import request from '@/utils/request'

/**
 * 消息管理API
 */
export default {
  // 获取消息列表
  getMessageList(params) {
    return request({
      url: '/admin/messages',
      method: 'get',
      params
    })
  },

  // 发送消息
  sendMessage(data) {
    return request({
      url: '/admin/messages',
      method: 'post',
      data
    })
  },

  // 获取消息详情
  getMessageDetail(id) {
    return request({
      url: `/admin/messages/${id}`,
      method: 'get'
    })
  },

  // 删除消息
  deleteMessage(id) {
    return request({
      url: `/admin/messages/${id}`,
      method: 'delete'
    })
  },

  // 批量发送消息
  batchSendMessage(data) {
    return request({
      url: '/admin/messages/batch',
      method: 'post',
      data
    })
  },

  // 标记消息为已读
  markAsRead(id) {
    return request({
      url: `/admin/messages/${id}/read`,
      method: 'put'
    })
  },

  // 批量标记为已读
  batchMarkAsRead(messageIds) {
    return request({
      url: '/admin/messages/batch/read',
      method: 'put',
      data: { messageIds }
    })
  },

  // 获取未读消息数量
  getUnreadCount() {
    return request({
      url: '/admin/messages/unread/count',
      method: 'get'
    })
  },

  // 获取消息统计
  getMessageStatistics() {
    return request({
      url: '/admin/messages/statistics',
      method: 'get'
    })
  },

  // 搜索消息
  searchMessages(keyword) {
    return request({
      url: '/admin/messages/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 获取系统通知
  getSystemNotifications() {
    return request({
      url: '/admin/messages/notifications/system',
      method: 'get'
    })
  },

  // 发送系统通知
  sendSystemNotification(data) {
    return request({
      url: '/admin/messages/notifications/system',
      method: 'post',
      data
    })
  }
}