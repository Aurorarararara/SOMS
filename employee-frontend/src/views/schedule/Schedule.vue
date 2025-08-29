<template>
  <div class="schedule-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h1 class="page-title">{{ $t('nav.schedule') }}</h1>
          <p class="page-subtitle">{{ $t('schedule.subtitle') }}</p>
        </div>
        <div class="header-right">
          <el-button type="primary" @click="showEventDialog = true">
            <el-icon><Plus /></el-icon>
            {{ $t('schedule.createEvent') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 日历视图 -->
    <div class="calendar-section">
      <div class="calendar-card">
        <div class="calendar-header">
          <div class="view-controls">
            <el-button-group>
              <el-button :type="currentView === 'month' ? 'primary' : ''" @click="currentView = 'month'">{{ $t('schedule.monthView') }}</el-button>
              <el-button :type="currentView === 'week' ? 'primary' : ''" @click="currentView = 'week'">{{ $t('schedule.weekView') }}</el-button>
              <el-button :type="currentView === 'day' ? 'primary' : ''" @click="currentView = 'day'">{{ $t('schedule.dayView') }}</el-button>
            </el-button-group>
          </div>
          <div class="date-navigation">
            <el-button @click="previousPeriod" circle><el-icon><ArrowLeft /></el-icon></el-button>
            <span class="current-date">{{ currentDateText }}</span>
            <el-button @click="nextPeriod" circle><el-icon><ArrowRight /></el-icon></el-button>
            <el-button @click="goToday" type="primary" plain>{{ $t('schedule.today') }}</el-button>
          </div>
        </div>

        <div class="calendar-content">
          <!-- 月视图 -->
          <div v-if="currentView === 'month'" class="month-view">
            <div class="weekdays">
              <div class="weekday" v-for="day in weekdays" :key="day">{{ day }}</div>
            </div>
            <div class="days-grid">
              <div 
                v-for="date in monthDays" 
                :key="date.date"
                class="day-cell"
                :class="{ 
                  'other-month': !date.currentMonth,
                  'today': date.isToday,
                  'selected': selectedDate === date.date 
                }"
                @click="selectDate(date.date)"
              >
                <div class="day-number">{{ date.day }}</div>
                <div class="day-events">
                  <div 
                    v-for="event in getEventsForDate(date.date)" 
                    :key="event.id" 
                    class="event-item"
                    :class="event.type"
                    @click.stop="viewEvent(event)"
                  >
                    {{ event.title }}
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- 周视图 -->
          <div v-if="currentView === 'week'" class="week-view">
            <div class="week-header">
              <div v-for="date in weekDays" :key="date.date" class="week-day-header">
                <div class="day-name">{{ date.dayName }}</div>
                <div class="day-date" :class="{ 'today': date.isToday }">{{ date.day }}</div>
              </div>
            </div>
            <div class="week-events">
              <div v-for="date in weekDays" :key="date.date" class="week-day-events">
                <div 
                  v-for="event in getEventsForDate(date.date)" 
                  :key="event.id"
                  class="week-event"
                  :class="event.type"
                  @click="viewEvent(event)"
                >
                  <div class="event-time">{{ event.time }}</div>
                  <div class="event-title">{{ event.title }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 事件列表 -->
    <div class="events-section" v-if="selectedDate">
      <div class="events-card">
        <div class="card-header">
          <h3>{{ formatSelectedDate(selectedDate) }} 的日程</h3>
          <el-button size="small" @click="addEventForDate">添加日程</el-button>
        </div>
        <div class="events-list">
          <div 
            v-for="event in getEventsForDate(selectedDate)" 
            :key="event.id"
            class="event-card"
            :class="event.type"
          >
            <div class="event-info">
              <div class="event-title">{{ event.title }}</div>
              <div class="event-meta">
                <span class="event-time">{{ event.time }}</span>
                <el-tag :type="getEventTypeTag(event.type)" size="small">{{ event.type }}</el-tag>
              </div>
              <div class="event-description" v-if="event.description">{{ event.description }}</div>
            </div>
            <div class="event-actions">
              <el-button text type="primary" @click="editEvent(event)" size="small">编辑</el-button>
              <el-button text type="danger" @click="deleteEvent(event)" size="small">删除</el-button>
            </div>
          </div>
          <div v-if="getEventsForDate(selectedDate).length === 0" class="empty-events">
            <el-icon><Calendar /></el-icon>
            <p>暂无日程安排</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑日程对话框 -->
    <el-dialog v-model="showEventDialog" :title="editingEvent ? '编辑日程' : '新建日程'" width="600px" @close="resetEventForm">
      <el-form ref="eventFormRef" :model="eventForm" :rules="eventRules" label-width="100px">
        <el-form-item label="日程标题" prop="title">
          <el-input v-model="eventForm.title" placeholder="请输入日程标题" />
        </el-form-item>
        <el-form-item label="日程类型" prop="type">
          <el-select v-model="eventForm.type" placeholder="请选择日程类型" style="width: 100%">
            <el-option label="会议" value="meeting" />
            <el-option label="任务" value="task" />
            <el-option label="提醒" value="reminder" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期时间" prop="datetime">
          <el-date-picker
            v-model="eventForm.datetime"
            type="datetime"
            placeholder="选择日期时间"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="eventForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入日程描述（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showEventDialog = false">取消</el-button>
          <el-button type="primary" @click="submitEvent" :loading="submitting">
            {{ editingEvent ? '保存' : '创建' }}
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ArrowLeft, ArrowRight, Calendar } from '@element-plus/icons-vue'

const { t: $t } = useI18n()

// 响应式数据
const currentView = ref('month')
const currentDate = ref(new Date())
const selectedDate = ref('')
const showEventDialog = ref(false)
const editingEvent = ref(false)
const submitting = ref(false)
const events = ref([
  { id: 1, title: '团队会议', type: 'meeting', date: '2024-01-15', time: '09:00', description: '讨论项目进展' },
  { id: 2, title: '项目截止', type: 'task', date: '2024-01-20', time: '18:00', description: '项目交付日期' },
  { id: 3, title: '生日提醒', type: 'reminder', date: '2024-01-25', time: '10:00', description: '同事生日' }
])

const eventFormRef = ref()
const eventForm = reactive({
  title: '',
  type: '',
  datetime: '',
  description: ''
})

const eventRules = {
  title: [{ required: true, message: '请输入日程标题', trigger: 'blur' }],
  type: [{ required: true, message: '请选择日程类型', trigger: 'change' }],
  datetime: [{ required: true, message: '请选择日期时间', trigger: 'change' }]
}

// 计算属性
const weekdays = computed(() => [$t('schedule.sun'), $t('schedule.mon'), $t('schedule.tue'), $t('schedule.wed'), $t('schedule.thu'), $t('schedule.fri'), $t('schedule.sat')])

const currentDateText = computed(() => {
  if (currentView.value === 'month') {
    return currentDate.value.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long' })
  } else if (currentView.value === 'week') {
    return `${currentDate.value.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })} 周`
  } else {
    return currentDate.value.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
  }
})

const monthDays = computed(() => {
  const year = currentDate.value.getFullYear()
  const month = currentDate.value.getMonth()
  const firstDay = new Date(year, month, 1)
  const lastDay = new Date(year, month + 1, 0)
  const startDate = new Date(firstDay)
  startDate.setDate(startDate.getDate() - firstDay.getDay())
  
  const days = []
  const today = new Date().toDateString()
  
  for (let i = 0; i < 42; i++) {
    const date = new Date(startDate)
    date.setDate(startDate.getDate() + i)
    
    days.push({
      date: date.toISOString().split('T')[0],
      day: date.getDate(),
      currentMonth: date.getMonth() === month,
      isToday: date.toDateString() === today
    })
  }
  
  return days
})

const weekDays = computed(() => {
  const startOfWeek = new Date(currentDate.value)
  startOfWeek.setDate(currentDate.value.getDate() - currentDate.value.getDay())
  
  const days = []
  const today = new Date().toDateString()
  
  for (let i = 0; i < 7; i++) {
    const date = new Date(startOfWeek)
    date.setDate(startOfWeek.getDate() + i)
    
    days.push({
      date: date.toISOString().split('T')[0],
      day: date.getDate(),
      dayName: weekdays[i],
      isToday: date.toDateString() === today
    })
  }
  
  return days
})

// 方法
const getEventsForDate = (date) => {
  return events.value.filter(event => event.date === date)
}

const selectDate = (date) => {
  selectedDate.value = date
}

const previousPeriod = () => {
  if (currentView.value === 'month') {
    currentDate.value.setMonth(currentDate.value.getMonth() - 1)
  } else if (currentView.value === 'week') {
    currentDate.value.setDate(currentDate.value.getDate() - 7)
  } else {
    currentDate.value.setDate(currentDate.value.getDate() - 1)
  }
  currentDate.value = new Date(currentDate.value)
}

const nextPeriod = () => {
  if (currentView.value === 'month') {
    currentDate.value.setMonth(currentDate.value.getMonth() + 1)
  } else if (currentView.value === 'week') {
    currentDate.value.setDate(currentDate.value.getDate() + 7)
  } else {
    currentDate.value.setDate(currentDate.value.getDate() + 1)
  }
  currentDate.value = new Date(currentDate.value)
}

const goToday = () => {
  currentDate.value = new Date()
  selectedDate.value = new Date().toISOString().split('T')[0]
}

const addEventForDate = () => {
  eventForm.datetime = selectedDate.value + ' 09:00'
  showEventDialog.value = true
}

const viewEvent = (event) => {
  ElMessage.info(`查看事件: ${event.title}`)
}

const editEvent = (event) => {
  editingEvent.value = true
  eventForm.title = event.title
  eventForm.type = event.type
  eventForm.datetime = `${event.date} ${event.time}`
  eventForm.description = event.description
  showEventDialog.value = true
}

const deleteEvent = async (event) => {
  try {
    await ElMessageBox.confirm('确认删除此日程吗？', '删除确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    events.value = events.value.filter(e => e.id !== event.id)
    ElMessage.success('删除成功！')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const submitEvent = async () => {
  try {
    await eventFormRef.value?.validate()
    submitting.value = true
    
    const [date, time] = eventForm.datetime.split(' ')
    const newEvent = {
      id: editingEvent.value ? editingEvent.value.id : Date.now(),
      title: eventForm.title,
      type: eventForm.type,
      date: date,
      time: time,
      description: eventForm.description
    }
    
    if (editingEvent.value) {
      const index = events.value.findIndex(e => e.id === editingEvent.value.id)
      events.value[index] = newEvent
      ElMessage.success('修改成功！')
    } else {
      events.value.push(newEvent)
      ElMessage.success('创建成功！')
    }
    
    showEventDialog.value = false
  } catch (error) {
    ElMessage.error('操作失败')
  } finally {
    submitting.value = false
  }
}

const resetEventForm = () => {
  editingEvent.value = false
  eventForm.title = ''
  eventForm.type = ''
  eventForm.datetime = ''
  eventForm.description = ''
  eventFormRef.value?.resetFields()
}

const formatSelectedDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN', { 
    year: 'numeric', 
    month: 'long', 
    day: 'numeric',
    weekday: 'long'
  })
}

const getEventTypeTag = (type) => {
  const types = {
    'meeting': 'primary',
    'task': 'warning',
    'reminder': 'info',
    'other': 'success'
  }
  return types[type] || 'info'
}

onMounted(() => {
  selectedDate.value = new Date().toISOString().split('T')[0]
})
</script>

<style scoped>
.schedule-container {
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

.calendar-section {
  margin-bottom: 24px;
}

.calendar-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.calendar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.date-navigation {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-date {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  min-width: 200px;
  text-align: center;
}

.month-view .weekdays {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  margin-bottom: 8px;
}

.weekday {
  padding: 12px;
  text-align: center;
  font-weight: 600;
  color: #666;
  background: #f5f5f5;
}

.days-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  background: #f0f0f0;
}

.day-cell {
  background: white;
  min-height: 80px;
  padding: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.day-cell:hover {
  background: #f8f9fa;
}

.day-cell.other-month {
  background: #fafafa;
  color: #ccc;
}

.day-cell.today {
  background: #e6f7ff;
}

.day-cell.selected {
  background: #1890ff;
  color: white;
}

.day-number {
  font-weight: 600;
  margin-bottom: 4px;
}

.event-item {
  font-size: 12px;
  padding: 2px 6px;
  margin-bottom: 2px;
  border-radius: 4px;
  background: #1890ff;
  color: white;
  cursor: pointer;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.event-item.meeting { background: #1890ff; }
.event-item.task { background: #faad14; }
.event-item.reminder { background: #13c2c2; }
.event-item.other { background: #52c41a; }

.week-view .week-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 1px;
  margin-bottom: 16px;
}

.week-day-header {
  text-align: center;
  padding: 12px;
  background: #f5f5f5;
}

.day-name {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.day-date {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.day-date.today {
  color: #1890ff;
}

.week-events {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 16px;
}

.week-event {
  padding: 8px;
  margin-bottom: 8px;
  border-radius: 6px;
  cursor: pointer;
  border-left: 4px solid #1890ff;
}

.week-event.meeting { border-left-color: #1890ff; background: #e6f7ff; }
.week-event.task { border-left-color: #faad14; background: #fff7e6; }
.week-event.reminder { border-left-color: #13c2c2; background: #e6fffb; }
.week-event.other { border-left-color: #52c41a; background: #f6ffed; }

.event-time {
  font-size: 12px;
  color: #666;
  margin-bottom: 2px;
}

.event-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.events-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: 1px solid #f0f0f0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.event-card {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 16px;
  margin-bottom: 12px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.event-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.event-info .event-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.event-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.event-time {
  font-size: 14px;
  color: #666;
}

.event-description {
  font-size: 14px;
  color: #999;
}

.event-actions {
  display: flex;
  gap: 8px;
}

.empty-events {
  text-align: center;
  padding: 48px 16px;
  color: #999;
}

.empty-events .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.dialog-footer {
  display: flex;
  gap: 12px;
}

@media (max-width: 768px) {
  .schedule-container {
    padding: 16px;
  }
  
  .calendar-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .day-cell {
    min-height: 60px;
  }
  
  .week-events {
    grid-template-columns: 1fr;
  }
}
</style>