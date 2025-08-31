<template>
  <div class="notification-detail">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h1>通知详情</h1>
      <div class="header-actions">
        <el-button type="success" @click="resendNotification" v-if="notification">重发</el-button>
        <el-button type="danger" @click="deleteNotification" v-if="notification">删除</el-button>
      </div>
    </div>

    <div class="detail-content" v-loading="loading">
      <div v-if="notification" class="notification-info">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card class="info-card">
              <template #header>
                <div class="card-header">
                  <span>通知内容</span>
                  <div class="notification-status">
                    <el-tag :type="notification.isRead ? 'success' : 'warning'">
                      {{ notification.isRead ? '已读' : '未读' }}
                    </el-tag>
                    <el-tag v-if="notification.priority === 'urgent'" type="danger">紧急</el-tag>
                  </div>
                </div>
              </template>
              
              <div class="notification-content">
                <h2 class="notification-title">{{ notification.title }}</h2>
                <div class="notification-body">{{ notification.content }}</div>
                
                <div class="notification-meta">
                  <div class="meta-item">
                    <span class="meta-label">通知类型：</span>
                    <el-tag :type="getNotificationTypeTag(notification.notificationType)">
                      {{ getNotificationTypeText(notification.notificationType) }}
                    </el-tag>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">发送时间：</span>
                    <span>{{ formatTime(notification.createTime) }}</span>
                  </div>
                  <div class="meta-item" v-if="notification.readTime">
                    <span class="meta-label">阅读时间：</span>
                    <span>{{ formatTime(notification.readTime) }}</span>
                  </div>
                  <div class="meta-item" v-if="notification.relatedType && notification.relatedId">
                    <span class="meta-label">相关内容：</span>
                    <el-link type="primary" @click="viewRelatedContent">
                      {{ notification.relatedType === 'task' ? '查看任务' : '查看相关内容' }}
                    </el-link>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>接收人信息</span>
              </template>
              <div class="recipient-info">
                <div class="recipient-item">
                  <el-avatar :size="40" :src="notification.recipientAvatar">
                    {{ notification.recipientName?.charAt(0) }}
                  </el-avatar>
                  <div class="recipient-details">
                    <div class="recipient-name">{{ notification.recipientName }}</div>
                    <div class="recipient-email">{{ notification.recipientEmail }}</div>
                    <div class="recipient-department">{{ notification.recipientDepartment }}</div>
                  </div>
                </div>
              </div>
            </el-card>
            
            <el-card class="info-card" style="margin-top: 20px;">
              <template #header>
                <span>发送统计</span>
              </template>
              <div class="send-stats">
                <div class="stat-item">
                  <span class="stat-label">发送状态：</span>
                  <el-tag type="success">已发送</el-tag>
                </div>
                <div class="stat-item">
                  <span class="stat-label">发送方式：</span>
                  <span>系统通知</span>
                </div>
                <div class="stat-item" v-if="notification.emailSent">
                  <span class="stat-label">邮件通知：</span>
                  <el-tag type="success">已发送</el-tag>
                </div>
                <div class="stat-item" v-if="notification.smsSent">
                  <span class="stat-label">短信通知：</span>
                  <el-tag type="success">已发送</el-tag>
                </div>
              </div>
            </el-card>
            
            <el-card class="info-card" style="margin-top: 20px;">
              <template #header>
                <span>操作记录</span>
              </template>
              <div class="operation-history">
                <div class="history-item">
                  <div class="history-time">{{ formatTime(notification.createTime) }}</div>
                  <div class="history-action">通知已发送</div>
                </div>
                <div class="history-item" v-if="notification.readTime">
                  <div class="history-time">{{ formatTime(notification.readTime) }}</div>
                  <div class="history-action">用户已阅读</div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
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
    }
  } catch (error) {
    console.error('加载通知详情失败:', error)
    ElMessage.error('加载通知详情失败')
  } finally {
    loading.value = false
  }
}

// 重发通知
const resendNotification = async () => {
  try {
    await ElMessageBox.confirm('确定要重新发送这条通知吗？', '确认重发', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const response = await notificationApi.resendNotification(route.params.id)
    if (response.code === 200) {
      ElMessage.success('通知重发成功')
      await loadNotificationDetail()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('重发通知失败:', error)
      ElMessage.error('重发通知失败')
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
    
    const response = await notificationApi.adminBatchDeleteNotifications([route.params.id])
    if (response.code === 200) {
      ElMessage.success('通知删除成功')
      router.push('/admin/notifications')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除通知失败:', error)
      ElMessage.error('删除通知失败')
    }
  }
}

// 查看相关内容
const viewRelatedContent = () => {
  if (notification.value.relatedType === 'task' && notification.value.relatedId) {
    router.push(`/admin/tasks/${notification.value.relatedId}`)
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 工具方法
const formatTime = (timeString) => {
  if (!timeString) return ''
  const date = new Date(timeString)
  return date.toLocaleString('zh-CN')
}

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
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.notification-status {
  display: flex;
  gap: 8px;
}

.notification-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.notification-body {
  margin-bottom: 20px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.notification-meta {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  font-weight: 500;
  color: #909399;
  margin-right: 8px;
  min-width: 80px;
}

.recipient-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.recipient-details {
  flex: 1;
}

.recipient-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.recipient-email,
.recipient-department {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.send-stats,
.operation-history {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  font-weight: 500;
  color: #909399;
}

.history-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.history-item:last-child {
  border-bottom: none;
}

.history-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.history-action {
  color: #606266;
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
