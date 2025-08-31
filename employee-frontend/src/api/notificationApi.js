import request from '@/utils/request'

/**
 * 通知管理API
 */

// ==================== 基础通知操作 ====================

/**
 * 分页查询通知
 */
export function getNotificationList(params) {
  return request({
    url: '/api/notifications/page',
    method: 'get',
    params
  })
}

/**
 * 复杂条件查询通知
 */
export function searchNotifications(params) {
  return request({
    url: '/api/notifications/search',
    method: 'post',
    params
  })
}

/**
 * 获取未读通知
 */
export function getUnreadNotifications() {
  return request({
    url: '/api/notifications/unread',
    method: 'get'
  })
}

/**
 * 获取未读通知数量
 */
export function getUnreadCount() {
  return request({
    url: '/api/notifications/unread/count',
    method: 'get'
  })
}

/**
 * 获取通知统计
 */
export function getNotificationStats() {
  return request({
    url: '/api/notifications/stats',
    method: 'get'
  })
}

/**
 * 获取最近通知
 */
export function getRecentNotifications(limit = 10) {
  return request({
    url: '/api/notifications/recent',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取任务相关通知
 */
export function getTaskNotifications(taskId) {
  return request({
    url: `/api/notifications/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 获取系统通知
 */
export function getSystemNotices() {
  return request({
    url: '/api/notifications/system',
    method: 'get'
  })
}

/**
 * 获取紧急通知
 */
export function getUrgentNotifications() {
  return request({
    url: '/api/notifications/urgent',
    method: 'get'
  })
}

/**
 * 获取通知详情
 */
export function getNotificationDetail(id) {
  return request({
    url: `/api/notifications/${id}`,
    method: 'get'
  })
}

/**
 * 按类型获取通知
 */
export function getNotificationsByType(notificationType) {
  return request({
    url: `/api/notifications/type/${notificationType}`,
    method: 'get'
  })
}

// ==================== 通知操作 ====================

/**
 * 标记通知为已读
 */
export function markAsRead(id) {
  return request({
    url: `/api/notifications/${id}/read`,
    method: 'put'
  })
}

/**
 * 批量标记为已读
 */
export function batchMarkAsRead(notificationIds) {
  return request({
    url: '/api/notifications/batch/read',
    method: 'put',
    data: notificationIds
  })
}

/**
 * 标记所有未读通知为已读
 */
export function markAllAsRead() {
  return request({
    url: '/api/notifications/all/read',
    method: 'put'
  })
}

/**
 * 删除通知
 */
export function deleteNotification(id) {
  return request({
    url: `/api/notifications/${id}`,
    method: 'delete'
  })
}

/**
 * 批量删除通知
 */
export function batchDeleteNotifications(notificationIds) {
  return request({
    url: '/api/notifications/batch',
    method: 'delete',
    data: notificationIds
  })
}

/**
 * 检查是否有未读紧急通知
 */
export function hasUrgentUnreadNotifications() {
  return request({
    url: '/api/notifications/urgent/check',
    method: 'get'
  })
}

// ==================== 通知设置 ====================

/**
 * 获取通知设置
 */
export function getNotificationSettings() {
  return request({
    url: '/api/notifications/settings',
    method: 'get'
  })
}

/**
 * 更新通知设置
 */
export function updateNotificationSettings(settings) {
  return request({
    url: '/api/notifications/settings',
    method: 'put',
    data: settings
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
    const socket = new SockJS(`${API_BASE_URL}/ws-notifications`)
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
        console.log('WebSocket连接成功:', frame)

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

          // 订阅系统广播通知
          stompClient.subscribe('/topic/system-notifications', (message) => {
            try {
              const data = JSON.parse(message.body)
              console.log('收到系统广播通知:', data)

              // 触发自定义事件
              window.dispatchEvent(new CustomEvent('system-notification-received', {
                detail: data
              }))
            } catch (error) {
              console.error('处理系统广播消息失败:', error)
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
        console.error('WebSocket连接失败:', error)
      }
    )

    return stompClient
  } catch (error) {
    console.error('创建WebSocket连接失败:', error)
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

/**
 * 订阅通知类型
 */
export function subscribeNotificationType(notificationType) {
  return request({
    url: '/api/notifications/subscribe',
    method: 'post',
    data: { notificationType }
  })
}

/**
 * 取消订阅通知类型
 */
export function unsubscribeNotificationType(notificationType) {
  return request({
    url: '/api/notifications/unsubscribe',
    method: 'post',
    data: { notificationType }
  })
}

// ==================== 通知模板 ====================

/**
 * 获取通知模板
 */
export function getNotificationTemplates() {
  return request({
    url: '/api/notifications/templates',
    method: 'get'
  })
}

/**
 * 预览通知模板
 */
export function previewNotificationTemplate(templateId, data) {
  return request({
    url: `/api/notifications/templates/${templateId}/preview`,
    method: 'post',
    data
  })
}

// ==================== 通知历史 ====================

/**
 * 获取通知历史
 */
export function getNotificationHistory(params) {
  return request({
    url: '/api/notifications/history',
    method: 'get',
    params
  })
}

/**
 * 清理通知历史
 */
export function cleanNotificationHistory(days = 30) {
  return request({
    url: '/api/notifications/history/clean',
    method: 'delete',
    params: { days }
  })
}

// ==================== 通知导出 ====================

/**
 * 导出通知数据
 */
export function exportNotifications(params, format = 'excel') {
  return request({
    url: '/api/notifications/export',
    method: 'post',
    data: params,
    params: { format },
    responseType: 'blob'
  })
}
