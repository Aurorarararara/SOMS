<template>
  <div class="month-view">
    <!-- 星期标题 -->
    <div class="week-header">
      <div 
        v-for="day in weekDays" 
        :key="day"
        class="week-day-header"
      >
        {{ day }}
      </div>
    </div>

    <!-- 日历网格 -->
    <div class="calendar-grid">
      <div
        v-for="date in calendarDates"
        :key="date.format('YYYY-MM-DD')"
        class="calendar-cell"
        :class="{
          'other-month': !date.isSame(currentDate, 'month'),
          'today': date.isSame(dayjs(), 'day'),
          'selected': date.isSame(selectedDate, 'day'),
          'has-schedules': getDateSchedules(date).length > 0
        }"
        @click="handleDateClick(date)"
        @dblclick="handleDateDoubleClick(date)"
      >
        <!-- 日期数字 -->
        <div class="date-number">
          {{ date.date() }}
        </div>

        <!-- 日程列表 -->
        <div class="date-schedules">
          <div
            v-for="(schedule, index) in getDateSchedules(date).slice(0, 3)"
            :key="schedule.id"
            class="schedule-item"
            :class="getScheduleClass(schedule)"
            :style="{ backgroundColor: schedule.color || '#409eff' }"
            @click.stop="handleScheduleClick(schedule.id)"
          >
            <div class="schedule-time" v-if="!schedule.all_day">
              {{ formatTime(schedule.start_time) }}
            </div>
            <div class="schedule-title">
              {{ schedule.title }}
            </div>
          </div>

          <!-- 更多日程指示器 -->
          <div
            v-if="getDateSchedules(date).length > 3"
            class="more-schedules"
            @click.stop="showMoreSchedules(date)"
          >
            +{{ getDateSchedules(date).length - 3 }} 更多
          </div>
        </div>
      </div>
    </div>

    <!-- 更多日程弹窗 -->
    <el-dialog
      v-model="moreSchedulesVisible"
      :title="`${selectedDateForMore?.format('YYYY年MM月DD日')} 的日程`"
      width="500px"
    >
      <div class="more-schedules-list">
        <div
          v-for="schedule in moreSchedulesList"
          :key="schedule.id"
          class="more-schedule-item"
          @click="handleScheduleClick(schedule.id)"
        >
          <div class="schedule-color-bar" :style="{ backgroundColor: schedule.color || '#409eff' }"></div>
          <div class="schedule-content">
            <div class="schedule-title">{{ schedule.title }}</div>
            <div class="schedule-meta">
              <span class="schedule-time">
                {{ formatTime(schedule.start_time) }} - {{ formatTime(schedule.end_time) }}
              </span>
              <span v-if="schedule.location" class="schedule-location">
                <el-icon><Location /></el-icon>
                {{ schedule.location }}
              </span>
            </div>
          </div>
          <div class="schedule-status" :class="schedule.status.toLowerCase()">
            {{ getStatusText(schedule.status) }}
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
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
  },
  selectedDate: {
    type: Object,
    required: true
  }
})

// Emits
const emit = defineEmits(['date-click', 'schedule-click', 'schedule-create'])

// 响应式数据
const moreSchedulesVisible = ref(false)
const selectedDateForMore = ref(null)
const moreSchedulesList = ref([])

// 星期标题
const weekDays = ['日', '一', '二', '三', '四', '五', '六']

// 计算属性
const calendarDates = computed(() => {
  const dates = []
  const startOfMonth = props.currentDate.startOf('month')
  const endOfMonth = props.currentDate.endOf('month')
  const startOfCalendar = startOfMonth.startOf('week')
  const endOfCalendar = endOfMonth.endOf('week')

  let current = startOfCalendar
  while (current.isBefore(endOfCalendar) || current.isSame(endOfCalendar, 'day')) {
    dates.push(current)
    current = current.add(1, 'day')
  }

  return dates
})

// 方法
const getDateSchedules = (date) => {
  const dateStr = date.format('YYYY-MM-DD')
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
}

const getScheduleClass = (schedule) => {
  return {
    'all-day': schedule.all_day,
    'private': schedule.is_private,
    [`priority-${schedule.priority?.toLowerCase()}`]: schedule.priority,
    [`status-${schedule.status?.toLowerCase()}`]: schedule.status
  }
}

const handleDateClick = (date) => {
  emit('date-click', date)
}

const handleDateDoubleClick = (date) => {
  const dateTime = date.hour(9).minute(0).format('YYYY-MM-DD HH:mm:ss')
  emit('schedule-create', dateTime)
}

const handleScheduleClick = (scheduleId) => {
  emit('schedule-click', scheduleId)
}

const showMoreSchedules = (date) => {
  selectedDateForMore.value = date
  moreSchedulesList.value = getDateSchedules(date)
  moreSchedulesVisible.value = true
}

const formatTime = (dateTime) => {
  return dayjs(dateTime).format('HH:mm')
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
</script>

<style scoped>
.month-view {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  border-bottom: 1px solid #e4e7ed;
  background: #fafafa;
}

.week-day-header {
  padding: 12px;
  text-align: center;
  font-weight: 600;
  color: #606266;
  border-right: 1px solid #e4e7ed;
}

.week-day-header:last-child {
  border-right: none;
}

.calendar-grid {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  grid-template-rows: repeat(6, 1fr);
}

.calendar-cell {
  border-right: 1px solid #e4e7ed;
  border-bottom: 1px solid #e4e7ed;
  padding: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
  overflow: hidden;
}

.calendar-cell:hover {
  background-color: #f5f7fa;
}

.calendar-cell.other-month {
  color: #c0c4cc;
  background-color: #fafafa;
}

.calendar-cell.today {
  background-color: #e3f2fd;
}

.calendar-cell.selected {
  background-color: #409eff;
  color: white;
}

.calendar-cell.selected .date-number {
  color: white;
}

.calendar-cell.has-schedules::after {
  content: '';
  position: absolute;
  top: 4px;
  right: 4px;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background-color: #409eff;
}

.date-number {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #303133;
}

.date-schedules {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
  color: white;
  cursor: pointer;
  transition: opacity 0.2s;
  overflow: hidden;
}

.schedule-item:hover {
  opacity: 0.8;
}

.schedule-item.all-day {
  font-weight: 500;
}

.schedule-item.private {
  opacity: 0.7;
}

.schedule-time {
  font-size: 10px;
  opacity: 0.9;
  white-space: nowrap;
}

.schedule-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.more-schedules {
  padding: 2px 6px;
  font-size: 11px;
  color: #909399;
  cursor: pointer;
  text-align: center;
  border: 1px dashed #d3d4d6;
  border-radius: 4px;
  transition: all 0.2s;
}

.more-schedules:hover {
  color: #409eff;
  border-color: #409eff;
  background-color: #f0f9ff;
}

.more-schedules-list {
  max-height: 400px;
  overflow-y: auto;
}

.more-schedule-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: background-color 0.2s;
}

.more-schedule-item:hover {
  background-color: #f5f7fa;
}

.more-schedule-item:last-child {
  border-bottom: none;
}

.schedule-color-bar {
  width: 4px;
  height: 40px;
  border-radius: 2px;
}

.schedule-content {
  flex: 1;
}

.schedule-content .schedule-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
}

.schedule-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.schedule-location {
  display: flex;
  align-items: center;
  gap: 4px;
}

.schedule-status {
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 12px;
  font-weight: 500;
}

.schedule-status.scheduled {
  background: #e1f5fe;
  color: #0277bd;
}

.schedule-status.in_progress {
  background: #fff3e0;
  color: #ef6c00;
}

.schedule-status.completed {
  background: #e8f5e8;
  color: #2e7d32;
}

.schedule-status.cancelled {
  background: #fce4ec;
  color: #c2185b;
}

/* 优先级样式 */
.schedule-item.priority-high {
  border-left: 3px solid #f56c6c;
}

.schedule-item.priority-medium {
  border-left: 3px solid #e6a23c;
}

.schedule-item.priority-low {
  border-left: 3px solid #67c23a;
}
</style>