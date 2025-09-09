<template>
  <div class="meeting-list-content">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="meetings.length === 0" class="empty-container">
      <el-empty description="暂无会议" />
    </div>
    
    <div v-else class="meetings-grid">
      <div 
        v-for="meeting in meetings" 
        :key="meeting.id"
        class="meeting-card"
      >
        <div class="meeting-header">
          <h4 class="meeting-title">{{ meeting.title }}</h4>
          <div class="meeting-actions">
            <el-dropdown trigger="click" @command="handleAction">
              <el-button size="small" text>
                <el-icon><MoreFilled /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item 
                    v-if="canJoin(meeting)" 
                    :command="{ action: 'join', meeting }"
                  >
                    <el-icon><VideoCamera /></el-icon>
                    加入会议
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="canEdit(meeting)" 
                    :command="{ action: 'edit', meeting }"
                  >
                    <el-icon><Edit /></el-icon>
                    编辑会议
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="canDelete(meeting)" 
                    :command="{ action: 'delete', meeting }"
                    divided
                  >
                    <el-icon><Delete /></el-icon>
                    删除会议
                  </el-dropdown-item>
                  <el-dropdown-item 
                    v-if="type === 'history'" 
                    :command="{ action: 'view', meeting }"
                  >
                    <el-icon><View /></el-icon>
                    查看详情
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
        
        <div class="meeting-info">
          <div class="info-item">
            <el-icon><Clock /></el-icon>
            <span>{{ formatMeetingTime(meeting) }}</span>
          </div>
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>{{ meeting.participantCount || 0 }} 人参与</span>
          </div>
          <div class="info-item" v-if="meeting.duration">
            <el-icon><Timer /></el-icon>
            <span>{{ formatDuration(meeting.duration) }}</span>
          </div>
        </div>
        
        <div class="meeting-status">
          <el-tag 
            :type="getStatusType(meeting.status)"
            size="small"
          >
            {{ getStatusText(meeting.status) }}
          </el-tag>
          <el-tag 
            v-if="meeting.isRecurring"
            type="info"
            size="small"
          >
            重复会议
          </el-tag>
        </div>
        
        <div class="meeting-description" v-if="meeting.description">
          <p>{{ meeting.description }}</p>
        </div>
        
        <div class="meeting-footer">
          <div class="meeting-host">
            <el-avatar :size="24" :src="meeting.hostAvatar">
              {{ meeting.hostName?.charAt(0) }}
            </el-avatar>
            <span class="host-name">{{ meeting.hostName }}</span>
          </div>
          
          <div class="meeting-actions-primary">
            <el-button 
              v-if="canJoin(meeting)"
              type="primary"
              size="small"
              @click="$emit('join', meeting)"
            >
              <el-icon><VideoCamera /></el-icon>
              加入
            </el-button>
            <el-button 
              v-else-if="type === 'history'"
              size="small"
              @click="$emit('view', meeting)"
            >
              <el-icon><View /></el-icon>
              查看
            </el-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { computed } from 'vue'
import { 
  MoreFilled, VideoCamera, Edit, Delete, View, 
  Clock, User, Timer 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

export default {
  name: 'MeetingListContent',
  components: {
    MoreFilled, VideoCamera, Edit, Delete, View,
    Clock, User, Timer
  },
  props: {
    meetings: {
      type: Array,
      default: () => []
    },
    loading: {
      type: Boolean,
      default: false
    },
    type: {
      type: String,
      default: 'upcoming' // upcoming, my, history
    }
  },
  emits: ['join', 'edit', 'delete', 'view'],
  setup(props, { emit }) {
    const userStore = useUserStore()
    const currentUser = computed(() => userStore.userInfo)
    
    // 格式化会议时间
    const formatMeetingTime = (meeting) => {
      const start = new Date(meeting.startTime)
      const end = meeting.endTime ? new Date(meeting.endTime) : null
      
      const today = new Date()
      const tomorrow = new Date(today)
      tomorrow.setDate(today.getDate() + 1)
      
      let dateStr = ''
      if (start.toDateString() === today.toDateString()) {
        dateStr = '今天'
      } else if (start.toDateString() === tomorrow.toDateString()) {
        dateStr = '明天'
      } else {
        dateStr = start.toLocaleDateString()
      }
      
      const timeStr = start.toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit'
      })
      
      if (end) {
        const endTimeStr = end.toLocaleTimeString('zh-CN', {
          hour: '2-digit',
          minute: '2-digit'
        })
        return `${dateStr} ${timeStr} - ${endTimeStr}`
      }
      
      return `${dateStr} ${timeStr}`
    }
    
    // 格式化持续时间
    const formatDuration = (minutes) => {
      if (minutes < 60) {
        return `${minutes} 分钟`
      }
      const hours = Math.floor(minutes / 60)
      const mins = minutes % 60
      return mins > 0 ? `${hours} 小时 ${mins} 分钟` : `${hours} 小时`
    }
    
    // 获取状态类型
    const getStatusType = (status) => {
      const statusMap = {
        'scheduled': 'info',
        'ongoing': 'success',
        'ended': 'info',
        'cancelled': 'danger'
      }
      return statusMap[status] || 'info'
    }
    
    // 获取状态文本
    const getStatusText = (status) => {
      const statusMap = {
        'scheduled': '已安排',
        'ongoing': '进行中',
        'ended': '已结束',
        'cancelled': '已取消'
      }
      return statusMap[status] || '未知'
    }
    
    // 判断是否可以加入
    const canJoin = (meeting) => {
      return meeting.status === 'scheduled' || meeting.status === 'ongoing'
    }
    
    // 判断是否可以编辑
    const canEdit = (meeting) => {
      return meeting.hostId === currentUser.value.id && 
             (meeting.status === 'scheduled')
    }
    
    // 判断是否可以删除
    const canDelete = (meeting) => {
      return meeting.hostId === currentUser.value.id && 
             (meeting.status === 'scheduled' || meeting.status === 'cancelled')
    }
    
    // 处理操作
    const handleAction = ({ action, meeting }) => {
      emit(action, meeting)
    }
    
    return {
      currentUser,
      formatMeetingTime,
      formatDuration,
      getStatusType,
      getStatusText,
      canJoin,
      canEdit,
      canDelete,
      handleAction
    }
  }
}
</script>

<style scoped>
.meeting-list-content {
  min-height: 200px;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 20px;
  text-align: center;
}

.meetings-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 16px;
  padding: 16px 0;
}

.meeting-card {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid #e4e7ed;
}

.meeting-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.meeting-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.meeting-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  flex: 1;
  margin-right: 8px;
}

.meeting-actions {
  flex-shrink: 0;
}

.meeting-info {
  margin-bottom: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 6px;
  font-size: 13px;
  color: #606266;
}

.info-item:last-child {
  margin-bottom: 0;
}

.info-item .el-icon {
  font-size: 14px;
  color: #909399;
}

.meeting-status {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.meeting-description {
  margin-bottom: 12px;
}

.meeting-description p {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.meeting-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.meeting-host {
  display: flex;
  align-items: center;
  gap: 8px;
}

.host-name {
  font-size: 13px;
  color: #606266;
}

.meeting-actions-primary {
  display: flex;
  gap: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .meetings-grid {
    grid-template-columns: 1fr;
    gap: 12px;
  }
  
  .meeting-card {
    padding: 12px;
  }
  
  .meeting-title {
    font-size: 15px;
  }
}
</style>