<template>
  <div class="day-view">
    <!-- 日期头部 -->
    <div class="day-header">
      <div class="date-info">
        <h2 class="date-title">{{ selectedDate.format('YYYY年MM月DD日') }}</h2>
        <div class="date-meta">
          <span class="weekday">{{ getWeekdayName() }}</span>
          <span v-if="isToday" class="today-badge">今天</span>
          <span class="schedule-count">{{ daySchedules.length }} 个日程</span>
        </div>
      </div>
      
      <div class="day-actions">
        <el-button
          type="primary"
          size="small"
          @click="createSchedule"
          :icon="Plus"
        >
          新建日程
        </el-button>
      </div>
    </div>

    <!-- 全天事件区域 -->
    <div v-if="allDaySchedules.length > 0" class="all-day-section">
      <div class="section-header">
        <h3>全天事件</h3>
      </div>
      <div class="all-day-schedules">
        <div
          v-for="schedule in allDaySchedules"
          :key="schedule.id"
          class="all-day-schedule"
          :style="{ backgroundColor: schedule.color || '#409eff' }"
          @click="handleScheduleClick(schedule.id)"
        >
          <div class="schedule-title">{{ schedule.title }}</div>
          <div v-if="schedule.location" class="schedule-location">
            <el-icon><Location /></el-icon>
            {{ schedule.location }}
          </div>
        </div>
      </div>
    </div>

    <!-- 时间轴区域 -->
    <div class="timeline-section" ref="timelineRef">
      <div class="timeline-container">
        <!-- 时间轴 -->
        <div class="time-column">
          <div
            v-for="hour in hours"
            :key="hour"
            class="time-slot"
            :class="{ 'current-hour': isCurrentHour(hour) }"
          >
            <div class="time-label">{{ formatHour(hour) }}</div>
          </div>
        </div>

        <!-- 日程区域 -->
        <div class="schedule-column" @click="handleColumnClick">
          <!-- 时间网格线 -->
          <div
            v-for="hour in hours"
            :key="`grid-${hour}`"
            class="time-grid-line"
            :class="{ 'current-time': isCurrentHour(hour) }"
          ></div>

          <!-- 日程事件 -->
          <div
            v-for="schedule in timedSchedules"
            :key="schedule.id"
            class="schedule-event"
            :class="getScheduleClass(schedule)"
            :style="getScheduleStyle(schedule)"
            @click.stop="handleScheduleClick(schedule.id)"
            @mousedown="startDrag(schedule, $event)"
          >
            <div class="schedule-content">
              <div class="schedule-time">
                {{ formatTime(schedule.start_time) }} - {{ formatTime(schedule.end_time) }}
              </div>
              <div class="schedule-title">{{ schedule.title }}</div>
              <div v-if="schedule.location" class="schedule-location">
                <el-icon><Location /></el-icon>
                {{ schedule.location }}
              </div>
              <div v-if="schedule.description" class="schedule-description">
                {{ truncateText(schedule.description, 50) }}
              </div>
            </div>
            
            <!-- 状态指示器 -->
            <div class="schedule-status-indicator" :class="schedule.status?.toLowerCase()"></div>
            
            <!-- 拖拽手柄 -->
            <div class="drag-handle"></div>
          </div>

          <!-- 当前时间线 -->
          <div
            v-if="isToday && showCurrentTimeLine"
            class="current-time-line"
            :style="{ top: currentTimeLinePosition + 'px' }"
          >
            <div class="current-time-dot"></div>
            <div class="current-time-text">{{ currentTimeText }}</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="daySchedules.length === 0" class="empty-state">
      <el-empty description="今日暂无日程安排">
        <el-button type="primary" @click="createSchedule">
          创建第一个日程
        </el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { Plus, Location } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// Props
const props = defineProps({
  schedules: {
    type: Array,
    default: () => []
  },
  selectedDate: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['schedule-click', 'schedule-create', 'schedule-update'])

// 响应式数据
const timelineRef = ref(null)
const dragState = ref({
  isDragging: false,
  schedule: null,
  startY: 0,
  startTime: null
})

// 小时数组 (0-23)
const hours = Array.from({ length: 24 }, (_, i) => i)

// 计算属性
const daySchedules = computed(() => {
  const dateStr = props.selectedDate.format('YYYY-MM-DD')
  return props.schedules.filter(schedule => {
    const scheduleDate = dayjs(schedule.start_time).format('YYYY-MM-DD')
    return scheduleDate === dateStr
  }).sort((a, b) => {
    // 全天事件排在前面
    if (a.all_day && !b.all_day) return -1
    if (!a.all_day && b.all_day) return 1
    // 按开始时间排序
    return dayjs(a.start_time).valueOf() - dayjs(b.start_time).valueOf()
  })
})

const allDaySchedules = computed(() => {
  return daySchedules.value.filter(schedule => schedule.all_day)
})

const timedSchedules = computed(() => {
  return daySchedules.value.filter(schedule => !schedule.all_day)
})

const isToday = computed(() => {
  return props.selectedDate.isSame(dayjs(), 'day')
})

const showCurrentTimeLine = computed(() => {
  return isToday.value
})

const currentTimeLinePosition = computed(() => {
  const now = dayjs()
  const hour = now.hour()
  const minute = now.minute()
  return (hour * 60 + minute) * (60 / 60) // 每小时60px高度
})

const currentTimeText = computed(() => {
  return dayjs().format('HH:mm')
})

// 方法
const getWeekdayName = () => {
  const weekdays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return weekdays[props.selectedDate.day()]
}

const formatHour = (hour) => {
  return hour.toString().padStart(2, '0') + ':00'
}

const formatTime = (dateTime) => {
  return dayjs(dateTime).format('HH:mm')
}

const isCurrentHour = (hour) => {
  if (!isToday.value) return false
  return hour === dayjs().hour()
}

const getScheduleClass = (schedule) => {
  return {
    'private': schedule.is_private,
    [`priority-${schedule.priority?.toLowerCase()}`]: schedule.priority,
    [`status-${schedule.status?.toLowerCase()}`]: schedule.status
  }
}

const getScheduleStyle = (schedule) => {
  const startTime = dayjs(schedule.start_time)
  const endTime = dayjs(schedule.end_time)
  
  const startMinutes = startTime.hour() * 60 + startTime.minute()
  const endMinutes = endTime.hour() * 60 + endTime.minute()
  const duration = endMinutes - startMinutes
  
  const top = (startMinutes / 60) * 60 // 每小时60px
  const height = Math.max((duration / 60) * 60, 30) // 最小高度30px
  
  return {
    top: top + 'px',
    height: height + 'px',
    backgroundColor: schedule.color || '#409eff',
    left: '4px',
    right: '4px'
  }
}

const handleColumnClick = (event) => {
  if (dragState.value.isDragging) return
  
  const rect = event.currentTarget.getBoundingClientRect()
  const y = event.clientY - rect.top
  const hour = Math.floor(y / 60)
  const minute = Math.round((y % 60) / 60 * 60 / 15) * 15 // 15分钟间隔
  
  const dateTime = props.selectedDate.hour(hour).minute(minute).format('YYYY-MM-DD HH:mm:ss')
  emit('schedule-create', dateTime)
}

const handleScheduleClick = (scheduleId) => {
  emit('schedule-click', scheduleId)
}

const createSchedule = () => {
  const now = dayjs()
  let dateTime
  
  if (props.selectedDate.isSame(now, 'day')) {
    // 如果是今天，从当前时间开始
    const nextHour = now.add(1, 'hour').startOf('hour')
    dateTime = nextHour.format('YYYY-MM-DD HH:mm:ss')
  } else {
    // 其他日期从9点开始
    dateTime = props.selectedDate.hour(9).minute(0).format('YYYY-MM-DD HH:mm:ss')
  }
  
  emit('schedule-create', dateTime)
}

const startDrag = (schedule, event) => {
  dragState.value = {
    isDragging: true,
    schedule: schedule,
    startY: event.clientY,
    startTime: dayjs(schedule.start_time)
  }
  
  document.addEventListener('mousemove', handleDrag)
  document.addEventListener('mouseup', endDrag)
  event.preventDefault()
}

const handleDrag = (event) => {
  if (!dragState.value.isDragging) return
  
  const deltaY = event.clientY - dragState.value.startY
  const deltaMinutes = Math.round(deltaY / 60 * 60 / 15) * 15 // 15分钟间隔
  
  const newStartTime = dragState.value.startTime.add(deltaMinutes, 'minute')
  
  // 更新临时样式
  const scheduleElement = event.target.closest('.schedule-event')
  if (scheduleElement) {
    const newTop = (newStartTime.hour() * 60 + newStartTime.minute()) / 60 * 60
    scheduleElement.style.top = newTop + 'px'
  }
}

const endDrag = (event) => {
  if (!dragState.value.isDragging) return
  
  const deltaY = event.clientY - dragState.value.startY
  const deltaMinutes = Math.round(deltaY / 60 * 60 / 15) * 15
  
  if (Math.abs(deltaMinutes) >= 15) {
    const newStartTime = dragState.value.startTime.add(deltaMinutes, 'minute')
    const duration = dayjs(dragState.value.schedule.end_time).diff(dayjs(dragState.value.schedule.start_time), 'minute')
    const newEndTime = newStartTime.add(duration, 'minute')
    
    const updatedSchedule = {
      ...dragState.value.schedule,
      start_time: newStartTime.format('YYYY-MM-DD HH:mm:ss'),
      end_time: newEndTime.format('YYYY-MM-DD HH:mm:ss')
    }
    
    emit('schedule-update', updatedSchedule)
  }
  
  dragState.value = {
    isDragging: false,
    schedule: null,
    startY: 0,
    startTime: null
  }
  
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', endDrag)
}

const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

// 生命周期
onMounted(() => {
  // 滚动到当前时间或工作时间
  nextTick(() => {
    if (timelineRef.value) {
      let scrollTop = 0
      
      if (isToday.value && showCurrentTimeLine.value) {
        // 滚动到当前时间
        scrollTop = Math.max(0, currentTimeLinePosition.value - 200)
      } else {
        // 滚动到9点
        scrollTop = 9 * 60
      }
      
      timelineRef.value.scrollTop = scrollTop
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', endDrag)
})
</script>

<style scoped>
.day-view {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: white;
}

.day-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.date-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.date-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.date-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}

.weekday {
  color: #606266;
}

.today-badge {
  background: #409eff;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.schedule-count {
  color: #909399;
}

.all-day-section {
  padding: 16px 24px;
  border-bottom: 1px solid #f0f2f5;
  background: #fafbfc;
}

.section-header h3 {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

.all-day-schedules {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.all-day-schedule {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  color: white;
  cursor: pointer;
  transition: opacity 0.2s;
}

.all-day-schedule:hover {
  opacity: 0.8;
}

.all-day-schedule .schedule-title {
  font-weight: 500;
}

.all-day-schedule .schedule-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  opacity: 0.9;
}

.timeline-section {
  flex: 1;
  overflow-y: auto;
  padding: 0 24px;
}

.timeline-container {
  display: grid;
  grid-template-columns: 80px 1fr;
  min-height: 1440px; /* 24小时 * 60px */
}

.time-column {
  border-right: 1px solid #e4e7ed;
  background: #fafafa;
}

.time-slot {
  height: 60px;
  border-bottom: 1px solid #f0f2f5;
  position: relative;
  display: flex;
  align-items: flex-start;
  padding-top: 4px;
}

.time-slot.current-hour {
  background-color: rgba(64, 158, 255, 0.05);
}

.time-label {
  font-size: 12px;
  color: #909399;
  padding: 0 12px;
  background: #fafafa;
}

.schedule-column {
  position: relative;
  cursor: pointer;
}

.time-grid-line {
  height: 60px;
  border-bottom: 1px solid #f0f2f5;
  position: relative;
}

.time-grid-line.current-time {
  background-color: rgba(64, 158, 255, 0.03);
}

.schedule-event {
  position: absolute;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
  z-index: 10;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.schedule-event:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 20;
}

.schedule-event.private {
  opacity: 0.8;
  border-style: dashed;
}

.schedule-content {
  padding: 8px 12px;
  color: white;
}

.schedule-time {
  font-size: 11px;
  opacity: 0.9;
  margin-bottom: 4px;
  font-weight: 500;
}

.schedule-title {
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  opacity: 0.8;
  margin-bottom: 4px;
}

.schedule-description {
  font-size: 11px;
  opacity: 0.8;
  line-height: 1.3;
}

.schedule-status-indicator {
  position: absolute;
  top: 0;
  right: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin: 4px;
}

.schedule-status-indicator.completed {
  background-color: #67c23a;
}

.schedule-status-indicator.in_progress {
  background-color: #e6a23c;
}

.schedule-status-indicator.cancelled {
  background-color: #f56c6c;
}

.drag-handle {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 20px;
  height: 4px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 2px;
  cursor: ns-resize;
}

.current-time-line {
  position: absolute;
  left: 0;
  right: 0;
  height: 2px;
  background-color: #f56c6c;
  z-index: 100;
  pointer-events: none;
}

.current-time-dot {
  position: absolute;
  left: -4px;
  top: -3px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #f56c6c;
}

.current-time-text {
  position: absolute;
  left: 12px;
  top: -8px;
  font-size: 11px;
  color: #f56c6c;
  background: white;
  padding: 0 4px;
  border-radius: 2px;
  font-weight: 500;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

/* 优先级样式 */
.schedule-event.priority-high {
  border-left: 4px solid #f56c6c;
}

.schedule-event.priority-medium {
  border-left: 4px solid #e6a23c;
}

.schedule-event.priority-low {
  border-left: 4px solid #67c23a;
}

.schedule-event.priority-urgent {
  border-left: 4px solid #ff4757;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(255, 71, 87, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(255, 71, 87, 0); }
  100% { box-shadow: 0 0 0 0 rgba(255, 71, 87, 0); }
}

/* 状态样式 */
.schedule-event.status-completed {
  opacity: 0.7;
}

.schedule-event.status-completed .schedule-title {
  text-decoration: line-through;
}

.schedule-event.status-cancelled {
  opacity: 0.5;
  background-color: #909399 !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .day-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .timeline-container {
    grid-template-columns: 60px 1fr;
  }
  
  .time-label {
    padding: 0 8px;
    font-size: 11px;
  }
  
  .schedule-content {
    padding: 6px 8px;
  }
}
</style>