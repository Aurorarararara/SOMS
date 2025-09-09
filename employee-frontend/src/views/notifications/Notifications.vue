<template>
  <div class="notifications-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">{{ $t('notifications.title') }}</h1>
          <p class="page-subtitle">{{ $t('notifications.subtitle') }}</p>
        </div>
        <div class="header-right">
          <el-button @click="markAllAsRead" :disabled="unreadCount === 0">
            <el-icon><Check /></el-icon>
            {{ $t('notifications.markAllRead') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 通知统计 -->
    <div class="stats-section">
      <div class="stats-grid">
        <div class="stat-card total">
          <div class="stat-icon">
            <el-icon><Bell /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">{{ $t('notifications.allNotifications') }}</div>
            <div class="stat-value">{{ totalCount }}</div>
          </div>
        </div>
        <div class="stat-card unread">
          <div class="stat-icon">
            <el-icon><Message /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">{{ $t('notifications.unreadNotifications') }}</div>
            <div class="stat-value">{{ unreadCount }}</div>
          </div>
        </div>
        <div class="stat-card important">
          <div class="stat-icon">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-title">{{ $t('notifications.importantNotifications') }}</div>
            <div class="stat-value">{{ importantCount }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 筛选区域 -->
    <div class="filter-section">
      <div class="filter-card">
        <div class="filter-content">
          <div class="filter-item">
            <span class="filter-label">{{ $t('notifications.status') }}：</span>
            <el-radio-group v-model="filterStatus" @change="loadNotifications">
              <el-radio-button label="">{{ $t('common.all') }}</el-radio-button>
              <el-radio-button label="unread">{{ $t('notifications.unread') }}</el-radio-button>
              <el-radio-button label="read">{{ $t('notifications.read') }}</el-radio-button>
            </el-radio-group>
          </div>
          <div class="filter-item">
            <span class="filter-label">{{ $t('notifications.level') }}：</span>
            <el-select v-model="filterLevel" :placeholder="$t('notifications.selectLevel')" clearable @change="loadNotifications">
              <el-option :label="$t('notifications.urgent')" value="紧急" />
              <el-option :label="$t('notifications.important')" value="重要" />
              <el-option :label="$t('notifications.normal')" value="一般" />
            </el-select>
          </div>
          <div class="filter-item">
            <span class="filter-label">{{ $t('notifications.type') }}：</span>
            <el-select v-model="filterType" :placeholder="$t('notifications.selectType')" clearable @change="loadNotifications">
              <el-option :label="$t('notifications.systemNotification')" value="系统通知" />
              <el-option :label="$t('notifications.companyAnnouncement')" value="公司公告" />
              <el-option :label="$t('notifications.departmentNotification')" value="部门通知" />
              <el-option :label="$t('notifications.personalMessage')" value="个人消息" />
            </el-select>
          </div>
        </div>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="notifications-section">
      <div class="notifications-card">
        <div v-loading="loading" class="notifications-list">
          <div 
            v-for="notification in notifications" 
            :key="notification.id"
            class="notification-item"
            :class="{ 'unread': !notification.isRead, 'important': notification.level === '紧急' }"
            @click="viewNotification(notification)"
          >
            <div class="notification-indicator">
              <div 
                class="status-dot" 
                :class="{ 'unread': !notification.isRead }"
              ></div>
            </div>
            
            <div class="notification-content">
              <div class="notification-header">
                <div class="notification-title">
                  <span class="title-text">{{ notification.title }}</span>
                  <el-tag 
                    :type="getLevelTagType(notification.level)" 
                    size="small"
                    class="level-tag"
                  >
                    {{ notification.level }}
                  </el-tag>
                </div>
                <div class="notification-meta">
                  <el-tag type="info" size="small">{{ notification.type }}</el-tag>
                  <span class="notification-time">{{ formatTime(notification.createTime) }}</span>
                </div>
              </div>
              
              <div class="notification-summary">
                {{ truncateText(notification.content, 120) }}
              </div>
              
              <div class="notification-actions" @click.stop>
                <el-button 
                  text 
                  type="primary" 
                  size="small"
                  @click="viewNotification(notification)"
                >
                  查看详情
                </el-button>
                <el-button 
                  text 
                  :type="notification.isRead ? 'info' : 'warning'" 
                  size="small"
                  @click="toggleReadStatus(notification)"
                >
                  {{ notification.isRead ? '标记未读' : '标记已读' }}
                </el-button>
                <el-button 
                  text 
                  type="danger" 
                  size="small"
                  @click="deleteNotification(notification)"
                >
                  删除
                </el-button>
              </div>
            </div>
          </div>
          
          <div v-if="notifications.length === 0" class="empty-notifications">
            <el-icon><DocumentRemove /></el-icon>
            <p>暂无通知</p>
          </div>
        </div>

        <div class="pagination-wrapper" v-if="pagination.total > 0">
          <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="loadNotifications"
            @current-change="loadNotifications"
          />
        </div>
      </div>
    </div>

    <!-- 通知详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="selectedNotification?.title"
      width="700px"
      @close="selectedNotification = null"
    >
      <div class="notification-detail" v-if="selectedNotification">
        <div class="detail-header">
          <div class="detail-meta">
            <el-tag :type="getLevelTagType(selectedNotification.level)">
              {{ selectedNotification.level }}
            </el-tag>
            <el-tag type="info">{{ selectedNotification.type }}</el-tag>
            <span class="detail-time">{{ formatDateTime(selectedNotification.createTime) }}</span>
          </div>
          <div class="detail-actions">
            <el-button 
              :type="selectedNotification.isRead ? 'info' : 'warning'" 
              size="small"
              @click="toggleReadStatus(selectedNotification)"
            >
              {{ selectedNotification.isRead ? '标记未读' : '标记已读' }}
            </el-button>
          </div>
        </div>
        
        <div class="detail-content">
          <div class="content-section">
            <h4>发布人员</h4>
            <div class="publisher-info">
              <el-avatar :size="40" :src="selectedNotification.publisher?.avatar">
                {{ selectedNotification.publisher?.name?.charAt(0) }}
              </el-avatar>
              <div class="publisher-details">
                <div class="publisher-name">{{ selectedNotification.publisher?.name }}</div>
                <div class="publisher-dept">{{ selectedNotification.publisher?.department }}</div>
              </div>
            </div>
          </div>
          
          <div class="content-section">
            <h4>通知内容</h4>
            <div class="content-text" v-html="selectedNotification.content"></div>
          </div>
          
          <div class="content-section" v-if="selectedNotification.attachments?.length">
            <h4>相关附件</h4>
            <div class="attachments-list">
              <div 
                v-for="attachment in selectedNotification.attachments" 
                :key="attachment.id"
                class="attachment-item"
                @click="downloadAttachment(attachment)"
              >
                <el-icon><Document /></el-icon>
                <span>{{ attachment.name }}</span>
                <el-button text type="primary" size="small">下载</el-button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check, Bell, Message, Warning, DocumentRemove, Document
} from '@element-plus/icons-vue'
import { announcementApi } from '@/api/announcement'

const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const showDetailDialog = ref(false)
const selectedNotification = ref(null)
const notifications = ref([])
const filterStatus = ref('')
const filterLevel = ref('')
const filterType = ref('')

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
})

// 计算属性
const totalCount = computed(() => notifications.value.length)
const unreadCount = computed(() => notifications.value.filter(n => !n.isRead).length)
const importantCount = computed(() => notifications.value.filter(n => n.level === '紧急').length)

// 方法
const loadNotifications = async () => {
  try {
    loading.value = true
    const params = {
      current: pagination.current,
      size: pagination.size,
      status: filterStatus.value,
      level: filterLevel.value,
      type: filterType.value
    }
    
    const { data } = await announcementApi.getList(params)
    notifications.value = data.records || mockNotifications()
    pagination.total = data.total || notifications.value.length
  } catch (error) {
    console.error('加载通知失败:', error)
    notifications.value = mockNotifications()
    pagination.total = notifications.value.length
  } finally {
    loading.value = false
  }
}

const mockNotifications = () => {
  return [
    {
      id: 1,
      title: '系统维护通知',
      content: '为了提升系统性能，将于本周末进行系统维护，预计维护时间为2小时，期间系统可能无法正常访问，请大家提前做好相关准备工作。',
      level: '重要',
      type: '系统通知',
      isRead: false,
      createTime: '2024-01-15T10:00:00',
      publisher: {
        name: '系统管理员',
        department: 'IT部门',
        avatar: ''
      }
    },
    {
      id: 2,
      title: '年度总结会议通知',
      content: '各部门请准备年度工作总结，会议将于下周一召开，请相关人员务必参加。',
      level: '紧急',
      type: '公司公告',
      isRead: true,
      createTime: '2024-01-14T14:30:00',
      publisher: {
        name: '人事部',
        department: '人力资源部',
        avatar: ''
      }
    },
    {
      id: 3,
      title: '新员工入职培训',
      content: '本月新入职员工请参加入职培训，培训时间为本周三至周五，地址在会议室A。',
      level: '一般',
      type: '部门通知',
      isRead: false,
      createTime: '2024-01-13T09:15:00',
      publisher: {
        name: '张经理',
        department: '人力资源部',
        avatar: ''
      }
    }
  ]
}

const viewNotification = async (notification) => {
  selectedNotification.value = notification
  showDetailDialog.value = true
  
  if (!notification.isRead) {
    await markAsRead(notification)
  }
}

const markAsRead = async (notification) => {
  try {
    await announcementApi.markAsRead(notification.id)
    notification.isRead = true
  } catch (error) {
    console.error('标记已读失败:', error)
  }
}

const markAllAsRead = async () => {
  try {
    await announcementApi.markAllAsRead()
    notifications.value.forEach(n => n.isRead = true)
    ElMessage.success('已全部标记为已读')
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

const toggleReadStatus = async (notification) => {
  try {
    if (notification.isRead) {
      await announcementApi.markAsUnread(notification.id)
      notification.isRead = false
      ElMessage.success('已标记为未读')
    } else {
      await announcementApi.markAsRead(notification.id)
      notification.isRead = true
      ElMessage.success('已标记为已读')
    }
  } catch (error) {
    ElMessage.error('操作失败，请重试')
  }
}

const deleteNotification = async (notification) => {
  try {
    await ElMessageBox.confirm('确认删除此通知吗？', '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await announcementApi.deleteNotification(notification.id)
    notifications.value = notifications.value.filter(n => n.id !== notification.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败，请重试')
    }
  }
}

const downloadAttachment = (attachment) => {
  ElMessage.info(`下载附件: ${attachment.name}`)
}

const getLevelTagType = (level) => {
  const types = {
    '紧急': 'danger',
    '重要': 'warning',
    '一般': 'info'
  }
  return types[level] || 'info'
}

const truncateText = (text, maxLength) => {
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

const formatTime = (time) => {
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)}天前`
  
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const formatDateTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 生命周期
onMounted(() => {
  loadNotifications()
})
</script>

<style scoped>
.notifications-container {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  margin-bottom: 24px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.stats-section {
  margin-bottom: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.stat-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-card.total .stat-icon { background: linear-gradient(135deg, #667eea, #764ba2); }
.stat-card.unread .stat-icon { background: linear-gradient(135deg, #fa709a, #fee140); }
.stat-card.important .stat-icon { background: linear-gradient(135deg, #f093fb, #f5576c); }

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.filter-section {
  margin-bottom: 24px;
}

.filter-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.filter-content {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.filter-label {
  font-size: 14px;
  color: #666;
  white-space: nowrap;
}

.notifications-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.notification-item {
  display: flex;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 8px;
  margin-bottom: 8px;
}

.notification-item:hover {
  background: #f8f9fa;
}

.notification-item.unread {
  background: #f6ffed;
  border-left: 4px solid #52c41a;
}

.notification-item.important {
  border-left: 4px solid #f5222d;
}

.notification-indicator {
  margin-right: 16px;
  display: flex;
  align-items: flex-start;
  padding-top: 2px;
}

.status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #d9d9d9;
}

.status-dot.unread {
  background: #52c41a;
}

.notification-content {
  flex: 1;
}

.notification-header {
  margin-bottom: 12px;
}

.notification-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.title-text {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.level-tag {
  flex-shrink: 0;
}

.notification-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-summary {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.notification-actions {
  display: flex;
  gap: 8px;
}

.empty-notifications {
  text-align: center;
  padding: 64px 16px;
  color: #999;
}

.empty-notifications .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;
}

.notification-detail .detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.detail-time {
  font-size: 14px;
  color: #666;
}

.content-section {
  margin-bottom: 24px;
}

.content-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 12px 0;
}

.publisher-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.publisher-name {
  font-size: 14px;
  font-weight: 600;
  color: #1a1a1a;
}

.publisher-dept {
  font-size: 12px;
  color: #666;
}

.content-text {
  font-size: 14px;
  line-height: 1.8;
  color: #1a1a1a;
}

.attachments-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: #f5f5f5;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.attachment-item:hover {
  background: #e6f7ff;
}

@media (max-width: 1200px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .notifications-container {
    padding: 16px;
  }
  
  .header-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .filter-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .notification-item {
    padding: 16px;
  }
  
  .notification-title {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .notification-actions {
    flex-wrap: wrap;
  }
}
</style>