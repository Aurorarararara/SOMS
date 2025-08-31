<template>
  <div class="notification-center">
    <!-- 通知铃铛图标 -->
    <el-popover
      placement="bottom-end"
      :width="400"
      trigger="click"
      popper-class="notification-popover"
    >
      <template #reference>
        <div class="notification-trigger" @click="loadNotifications">
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
            <el-icon :size="20" class="notification-icon">
              <Bell />
            </el-icon>
          </el-badge>
        </div>
      </template>

      <!-- 通知内容 -->
      <div class="notification-content">
        <!-- 头部 -->
        <div class="notification-header">
          <div class="header-title">
            <span>通知中心</span>
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" type="danger" />
          </div>
          <div class="header-actions">
            <el-button type="text" size="small" @click="markAllAsRead" :disabled="unreadCount === 0">
              全部已读
            </el-button>
            <el-button type="text" size="small" @click="openNotificationManagement">
              管理
            </el-button>
          </div>
        </div>

        <!-- 通知列表 -->
        <div class="notification-list" v-loading="loading">
          <div v-if="notifications.length === 0" class="empty-state">
            <el-icon :size="48" class="empty-icon"><Bell /></el-icon>
            <p>暂无通知</p>
          </div>
          
          <div
            v-for="notification in notifications"
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': notification.isUnread }"
            @click="handleNotificationClick(notification)"
          >
            <div class="notification-avatar">
              <el-icon :size="16" :class="getNotificationIconClass(notification.notificationType)">
                <component :is="getNotificationIcon(notification.notificationType)" />
              </el-icon>
            </div>
            
            <div class="notification-body">
              <div class="notification-title">{{ notification.title }}</div>
              <div class="notification-content-text">{{ notification.content }}</div>
              <div class="notification-meta">
                <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
                <el-tag v-if="notification.priority === 'urgent'" type="danger" size="small">紧急</el-tag>
              </div>
            </div>
            
            <div class="notification-actions">
              <el-button
                v-if="notification.isUnread"
                type="text"
                size="small"
                @click.stop="markAsRead(notification.id)"
              >
                标记已读
              </el-button>
              <el-button
                type="text"
                size="small"
                @click.stop="deleteNotification(notification.id)"
              >
                删除
              </el-button>
            </div>
          </div>
        </div>

        <!-- 底部 -->
        <div class="notification-footer">
          <el-button type="text" @click="openNotificationManagement" style="width: 100%">
            查看全部通知
          </el-button>
        </div>
      </div>
    </el-popover>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Bell, 
  Message, 
  Warning, 
  Check, 
  InfoFilled,
  User,
  Document
} from '@element-plus/icons-vue'
import * as notificationApi from '@/api/notificationApi'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const notifications = ref([])
const unreadCount = ref(0)
let websocket = null
let pollingTimer = null

// 加载通知数据
const loadNotifications = async () => {
  loading.value = true
  try {
    // 加载最近通知
    const response = await notificationApi.getRecentNotifications(10)
    if (response.code === 200) {
      notifications.value = response.data || []
    }
    
    // 加载未读数量
    const countResponse = await notificationApi.getUnreadCount()
    if (countResponse.code === 200) {
      unreadCount.value = countResponse.data || 0
    }
  } catch (error) {
    console.error('加载通知失败:', error)
  } finally {
    loading.value = false
  }
}

// 标记单个通知为已读
const markAsRead = async (notificationId) => {
  try {
    const response = await notificationApi.markAsRead(notificationId)
    if (response.code === 200) {
      // 更新本地状态
      const notification = notifications.value.find(n => n.id === notificationId)
      if (notification) {
        notification.isRead = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    ElMessage.error('标记已读失败')
  }
}

// 标记所有通知为已读
const markAllAsRead = async () => {
  try {
    const response = await notificationApi.markAllAsRead()
    if (response.code === 200) {
      // 更新本地状态
      notifications.value.forEach(notification => {
        notification.isRead = true
      })
      unreadCount.value = 0
      ElMessage.success('已标记所有通知为已读')
    }
  } catch (error) {
    console.error('标记全部已读失败:', error)
    ElMessage.error('标记全部已读失败')
  }
}

// 删除通知
const deleteNotification = async (notificationId) => {
  try {
    const response = await notificationApi.deleteNotification(notificationId)
    if (response.code === 200) {
      // 从列表中移除
      const index = notifications.value.findIndex(n => n.id === notificationId)
      if (index > -1) {
        const notification = notifications.value[index]
        if (notification.isUnread) {
          unreadCount.value = Math.max(0, unreadCount.value - 1)
        }
        notifications.value.splice(index, 1)
      }
      ElMessage.success('通知已删除')
    }
  } catch (error) {
    console.error('删除通知失败:', error)
    ElMessage.error('删除通知失败')
  }
}

// 处理通知点击
const handleNotificationClick = async (notification) => {
  // 如果未读，先标记为已读
  if (notification.isUnread) {
    await markAsRead(notification.id)
  }
  
  // 根据通知类型跳转到相应页面
  if (notification.relatedType === 'task' && notification.relatedId) {
    router.push(`/tasks/${notification.relatedId}`)
  }
}

// 打开通知管理页面
const openNotificationManagement = () => {
  router.push('/notifications')
}

// 获取通知图标
const getNotificationIcon = (type) => {
  const icons = {
    'task_assigned': Document,
    'task_updated': Document,
    'task_completed': Check,
    'task_overdue': Warning,
    'task_commented': Message,
    'system_notice': InfoFilled,
    'user_mention': User
  }
  return icons[type] || Bell
}

// 获取通知图标样式类
const getNotificationIconClass = (type) => {
  const classes = {
    'task_assigned': 'icon-primary',
    'task_updated': 'icon-warning',
    'task_completed': 'icon-success',
    'task_overdue': 'icon-danger',
    'task_commented': 'icon-info',
    'system_notice': 'icon-info',
    'user_mention': 'icon-primary'
  }
  return classes[type] || 'icon-default'
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  
  const time = new Date(timeString)
  const now = new Date()
  const diff = now - time
  
  const minutes = Math.floor(diff / (1000 * 60))
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return time.toLocaleDateString('zh-CN')
}

// 初始化WebSocket连接
const initWebSocket = () => {
  try {
    websocket = notificationApi.connectNotificationWebSocket()
    
    websocket.onopen = () => {
      console.log('通知WebSocket连接已建立')
    }
    
    websocket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type === 'notification') {
          // 新通知到达
          notifications.value.unshift(data.notification)
          unreadCount.value += 1
          
          // 显示桌面通知
          if (Notification.permission === 'granted') {
            new Notification(data.notification.title, {
              body: data.notification.content,
              icon: '/favicon.ico'
            })
          }
        }
      } catch (error) {
        console.error('处理WebSocket消息失败:', error)
      }
    }
    
    websocket.onerror = (error) => {
      console.error('WebSocket连接错误:', error)
    }
    
    websocket.onclose = () => {
      console.log('WebSocket连接已关闭')
      // 5秒后重连
      setTimeout(initWebSocket, 5000)
    }
  } catch (error) {
    console.error('初始化WebSocket失败:', error)
    // 降级到轮询模式
    startPolling()
  }
}

// 开始轮询
const startPolling = () => {
  pollingTimer = setInterval(async () => {
    try {
      const response = await notificationApi.getUnreadCount()
      if (response.code === 200) {
        const newCount = response.data || 0
        if (newCount > unreadCount.value) {
          // 有新通知，重新加载
          await loadNotifications()
        }
      }
    } catch (error) {
      console.error('轮询通知失败:', error)
    }
  }, 30000) // 30秒轮询一次
}

// 请求桌面通知权限
const requestNotificationPermission = () => {
  if ('Notification' in window && Notification.permission === 'default') {
    Notification.requestPermission()
  }
}

onMounted(async () => {
  await loadNotifications()
  requestNotificationPermission()
  initWebSocket()
})

onUnmounted(() => {
  if (websocket) {
    websocket.close()
  }
  if (pollingTimer) {
    clearInterval(pollingTimer)
  }
})
</script>

<style scoped>
.notification-center {
  position: relative;
}

.notification-trigger {
  cursor: pointer;
  padding: 8px;
  border-radius: 6px;
  transition: background-color 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.notification-trigger:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

.notification-icon {
  color: #606266;
}

.notification-content {
  max-height: 500px;
  display: flex;
  flex-direction: column;
}

.notification-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 12px 16px;
  border-bottom: 1px solid #ebeef5;
}

.header-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 8px;
}

.notification-list {
  flex: 1;
  max-height: 400px;
  overflow-y: auto;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-icon {
  margin-bottom: 12px;
  opacity: 0.5;
}

.notification-item {
  display: flex;
  padding: 12px 16px;
  border-bottom: 1px solid #f5f7fa;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #f0f9ff;
  border-left: 3px solid #409eff;
}

.notification-item.unread::before {
  content: '';
  position: absolute;
  left: 8px;
  top: 16px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #409eff;
}

.notification-avatar {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  flex-shrink: 0;
}

.notification-avatar .icon-primary {
  color: #409eff;
  background-color: #ecf5ff;
}

.notification-avatar .icon-success {
  color: #67c23a;
  background-color: #f0f9ff;
}

.notification-avatar .icon-warning {
  color: #e6a23c;
  background-color: #fdf6ec;
}

.notification-avatar .icon-danger {
  color: #f56c6c;
  background-color: #fef0f0;
}

.notification-avatar .icon-info {
  color: #909399;
  background-color: #f4f4f5;
}

.notification-avatar .icon-default {
  color: #606266;
  background-color: #f5f7fa;
}

.notification-body {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-content-text {
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  margin-bottom: 6px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.notification-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.notification-time {
  font-size: 12px;
  color: #c0c4cc;
}

.notification-actions {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 8px;
  opacity: 0;
  transition: opacity 0.2s;
}

.notification-item:hover .notification-actions {
  opacity: 1;
}

.notification-footer {
  padding: 12px 16px;
  border-top: 1px solid #ebeef5;
  background-color: #fafafa;
}
</style>

<style>
.notification-popover {
  padding: 0 !important;
}
</style>
