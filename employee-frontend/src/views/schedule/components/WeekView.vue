<template>
  <div class="week-view">
    <!-- 时间轴头部 -->
    <div class="week-header">
      <div class="time-column-header"></div>
      <div
        v-for="date in weekDates"
        :key="date.format('YYYY-MM-DD')"
        class="day-header"
        :class="{
          'today': date.isSame(dayjs(), 'day'),
          'weekend': date.day() === 0 || date.day() === 6
        }"
        @click="handleDateClick(date)"
      >
        <div class="day-name">{{ getDayName(date) }}</div>
        <div class="day-number">{{ date.date() }}</div>
      </div>
    </div>

    <!-- 时间轴和日程区域 -->
    <div class="week-content" ref="weekContentRef">
      <!-- 时间轴 -->
      <div class="time-column">
        <div
          v-for="hour in hours"
          :key="hour"
          class="time-slot"
        >
          <div class="time-label">{{ formatHour(hour) }}</div>
        </div>
      </div>

      <!-- 日程网格 -->
      <div class="days-grid">
        <div
          v-for="date in weekDates"
          :key="date.format('YYYY-MM-DD')"
          class="day-column"
          @click="handleDayColumnClick(date, $event)"
        >
          <!-- 时间网格线 -->
          <div
            v-for="hour in hours"
            :key="hour"
            class="time-grid-line"
            :class="{ 'current-time': isCurrentHour(date, hour) }"
          ></div>

          <!-- 日程事件 -->
          <div
            v-for="schedule in getDaySchedules(date)"
            :key="schedule.id"
            class="schedule-event"
            :class="getScheduleClass(schedule)"
            :style="getScheduleStyle(schedule)"
            @click.stop="handleScheduleClick(schedule.id)"
            @mousedown="startDrag(schedule, $event)"
          >
            <div class="schedule-time">
              {{ formatTime(schedule.start_time) }} - {{ formatTime(schedule.end_time) }}
            </div>
            <div class="schedule-title">{{ schedule.title }}</div>
            <div v-if="schedule.location" class="schedule-location">
              <el-icon><Location /></el-icon>
              {{ schedule.location }}
            </div>
          </div>
        </div>
      </div>

      <!-- 当前时间线 -->
      <div
        v-if="showCurrentTimeLine"
        class="current-time-line"
        :style="{ top: currentTimeLinePosition + 'px' }"
      >
        <div class="current-time-dot"></div>
        <div class="current-time-text">{{ currentTimeText }}</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import { Location } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// Props
const props = defineProps({
  schedules: {
    type: Array,
    default: () => []
  },
  currentDate: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['schedule-click', 'schedule-create', 'schedule-update'])

// 响应式数据
const weekContentRef = ref(null)
const dragState = ref({
  isDragging: false,
  schedule: null,
  startY: 0,
  startTime: null
})

// 小时数组 (0-23)
const hours = Array.from({ length: 24 }, (_, i) => i)

// 计算属性
const weekDates = computed(() => {
  const startOfWeek = props.currentDate.startOf('week')
  const dates = []
  for (let i = 0; i < 7; i++) {
    dates.push(startOfWeek.add(i, 'day'))
  }
  return dates
})

const showCurrentTimeLine = computed(() => {
  const today = dayjs()
  return weekDates.value.some(date => date.isSame(today, 'day'))
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
const getDayName = (date) => {
  const dayNames = ['日', '一', '二', '三', '四', '五', '六']
  return dayNames[date.day()]
}

const formatHour = (hour) => {
  return hour.toString().padStart(2, '0') + ':00'
}

const formatTime = (dateTime) => {
  return dayjs(dateTime).format('HH:mm')
}

const isCurrentHour = (date, hour) => {
  const now = dayjs()
  return date.isSame(now, 'day') && hour === now.hour()
}

const getDaySchedules = (date) => {
  const dateStr = date.format('YYYY-MM-DD')
  return props.schedules.filter(schedule => {
    const scheduleDate = dayjs(schedule.start_time).format('YYYY-MM-DD')
    return scheduleDate === dateStr && !schedule.all_day
  })
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
  const height = Math.max((duration / 60) * 60, 20) // 最小高度20px
  
  return {
    top: top + 'px',
    height: height + 'px',
    backgroundColor: schedule.color || '#409eff',
    left: '2px',
    right: '2px'
  }
}

const handleDateClick = (date) => {
  emit('date-click', date)
}

const handleDayColumnClick = (date, event) => {
  const rect = event.currentTarget.getBoundingClientRect()
  const y = event.clientY - rect.top
  const hour = Math.floor(y / 60)
  const minute = Math.round((y % 60) / 60 * 60 / 15) * 15 // 15分钟间隔
  
  const dateTime = date.hour(hour).minute(minute).format('YYYY-MM-DD HH:mm:ss')
  emit('schedule-create', dateTime)
}

const handleScheduleClick = (scheduleId) => {
  emit('schedule-click', scheduleId)
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
  const duration = dayjs(dragState.value.schedule.end_time).diff(dayjs(dragState.value.schedule.start_time), 'minute')
  const newEndTime = newStartTime.add(duration, 'minute')
  
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

// 生命周期
onMounted(() => {
  // 滚动到当前时间
  nextTick(() => {
    if (weekContentRef.value && showCurrentTimeLine.value) {
      const scrollTop = Math.max(0, currentTimeLinePosition.value - 200)
      weekContentRef.value.scrollTop = scrollTop
    }
  })
})

onUnmounted(() => {
  document.removeEventListener('mousemove', handleDrag)
  document.removeEventListener('mouseup', endDrag)
})
</script>

<style scoped>
.week-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.week-header {
  display: grid;
  grid-template-columns: 60px repeat(7, 1fr);
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.time-column-header {
  border-right: 1px solid #e4e7ed;
}

.day-header {
  padding: 12px 8px;
  text-align: center;
  cursor: pointer;
  transition: background-color 0.2s;
  border-right: 1px solid #e4e7ed;
}

.day-header:hover {
  background-color: #f0f2f5;
}

.day-header.today {
  background-color: #e3f2fd;
}

.day-header.weekend {
  background-color: #fafafa;
}

.day-name {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.day-number {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.day-header.today .day-number {
  color: #409eff;
}

.week-content {
  flex: 1;
  display: grid;
  grid-template-columns: 60px 1fr;
  overflow-y: auto;
  position: relative;
}

.time-column {
  border-right: 1px solid #e4e7ed;
  background: #fafafa;
}

.time-slot {
  height: 60px;
  border-bottom: 1px solid #f0f2f5;
  position: relative;
}

.time-label {
  position: absolute;
  top: -8px;
  right: 8px;
  font-size: 12px;
  color: #909399;
  background: #fafafa;
  padding: 0 4px;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  position: relative;
}

.day-column {
  border-right: 1px solid #e4e7ed;
  position: relative;
  cursor: pointer;
}

.day-column:last-child {
  border-right: none;
}

.time-grid-line {
  height: 60px;
  border-bottom: 1px solid #f0f2f5;
  position: relative;
}

.time-grid-line.current-time {
  background-color: rgba(64, 158, 255, 0.05);
}

.schedule-event {
  position: absolute;
  border-radius: 4px;
  padding: 4px 8px;
  color: white;
  cursor: pointer;
  transition: all 0.2s;
  overflow: hidden;
  z-index: 10;
  border-left: 3px solid rgba(255, 255, 255, 0.3);
}

.schedule-event:hover {
  transform: scale(1.02);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 20;
}

.schedule-event.private {
  opacity: 0.8;
}

.schedule-time {
  font-size: 11px;
  opacity: 0.9;
  margin-bottom: 2px;
}

.schedule-title {
  font-size: 12px;
  font-weight: 500;
  margin-bottom: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-location {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 10px;
  opacity: 0.8;
}

.current-time-line {
  position: absolute;
  left: 60px;
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

/* 优先级样式 */
.schedule-event.priority-high {
  border-left-color: #f56c6c;
}

.schedule-event.priority-medium {
  border-left-color: #e6a23c;
}

.schedule-event.priority-low {
  border-left-color: #67c23a;
}

/* 状态样式 */
.schedule-event.status-completed {
  opacity: 0.7;
  text-decoration: line-through;
}

.schedule-event.status-cancelled {
  opacity: 0.5;
  background-color: #909399 !important;
}
</style>