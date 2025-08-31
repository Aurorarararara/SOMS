import request from '@/utils/request'

/**
 * 管理端通知管理API
 */

// ==================== 基础通知操作 ====================

/**
 * 分页查询所有通知（管理员用）
 */
export function getAllNotifications(params) {
  return request({
    url: '/admin/notifications/all',
    method: 'get',
    params
  })
}

/**
 * 分页查询用户通知
 */
export function getNotificationList(params) {
  return request({
    url: '/admin/notifications/page',
    method: 'get',
    params
  })
}

/**
 * 复杂条件查询通知
 */
export function searchNotifications(params) {
  return request({
    url: '/admin/notifications/search',
    method: 'post',
    params
  })
}

/**
 * 发送通知
 */
export function sendNotification(data) {
  return request({
    url: '/admin/notifications/send',
    method: 'post',
    data
  })
}

/**
 * 批量发送通知
 */
export function batchSendNotifications(data) {
  return request({
    url: '/admin/notifications/batch/send',
    method: 'post',
    data
  })
}

/**
 * 广播系统通知
 */
export function broadcastSystemNotification(data) {
  return request({
    url: '/admin/notifications/broadcast',
    method: 'post',
    data
  })
}

/**
 * 按部门发送通知
 */
export function sendNotificationToDepartment(departmentId, data) {
  return request({
    url: `/admin/notifications/department/${departmentId}/send`,
    method: 'post',
    data
  })
}

/**
 * 获取未读通知
 */
export function getUnreadNotifications() {
  return request({
    url: '/admin/notifications/unread',
    method: 'get'
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/admin/notifications/unread/count',
    method: 'get'
  })
}

/**
 * 获取通知统计
 */
export function getNotificationStats() {
  return request({
    url: '/admin/notifications/stats',
    method: 'get'
  })
}

/**
 * 获取系统通知统计（管理员用）
 */
export function getSystemNotificationStats() {
  return request({
    url: '/admin/notifications/stats/system',
    method: 'get'
  })
}

/**
 * 获取按部门分组的通知统计
 */
export function getNotificationStatsByDepartment() {
  return request({
    url: '/admin/notifications/stats/department',
    method: 'get'
  })
}

/**
 * 获取通知发送统计
 */
export function getNotificationSendStats(params) {
  return request({
    url: '/admin/notifications/stats/send',
    method: 'get',
    params
  })
}

/**
 * 获取最近通知
 */
export function getRecentNotifications(limit = 10) {
  return request({
    url: '/admin/notifications/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取任务相关通知
 */
export function getTaskNotifications(taskId) {
  return request({
    url: `/admin/notifications/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 获取系统通知
 */
export function getSystemNotices() {
  return request({
    url: '/admin/notifications/system',
    method: 'get'
  })
}

/**
 * 获取紧急通知
 */
export function getUrgentNotifications() {
  return request({
    url: '/admin/notifications/urgent',
    method: 'get'
  })
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(id) {
  return request({
    url: `/admin/notifications/${id}`,
    method: 'get'
  })
}

/**
 * 按类型获取通知
 */
export function getNotificationsByType(notificationType) {
  return request({
    url: `/admin/notifications/type/${notificationType}`,
    method: 'get'
  })
}

// ==================== 通知操作 ====================

/**
 * 标记通知为已读
 */
export function markAsRead(id) {
  return request({
    url: `/admin/notifications/${id}/read`,
    method: 'put'
  })
}

/**
 * 批量标记为已读
 */
export function batchMarkAsRead(notificationIds) {
  return request({
    url: '/admin/notifications/batch/read',
    method: 'put',
    data: notificationIds
  })
}

/**
 * 标记所有未读通知为已读
 */
export function markAllAsRead() {
  return request({
    url: '/admin/notifications/all/read',
    method: 'put'
  })
}

/**
 * 删除通知
 */
export function deleteNotification(id) {
  return request({
    url: `/admin/notifications/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除通知
 */
export function batchDeleteNotifications(notificationIds) {
  return request({
    url: '/admin/notifications/batch',
    method: 'delete',
    data: notificationIds
  })
}

/**
 * 管理员批量删除通知
 */
export function adminBatchDeleteNotifications(notificationIds) {
  return request({
    url: '/admin/notifications/admin/batch',
    method: 'delete',
    data: notificationIds
  })
}

/**
 * 清理过期通知
 */
export function cleanExpiredNotifications(expireDays = 30) {
  return request({
    url: '/admin/notifications/cleanup',
    method: 'delete',
    params: { expireDays }
  })
}

/**
 * 检查是否有未读紧急通知
 */
export function hasUrgentUnreadNotifications() {
  return request({
    url: '/admin/notifications/urgent/check',
    method: 'get'
  })
}

// ==================== 通知管理 ====================

/**
 * 获取通知模板
 */
export function getNotificationTemplates() {
  return request({
    url: '/admin/notifications/templates',
    method: 'get'
  })
}

/**
 * 创建通知模板
 */
export function createNotificationTemplate(data) {
  return request({
    url: '/admin/notifications/templates',
    method: 'post',
    data
  })
}

/**
 * 更新通知模板
 */
export function updateNotificationTemplate(id, data) {
  return request({
    url: `/admin/notifications/templates/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除通知模板
 */
export function deleteNotificationTemplate(id) {
  return request({
    url: `/admin/notifications/templates/${id}`,
    method: 'delete'
  })
}

/**
 * 预览通知模板
 */
export function previewNotificationTemplate(templateId, data) {
  return request({
    url: `/admin/notifications/templates/${templateId}/preview`,
    method: 'post',
    data
  })
}

// ==================== 通知设置 ====================

/**
 * 获取系统通知设置
 */
export function getSystemNotificationSettings() {
  return request({
    url: '/admin/notifications/settings/system',
    method: 'get'
  })
}

/**
 * 更新系统通知设置
 */
export function updateSystemNotificationSettings(settings) {
  return request({
    url: '/admin/notifications/settings/system',
    method: 'put',
    data: settings
  })
}

/**
 * 获取用户通知设置
 */
export function getUserNotificationSettings(userId) {
  return request({
    url: `/admin/notifications/settings/user/${userId}`,
    method: 'get'
  })
}

/**
 * 更新用户通知设置
 */
export function updateUserNotificationSettings(userId, settings) {
  return request({
    url: `/admin/notifications/settings/user/${userId}`,
    method: 'put',
    data: settings
  })
}

// ==================== 通知导出 ====================

/**
 * 导出通知数据
 */
export function exportNotifications(params, format = 'excel') {
  return request({
    url: '/admin/notifications/export',
    method: 'post',
    data: params,
    params: { format },
    responseType: 'blob'
  })
}

// ==================== 实时通知 ====================

/**
 * 建立WebSocket连接获取实时通知
 */
export function connectNotificationWebSocket() {
  try {
    const token = localStorage.getItem('token')
    if (!token) {
      console.warn('未找到认证token，无法建立WebSocket连接')
      return null
    }

    // 导入SockJS和STOMP（需要在项目中安装这些依赖）
    const SockJS = window.SockJS
    const Stomp = window.Stomp

    if (!SockJS || !Stomp) {
      console.error('SockJS或STOMP库未加载，请确保已正确引入')
      return null
    }

    // 使用SockJS和STOMP协议连接
    const socket = new SockJS(`${process.env.VUE_APP_API_BASE_URL || 'http://localhost:8081'}/ws-notifications`)
    const stompClient = Stomp.over(socket)

    // 配置STOMP客户端
    stompClient.configure({
      connectHeaders: {
        'Authorization': `Bearer ${token}`
      },
      debug: (str) => {
        console.log('STOMP Debug:', str)
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000
    })

    // 连接WebSocket
    stompClient.connect(
      { 'Authorization': `Bearer ${token}` },
      (frame) => {
        console.log('管理端WebSocket连接成功:', frame)

        // 订阅个人通知队列
        const userId = getUserIdFromToken(token)
        if (userId) {
          stompClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
            try {
              const data = JSON.parse(message.body)
              console.log('收到WebSocket通知:', data)

              // 触发自定义事件
              window.dispatchEvent(new CustomEvent('notification-received', {
                detail: data
              }))
            } catch (error) {
              console.error('处理WebSocket消息失败:', error)
            }
          })

          // 订阅管理员广播通知
          stompClient.subscribe('/topic/admin-notifications', (message) => {
            try {
              const data = JSON.parse(message.body)
              console.log('收到管理员广播通知:', data)

              // 触发自定义事件
              window.dispatchEvent(new CustomEvent('admin-notification-received', {
                detail: data
              }))
            } catch (error) {
              console.error('处理管理员广播消息失败:', error)
            }
          })

          // 订阅系统统计更新
          stompClient.subscribe('/topic/system-stats', (message) => {
            try {
              const data = JSON.parse(message.body)
              console.log('收到系统统计更新:', data)

              // 触发自定义事件
              window.dispatchEvent(new CustomEvent('system-stats-updated', {
                detail: data
              }))
            } catch (error) {
              console.error('处理系统统计更新失败:', error)
            }
          })

          // 发送连接确认消息
          stompClient.send('/app/notifications/connect', {}, JSON.stringify({
            userId: userId,
            timestamp: Date.now()
          }))
        }
      },
      (error) => {
        console.error('管理端WebSocket连接失败:', error)
      }
    )

    return stompClient
  } catch (error) {
    console.error('创建管理端WebSocket连接失败:', error)
    return null
  }
}

// 从token中获取用户ID的辅助函数
function getUserIdFromToken(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.userId || payload.sub
  } catch (error) {
    console.error('解析token失败:', error)
    return null
  }
}
