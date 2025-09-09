<template>
  <el-dialog
    v-model="dialogVisible"
    title="日程详情"
    width="600px"
    :close-on-click-modal="false"
  >
    <div v-loading="loading" class="schedule-detail">
      <template v-if="scheduleDetail">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h3 class="section-title">基本信息</h3>
          <div class="detail-grid">
            <div class="detail-item">
              <label>标题</label>
              <div class="detail-value">{{ scheduleDetail.title }}</div>
            </div>
            <div class="detail-item">
              <label>时间</label>
              <div class="detail-value">
                <div v-if="scheduleDetail.all_day" class="time-info">
                  <el-tag type="info" size="small">全天</el-tag>
                  {{ formatDate(scheduleDetail.start_time) }}
                </div>
                <div v-else class="time-info">
                  {{ formatDateTime(scheduleDetail.start_time) }} - {{ formatDateTime(scheduleDetail.end_time) }}
                  <span class="duration">({{ getDuration() }})</span>
                </div>
              </div>
            </div>
            <div class="detail-item" v-if="scheduleDetail.location">
              <label>地点</label>
              <div class="detail-value">
                <el-icon><Location /></el-icon>
                {{ scheduleDetail.location }}
              </div>
            </div>
            <div class="detail-item">
              <label>状态</label>
              <div class="detail-value">
                <el-tag :type="getStatusType(scheduleDetail.status)">
                  {{ getStatusText(scheduleDetail.status) }}
                </el-tag>
              </div>
            </div>
            <div class="detail-item">
              <label>优先级</label>
              <div class="detail-value">
                <el-tag :type="getPriorityType(scheduleDetail.priority)">
                  {{ getPriorityText(scheduleDetail.priority) }}
                </el-tag>
              </div>
            </div>
            <div class="detail-item" v-if="scheduleDetail.reminder_minutes">
              <label>提醒</label>
              <div class="detail-value">
                <el-icon><Bell /></el-icon>
                提前 {{ getReminderText(scheduleDetail.reminder_minutes) }}
              </div>
            </div>
          </div>
        </div>

        <!-- 描述 -->
        <div class="detail-section" v-if="scheduleDetail.description">
          <h3 class="section-title">描述</h3>
          <div class="description-content">
            {{ scheduleDetail.description }}
          </div>
        </div>

        <!-- 参与者 -->
        <div class="detail-section" v-if="participants.length > 0">
          <h3 class="section-title">参与者 ({{ participants.length }})</h3>
          <div class="participants-list">
            <div
              v-for="participant in participants"
              :key="participant.user_id"
              class="participant-item"
            >
              <el-avatar :size="32" :src="participant.avatar">
                {{ (participant.real_name || participant.username).charAt(0) }}
              </el-avatar>
              <div class="participant-info">
                <div class="participant-name">
                  {{ participant.real_name || participant.username }}
                  <el-tag
                    v-if="participant.role === 'ORGANIZER'"
                    type="warning"
                    size="small"
                  >
                    组织者
                  </el-tag>
                </div>
                <div class="participant-dept">{{ participant.department_name }}</div>
              </div>
              <div class="participant-status">
                <el-tag
                  :type="getParticipantStatusType(participant.status)"
                  size="small"
                >
                  {{ getParticipantStatusText(participant.status) }}
                </el-tag>
              </div>
            </div>
          </div>
        </div>

        <!-- 操作记录 -->
        <div class="detail-section">
          <h3 class="section-title">创建信息</h3>
          <div class="meta-info">
            <div class="meta-item">
              <span class="meta-label">创建时间：</span>
              <span class="meta-value">{{ formatDateTime(scheduleDetail.created_time) }}</span>
            </div>
            <div class="meta-item" v-if="scheduleDetail.updated_time !== scheduleDetail.created_time">
              <span class="meta-label">更新时间：</span>
              <span class="meta-value">{{ formatDateTime(scheduleDetail.updated_time) }}</span>
            </div>
            <div class="meta-item" v-if="scheduleDetail.is_private">
              <span class="meta-label">私人日程：</span>
              <el-tag type="info" size="small">仅自己可见</el-tag>
            </div>
          </div>
        </div>
      </template>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="canEdit"
          type="primary"
          @click="handleEdit"
        >
          编辑
        </el-button>
        <el-button
          v-if="canDelete"
          type="danger"
          @click="handleDelete"
        >
          删除
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Location, Bell } from '@element-plus/icons-vue'
import dayjs from 'dayjs'
import duration from 'dayjs/plugin/duration'
import { useScheduleStore } from '@/stores/schedule'
import { useUserStore } from '@/stores/user'

dayjs.extend(duration)

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  scheduleId: {
    type: [Number, String],
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'edit', 'delete'])

// Store
const scheduleStore = useScheduleStore()
const userStore = useUserStore()

// 响应式数据
const loading = ref(false)
const scheduleDetail = ref(null)
const participants = ref([])

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const canEdit = computed(() => {
  if (!scheduleDetail.value) return false
  const currentUserId = userStore.userInfo?.id
  return scheduleDetail.value.user_id === currentUserId || 
         scheduleDetail.value.created_by === currentUserId
})

const canDelete = computed(() => {
  if (!scheduleDetail.value) return false
  const currentUserId = userStore.userInfo?.id
  return scheduleDetail.value.user_id === currentUserId || 
         scheduleDetail.value.created_by === currentUserId
})

// 方法
const fetchScheduleDetail = async () => {
  if (!props.scheduleId) return
  
  try {
    loading.value = true
    const detail = await scheduleStore.fetchScheduleDetail(props.scheduleId)
    if (detail) {
      scheduleDetail.value = detail.schedule
      participants.value = detail.participants || []
    }
  } catch (error) {
    console.error('获取日程详情失败:', error)
    ElMessage.error('获取日程详情失败')
  } finally {
    loading.value = false
  }
}

const formatDate = (dateTime) => {
  return dayjs(dateTime).format('YYYY年MM月DD日')
}

const formatDateTime = (dateTime) => {
  return dayjs(dateTime).format('YYYY-MM-DD HH:mm')
}

const getDuration = () => {
  if (!scheduleDetail.value) return ''
  const start = dayjs(scheduleDetail.value.start_time)
  const end = dayjs(scheduleDetail.value.end_time)
  const diff = dayjs.duration(end.diff(start))
  
  const hours = diff.hours()
  const minutes = diff.minutes()
  
  if (hours > 0 && minutes > 0) {
    return `${hours}小时${minutes}分钟`
  } else if (hours > 0) {
    return `${hours}小时`
  } else {
    return `${minutes}分钟`
  }
}

const getStatusText = (status) => {
  const statusMap = {
    'SCHEDULED': '待开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消'
  }
  return statusMap[status] || status
}

const getStatusType = (status) => {
  const typeMap = {
    'SCHEDULED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success',
    'CANCELLED': 'danger'
  }
  return typeMap[status] || 'info'
}

const getPriorityText = (priority) => {
  const priorityMap = {
    'LOW': '低',
    'MEDIUM': '中',
    'HIGH': '高',
    'URGENT': '紧急'
  }
  return priorityMap[priority] || priority
}

const getPriorityType = (priority) => {
  const typeMap = {
    'LOW': 'success',
    'MEDIUM': 'warning',
    'HIGH': 'danger',
    'URGENT': 'danger'
  }
  return typeMap[priority] || 'info'
}

const getReminderText = (minutes) => {
  if (minutes < 60) {
    return `${minutes}分钟`
  } else if (minutes < 1440) {
    const hours = Math.floor(minutes / 60)
    return `${hours}小时`
  } else {
    const days = Math.floor(minutes / 1440)
    return `${days}天`
  }
}

const getParticipantStatusText = (status) => {
  const statusMap = {
    'PENDING': '待回复',
    'ACCEPTED': '已接受',
    'DECLINED': '已拒绝',
    'TENTATIVE': '待定'
  }
  return statusMap[status] || status
}

const getParticipantStatusType = (status) => {
  const typeMap = {
    'PENDING': 'info',
    'ACCEPTED': 'success',
    'DECLINED': 'danger',
    'TENTATIVE': 'warning'
  }
  return typeMap[status] || 'info'
}

const handleEdit = () => {
  emit('edit', scheduleDetail.value)
}

const handleDelete = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个日程吗？', '确认删除', {
      type: 'warning'
    })
    
    emit('delete', props.scheduleId)
  } catch (error) {
    // 用户取消删除
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

// 监听器
watch(() => props.modelValue, (visible) => {
  if (visible && props.scheduleId) {
    fetchScheduleDetail()
  } else {
    scheduleDetail.value = null
    participants.value = []
  }
})

watch(() => props.scheduleId, (newId) => {
  if (newId && props.modelValue) {
    fetchScheduleDetail()
  }
})
</script>

<style scoped>
.schedule-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 8px;
}

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.detail-item label {
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.detail-value {
  font-size: 14px;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 6px;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.duration {
  font-size: 12px;
  color: #909399;
}

.description-content {
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.6;
  color: #303133;
  white-space: pre-wrap;
}

.participants-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.participant-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.participant-info {
  flex: 1;
}

.participant-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.participant-dept {
  font-size: 12px;
  color: #909399;
}

.participant-status {
  display: flex;
  align-items: center;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.meta-label {
  color: #909399;
  min-width: 80px;
}

.meta-value {
  color: #303133;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>