<template>
  <div class="notification-detail">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h1>通知详情</h1>
    </div>

    <div class="notification-content" v-loading="loading">
      <div v-if="notification" class="notification-card">
        <div class="notification-header">
          <div class="notification-title">
            <h2>{{ notification.title }}</h2>
            <el-tag v-if="notification.priority === 'urgent'" type="danger">紧急</el-tag>
          </div>
          <div class="notification-meta">
            <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
            <el-tag :type="getNotificationTypeTag(notification.notificationType)">
              {{ getNotificationTypeText(notification.notificationType) }}
            </el-tag>
          </div>
        </div>

        <div class="notification-body">
          <div class="notification-content-text">
            {{ notification.content }}
          </div>
        </div>

        <div class="notification-actions">
          <el-button v-if="notification.isUnread" type="primary" @click="markAsRead">
            标记为已读
          </el-button>
          <el-button @click="deleteNotification" type="danger">删除</el-button>
        </div>
      </div>

      <div v-else class="empty-state">
        <el-icon :size="64"><Bell /></el-icon>
        <p>通知不存在或已被删除</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Bell } from '@element-plus/icons-vue'
import * as notificationApi from '@/api/notificationApi'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const notification = ref(null)

// 加载通知详情
const loadNotificationDetail = async () => {
  loading.value = true
  try {
    const response = await notificationApi.getNotificationDetail(route.params.id)
    if (response.code === 200) {
      notification.value = response.data
      // 如果是未读通知，自动标记为已读
      if (notification.value.isUnread) {
        await markAsRead(false)
      }
    }
  } catch (error) {
    console.error('加载通知详情失败:', error)
    ElMessage.error('加载通知详情失败')
  } finally {
    loading.value = false
  }
}

// 标记为已读
const markAsRead = async (showMessage = true) => {
  try {
    const response = await notificationApi.markAsRead(route.params.id)
    if (response.code === 200) {
      if (notification.value) {
        notification.value.isUnread = false
      }
      if (showMessage) {
        ElMessage.success('已标记为已读')
      }
    }
  } catch (error) {
    console.error('标记已读失败:', error)
    if (showMessage) {
      ElMessage.error('标记已读失败')
    }
  }
}

// 删除通知
const deleteNotification = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这条通知吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const response = await notificationApi.deleteNotification(route.params.id)
    if (response.code === 200) {
      ElMessage.success('通知已删除')
      goBack()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
    }
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 格式化时间
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

// 获取通知类型标签
const getNotificationTypeTag = (type) => {
  const tags = {
    'task_assigned': 'primary',
    'task_updated': 'warning',
    'task_completed': 'success',
    'task_overdue': 'danger',
    'task_commented': 'info',
    'system_notice': 'info',
    'user_mention': 'primary'
  }
  return tags[type] || 'info'
}

// 获取通知类型文本
const getNotificationTypeText = (type) => {
  const texts = {
    'task_assigned': '任务分配',
    'task_updated': '任务更新',
    'task_completed': '任务完成',
    'task_overdue': '任务逾期',
    'task_commented': '任务评论',
    'system_notice': '系统通知',
    'user_mention': '用户提及'
  }
  return texts[type] || '未知类型'
}

onMounted(() => {
  loadNotificationDetail()
})
</script>

<style scoped>
.notification-detail {
  padding: 24px;
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.notification-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.notification-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.notification-title h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-time {
  color: #909399;
  font-size: 14px;
}

.notification-body {
  margin-bottom: 24px;
}

.notification-content-text {
  font-size: 16px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.notification-actions {
  display: flex;
  gap: 12px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state p {
  margin-top: 16px;
  font-size: 16px;
}
</style>
