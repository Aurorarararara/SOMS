<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <div class="welcome-section">
      <div class="welcome-content">
        <div class="greeting-info">
          <h1 class="welcome-title">
            {{ greetingText }}，{{ userStore.userInfo?.realName || '用户' }}！
          </h1>
          <p class="welcome-subtitle">今天是 {{ currentDate }}，期待您的高效工作</p>
        </div>
        <div class="quick-stats">
          <div class="stat-card">
            <div class="stat-icon primary">
              <el-icon size="20"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ attendanceStats.workDays }}</div>
              <div class="stat-label">本月出勤</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon success">
              <el-icon size="20"><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-number">{{ attendanceStats.workHours }}h</div>
              <div class="stat-label">工作时长</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="dashboard-content">
      <!-- 左侧内容 -->
      <div class="content-left">
        <!-- 快捷操作卡片 -->
        <div class="quick-actions-card">
          <div class="card-header">
            <h3>快捷操作</h3>
            <el-icon><Operation /></el-icon>
          </div>
          <div class="actions-grid">
            <div 
              class="action-item" 
              v-for="action in quickActions" 
              :key="action.key"
              @click="handleQuickAction(action)"
            >
              <div class="action-icon" :style="{ backgroundColor: action.color }">
                <el-icon size="20" :color="action.iconColor">
                  <component :is="action.icon" />
                </el-icon>
              </div>
              <div class="action-content">
                <div class="action-title">{{ action.title }}</div>
                <div class="action-desc">{{ action.desc }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 考勤打卡卡片 -->
        <div class="attendance-card">
          <div class="card-header">
            <h3>今日考勤</h3>
            <el-icon><Clock /></el-icon>
          </div>
          <div class="attendance-content">
            <div class="attendance-status">
              <div class="status-item">
                <div class="status-label">上班打卡</div>
                <div class="status-value" :class="{ 'checked-in': todayAttendance?.checkInTime }">
                  {{ todayAttendance?.checkInTime || '未打卡' }}
                </div>
              </div>
              <div class="status-item">
                <div class="status-label">下班打卡</div>
                <div class="status-value" :class="{ 'checked-out': todayAttendance?.checkOutTime }">
                  {{ todayAttendance?.checkOutTime || '未打卡' }}
                </div>
              </div>
            </div>
            <div class="attendance-actions">
              <el-button 
                type="primary" 
                :disabled="todayAttendance?.checkInTime && todayAttendance?.checkOutTime"
                @click="handleAttendanceAction"
                class="attendance-btn"
              >
                {{ getAttendanceButtonText() }}
              </el-button>
            </div>
          </div>
        </div>

        <!-- 本月统计 -->
        <div class="statistics-card">
          <div class="card-header">
            <h3>本月统计</h3>
            <el-icon><DataAnalysis /></el-icon>
          </div>
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon-wrapper primary">
                <el-icon><UserFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ monthlyStats.attendanceDays }}</div>
                <div class="stat-text">出勤天数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon-wrapper success">
                <el-icon><SuccessFilled /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ monthlyStats.lateDays }}</div>
                <div class="stat-text">迟到次数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon-wrapper warning">
                <el-icon><DocumentRemove /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ monthlyStats.leaveDays }}</div>
                <div class="stat-text">请假天数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon-wrapper info">
                <el-icon><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-number">{{ monthlyStats.workHours }}h</div>
                <div class="stat-text">工作时长</div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="content-right">
        <!-- 最新公告 -->
        <div class="announcements-card">
          <div class="card-header">
            <h3>最新公告</h3>
            <el-button text @click="$router.push('/notifications')">
              查看更多 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="announcements-list">
            <div 
              class="announcement-item" 
              v-for="item in recentAnnouncements" 
              :key="item.id"
              @click="viewAnnouncement(item)"
            >
              <div class="announcement-icon">
                <el-icon :color="getAnnouncementIconColor(item.type)">
                  <Bell />
                </el-icon>
              </div>
              <div class="announcement-content">
                <div class="announcement-title">{{ item.title }}</div>
                <div class="announcement-meta">
                  <span class="announcement-time">{{ formatTime(item.publishTime || item.createTime) }}</span>
                  <el-tag 
                    :type="getAnnouncementTagType(item.type)" 
                    size="small"
                  >
                    {{ getAnnouncementDisplayText(item.type) }}
                  </el-tag>
                </div>
              </div>
            </div>
            <div v-if="recentAnnouncements.length === 0" class="empty-state">
              <el-icon><DocumentRemove /></el-icon>
              <p>暂无公告</p>
            </div>
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="todo-card">
          <div class="card-header">
            <h3>待办事项</h3>
            <el-button text @click="$router.push('/schedule')">
              管理日程 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div class="todo-list">
            <div 
              class="todo-item" 
              v-for="item in todoItems" 
              :key="item.id"
            >
              <el-checkbox v-model="item.completed" @change="toggleTodo(item)" />
              <div class="todo-content" :class="{ completed: item.completed }">
                <div class="todo-title">{{ item.title }}</div>
                <div class="todo-time">{{ item.dueTime }}</div>
              </div>
            </div>
            <div v-if="todoItems.length === 0" class="empty-state">
              <el-icon><SuccessFilled /></el-icon>
              <p>所有任务已完成</p>
            </div>
          </div>
        </div>

        <!-- 天气信息 -->
        <div class="weather-card">
          <div class="card-header">
            <h3>今日天气</h3>
            <el-icon><Sunny /></el-icon>
          </div>
          <div class="weather-content">
            <div class="weather-main">
              <div class="weather-icon">
                <el-icon size="32" color="#f39c12"><Sunny /></el-icon>
              </div>
              <div class="weather-info">
                <div class="temperature">{{ weather.temperature }}°C</div>
                <div class="weather-desc">{{ weather.description }}</div>
              </div>
            </div>
            <div class="weather-details">
              <div class="detail-item">
                <span>湿度</span>
                <span>{{ weather.humidity }}%</span>
              </div>
              <div class="detail-item">
                <span>风速</span>
                <span>{{ weather.windSpeed }} km/h</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Calendar, Timer, Operation, Clock, DataAnalysis, UserFilled,
  SuccessFilled, DocumentRemove, Bell, ArrowRight, Sunny
} from '@element-plus/icons-vue'
import { attendanceApi } from '@/api/attendance'
import { leaveApi } from '@/api/leave'
import { announcementApi } from '@/api/announcement'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const todayAttendance = ref(null)
const recentAnnouncements = ref([])
const todoItems = ref([
  { id: 1, title: '完成项目进度报告', dueTime: '今天 18:00', completed: false },
  { id: 2, title: '参加团队周会', dueTime: '明天 10:00', completed: false },
  { id: 3, title: '提交月度总结', dueTime: '本周五', completed: true }
])

const attendanceStats = reactive({
  workDays: 20,
  workHours: 160
})

const monthlyStats = reactive({
  attendanceDays: 20,
  lateDays: 2,
  leaveDays: 1,
  workHours: 160
})

const weather = reactive({
  temperature: 24,
  description: '晴朗',
  humidity: 65,
  windSpeed: 8
})

// 计算属性
const greetingText = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  return new Date().toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  })
})

// 快捷操作配置
const quickActions = [
  {
    key: 'attendance',
    title: '考勤打卡',
    desc: '上下班打卡',
    icon: 'Clock',
    color: '#e3f2fd',
    iconColor: '#1976d2'
  },
  {
    key: 'leave',
    title: '请假申请',
    desc: '提交请假申请',
    icon: 'DocumentRemove',
    color: '#fff3e0',
    iconColor: '#f57c00'
  },
  {
    key: 'schedule',
    title: '日程管理',
    desc: '查看我的日程',
    icon: 'Calendar',
    color: '#f3e5f5',
    iconColor: '#7b1fa2'
  },
  {
    key: 'profile',
    title: '个人信息',
    desc: '更新个人资料',
    icon: 'User',
    color: '#e8f5e8',
    iconColor: '#388e3c'
  }
]

// 方法
const handleQuickAction = (action) => {
  switch (action.key) {
    case 'attendance':
      router.push('/attendance')
      break
    case 'leave':
      router.push('/leave')
      break
    case 'schedule':
      router.push('/schedule')
      break
    case 'profile':
      router.push('/profile')
      break
  }
}

const handleAttendanceAction = async () => {
  try {
    const isCheckIn = !todayAttendance.value?.checkInTime
    const action = isCheckIn ? '上班打卡' : '下班打卡'
    
    await ElMessageBox.confirm(
      `确认${action}吗？`,
      '考勤打卡',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'info'
      }
    )

    // 调用打卡API
    if (isCheckIn) {
      await attendanceApi.checkIn()
    } else {
      await attendanceApi.checkOut()
    }

    ElMessage.success(`${action}成功！`)
    loadTodayAttendance()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('打卡失败，请重试')
    }
  }
}

const getAttendanceButtonText = () => {
  if (!todayAttendance.value?.checkInTime) {
    return '上班打卡'
  }
  if (!todayAttendance.value?.checkOutTime) {
    return '下班打卡'
  }
  return '已完成打卡'
}

const viewAnnouncement = (item) => {
  router.push(`/notifications/${item.id}`)
}

const getAnnouncementIconColor = (type) => {
  const colors = {
    'urgent': '#f56565',
    'important': '#ed8936',
    'normal': '#38b2ac'
  }
  return colors[type] || '#38b2ac'
}

const getAnnouncementTagType = (type) => {
  const types = {
    'urgent': 'danger',
    'important': 'warning',
    'normal': 'info'
  }
  return types[type] || 'info'
}

const getAnnouncementDisplayText = (type) => {
  const texts = {
    'urgent': '紧急',
    'important': '重要',
    'normal': '一般'
  }
  return texts[type] || '一般'
}

const toggleTodo = (item) => {
  ElMessage.success(item.completed ? '任务已完成' : '任务已重新开启')
}

const formatTime = (time) => {
  return new Date(time).toLocaleDateString('zh-CN', {
    month: 'long',
    day: 'numeric'
  })
}

// 数据加载
const loadTodayAttendance = async () => {
  try {
    const today = new Date().toISOString().split('T')[0]
    const { data } = await attendanceApi.getTodayRecord(today)
    todayAttendance.value = data
  } catch (error) {
    console.error('加载考勤数据失败:', error)
  }
}

const loadRecentAnnouncements = async () => {
  try {
    const { data } = await announcementApi.getRecentList(5)
    recentAnnouncements.value = data || []
  } catch (error) {
    console.error('加载公告数据失败:', error)
    // 使用模拟数据作为备用方案
    recentAnnouncements.value = [
      {
        id: 1,
        title: '欢迎使用办公系统',
        content: '欢迎大家使用新的办公管理系统，请认真阅读使用手册。',
        type: 'normal',
        publishTime: new Date().toISOString(),
        createTime: new Date().toISOString()
      },
      {
        id: 2,
        title: '系统维护通知',
        content: '系统将于本周六进行维护升级，维护期间系统暂停使用。',
        type: 'urgent',
        publishTime: new Date().toISOString(),
        createTime: new Date().toISOString()
      },
      {
        id: 3,
        title: '考勤制度说明',
        content: '请各位员工严格遵守考勤制度，按时签到签退。',
        type: 'normal',
        publishTime: new Date().toISOString(),
        createTime: new Date().toISOString()
      }
    ]
    // 只在非网络错误时显示错误消息
    if (!error.message?.includes('网络错误')) {
      ElMessage.warning('暂时无法加载最新公告，显示示例数据')
    }
  }
}

const loadMonthlyStats = async () => {
  try {
    const now = new Date()
    const year = now.getFullYear()
    const month = now.getMonth() + 1
    
    // 加载考勤统计
    // const attendanceData = await attendanceApi.getMonthlyStats(year, month)
    // const leaveData = await leaveApi.getMonthlyStats(year, month)
    
    // 这里使用模拟数据，实际应用中替换为API调用
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

// 生命周期
onMounted(() => {
  loadTodayAttendance()
  loadRecentAnnouncements()
  loadMonthlyStats()
})
</script>

<style scoped>
.dashboard-container {
  padding: 32px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  min-height: calc(100vh - 60px);
}

/* 欢迎区域 */
.welcome-section {
  background: linear-gradient(135deg, #1e293b 0%, #334155 100%);
  border-radius: 20px;
  padding: 40px;
  margin-bottom: 32px;
  color: white;
  box-shadow: 0 10px 30px rgba(30, 41, 59, 0.2);
  position: relative;
  overflow: hidden;
}

.welcome-section::before {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 70%);
  border-radius: 50%;
  transform: translate(50%, -50%);
}

.welcome-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
  z-index: 1;
}

.welcome-title {
  font-size: 32px;
  font-weight: 700;
  margin: 0 0 12px 0;
  background: linear-gradient(135deg, #ffffff 0%, #e2e8f0 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
  color: #cbd5e1;
}

.quick-stats {
  display: flex;
  gap: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  background: rgba(255, 255, 255, 0.1);
  padding: 20px 24px;
  border-radius: 16px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
}

.stat-card:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.stat-icon.primary {
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  color: white;
}

.stat-icon.success {
  background: linear-gradient(135deg, #10b981 0%, #047857 100%);
  color: white;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  line-height: 1;
  color: white;
}

.stat-label {
  font-size: 14px;
  opacity: 0.8;
  color: #cbd5e1;
  margin-top: 4px;
}

/* 主要内容区域 */
.dashboard-content {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 32px;
}

.content-left {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.content-right {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 通用卡片样式 */
.quick-actions-card,
.attendance-card,
.statistics-card,
.announcements-card,
.todo-card,
.weather-card {
  background: white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  border: 1px solid #e2e8f0;
  transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
}

.quick-actions-card:hover,
.attendance-card:hover,
.statistics-card:hover,
.announcements-card:hover,
.todo-card:hover,
.weather-card:hover {
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transform: translateY(-4px);
  border-color: #cbd5e1;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f1f5f9;
}

.card-header h3 {
  font-size: 20px;
  font-weight: 700;
  color: #1e293b;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-header .el-icon {
  color: #64748b;
}

/* 快捷操作 */
.actions-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0.0, 0.2, 1);
  border: 1px solid #e2e8f0;
}

.action-item:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.action-icon {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.action-title {
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 4px;
  font-size: 16px;
}

.action-desc {
  font-size: 14px;
  color: #64748b;
}

/* 考勤卡片 */
.attendance-content {
  text-align: center;
}

.attendance-status {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 28px;
}
.status-item {
  text-align: center;
  padding: 20px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 16px;
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.status-item:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  transform: translateY(-2px);
}

.status-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
  font-weight: 600;
}

.status-value {
  font-size: 18px;
  font-weight: 700;
  color: #94a3b8;
  padding: 12px 16px;
  border-radius: 12px;
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.2);
  transition: all 0.3s ease;
}

.status-value.checked-in,
.status-value.checked-out {
  color: #10b981;
  background: rgba(16, 185, 129, 0.1);
  border-color: rgba(16, 185, 129, 0.2);
}

.attendance-btn {
  width: 100%;
  height: 52px;
  font-size: 16px;
  font-weight: 700;
  border-radius: 16px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  border: none;
  color: white;
  transition: all 0.3s ease;
}

.attendance-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(59, 130, 246, 0.4);
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border: 1px solid #e2e8f0;
  transition: all 0.3s ease;
}

.stat-card:hover {
  background: linear-gradient(135deg, #e2e8f0 0%, #cbd5e1 100%);
  transform: translateY(-2px);
}

.stat-icon-wrapper {
  width: 52px;
  height: 52px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.stat-icon-wrapper.primary { 
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
}
.stat-icon-wrapper.success { 
  background: linear-gradient(135deg, #10b981 0%, #047857 100%);
}
.stat-icon-wrapper.warning { 
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}
.stat-icon-wrapper.info { 
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
}

.stat-text {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
  font-weight: 500;
}

/* 公告列表 */
.announcements-list {
  max-height: 400px;
  overflow-y: auto;
}

.announcement-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
  cursor: pointer;
  transition: all 0.3s ease;
}

.announcement-item:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  margin: 0 -16px;
  padding: 16px;
  border-radius: 12px;
}

.announcement-item:last-child {
  border-bottom: none;
}

.announcement-icon {
  margin-top: 2px;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  background: rgba(59, 130, 246, 0.1);
}

.announcement-title {
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 8px;
  line-height: 1.5;
  font-size: 15px;
}

.announcement-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.announcement-time {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

/* 待办事项 */
.todo-list {
  max-height: 300px;
  overflow-y: auto;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 0;
  border-bottom: 1px solid #f1f5f9;
  transition: all 0.3s ease;
}

.todo-item:hover {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  margin: 0 -16px;
  padding: 16px;
  border-radius: 12px;
}

.todo-item:last-child {
  border-bottom: none;
}

.todo-content {
  flex: 1;
}

.todo-content.completed .todo-title {
  text-decoration: line-through;
  color: #94a3b8;
}

.todo-title {
  font-weight: 600;
  color: #1e293b;
  margin-bottom: 6px;
  font-size: 15px;
}

.todo-time {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

/* 天气卡片 */
.weather-content {
  text-align: center;
}

.weather-main {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
}

.temperature {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.weather-desc {
  font-size: 16px;
  color: #64748b;
  margin-top: 8px;
  font-weight: 500;
}

.weather-details {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding-top: 20px;
  border-top: 1px solid #f1f5f9;
}

.detail-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 14px;
  text-align: center;
  padding: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
}

.detail-item span:first-child {
  color: #64748b;
  font-weight: 500;
}

.detail-item span:last-child {
  font-weight: 700;
  color: #1e293b;
  font-size: 16px;
}

/* 空状态 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #94a3b8;
}

.empty-state .el-icon {
  font-size: 48px;
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  font-size: 16px;
  font-weight: 500;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .dashboard-content {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .content-right {
    order: -1;
  }
  
  .welcome-content {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 20px;
  }
  
  .welcome-section {
    padding: 24px;
  }
  
  .welcome-title {
    font-size: 24px;
  }
  
  .quick-stats {
    flex-direction: column;
    gap: 16px;
  }
  
  .actions-grid {
    grid-template-columns: 1fr;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .attendance-status {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .weather-details {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 480px) {
  .dashboard-container {
    padding: 16px;
  }
  
  .quick-actions-card,
  .attendance-card,
  .statistics-card,
  .announcements-card,
  .todo-card,
  .weather-card {
    padding: 20px;
  }
}
</style>