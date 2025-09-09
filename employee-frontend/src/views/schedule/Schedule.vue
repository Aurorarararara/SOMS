<template>
  <div class="schedule-container">
    <!-- 头部工具栏 -->
    <div class="schedule-header">
      <div class="header-left">
        <h2 class="page-title">
          <el-icon><Calendar /></el-icon>
          日程管理
        </h2>
        <div class="date-navigation">
          <el-button-group>
            <el-button @click="goToPrevious" :icon="ArrowLeft" />
            <el-button @click="goToToday">今天</el-button>
            <el-button @click="goToNext" :icon="ArrowRight" />
          </el-button-group>
          <span class="current-date">{{ currentDateText }}</span>
        </div>
      </div>
      
      <div class="header-right">
        <div class="view-modes">
          <el-radio-group v-model="viewMode" @change="handleViewModeChange">
            <el-radio-button label="month">月视图</el-radio-button>
            <el-radio-button label="week">周视图</el-radio-button>
            <el-radio-button label="day">日视图</el-radio-button>
          </el-radio-group>
        </div>
        
        <div class="action-buttons">
          <el-button type="primary" @click="showCreateDialog" :icon="Plus">
            新建日程
          </el-button>
          <el-dropdown @command="handleMoreAction">
            <el-button :icon="More" />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="search">搜索日程</el-dropdown-item>
                <el-dropdown-item command="export">导出日程</el-dropdown-item>
                <el-dropdown-item command="statistics">统计报表</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="schedule-content">
      <!-- 侧边栏 -->
      <div class="schedule-sidebar">
        <!-- 今日日程 -->
        <div class="sidebar-section">
          <h3 class="section-title">今日日程</h3>
          <div class="today-schedules">
            <div 
              v-for="schedule in todaySchedules" 
              :key="schedule.id"
              class="today-schedule-item"
              @click="viewScheduleDetail(schedule.id)"
            >
              <div class="schedule-time">
                {{ formatTime(schedule.start_time) }}
              </div>
              <div class="schedule-info">
                <div class="schedule-title">{{ schedule.title }}</div>
                <div class="schedule-location" v-if="schedule.location">
                  <el-icon><Location /></el-icon>
                  {{ schedule.location }}
                </div>
              </div>
              <div 
                class="schedule-status" 
                :class="schedule.status.toLowerCase()"
              >
                {{ getStatusText(schedule.status) }}
              </div>
            </div>
            <div v-if="todaySchedules.length === 0" class="empty-state">
              <el-icon><Calendar /></el-icon>
              <p>今日暂无日程</p>
            </div>
          </div>
        </div>

        <!-- 即将到来的日程 -->
        <div class="sidebar-section">
          <h3 class="section-title">即将到来</h3>
          <div class="upcoming-schedules">
            <div 
              v-for="schedule in upcomingSchedules" 
              :key="schedule.id"
              class="upcoming-schedule-item"
              @click="viewScheduleDetail(schedule.id)"
            >
              <div class="schedule-date">
                {{ formatDate(schedule.start_time) }}
              </div>
              <div class="schedule-info">
                <div class="schedule-title">{{ schedule.title }}</div>
                <div class="schedule-time">
                  {{ formatTime(schedule.start_time) }} - {{ formatTime(schedule.end_time) }}
                </div>
              </div>
            </div>
            <div v-if="upcomingSchedules.length === 0" class="empty-state">
              <el-icon><Clock /></el-icon>
              <p>暂无即将到来的日程</p>
            </div>
          </div>
        </div>

        <!-- 统计信息 -->
        <div class="sidebar-section">
          <h3 class="section-title">本月统计</h3>
          <div class="statistics-grid">
            <div class="stat-item">
              <div class="stat-number">{{ statistics.total_count || 0 }}</div>
              <div class="stat-label">总日程</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ statistics.completed_count || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ statistics.today_count || 0 }}</div>
              <div class="stat-label">今日</div>
            </div>
            <div class="stat-item">
              <div class="stat-number">{{ statistics.week_count || 0 }}</div>
              <div class="stat-label">本周</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 日历视图区域 -->
      <div class="schedule-main">
        <div v-loading="loading" class="calendar-container">
          <!-- 月视图 -->
          <MonthView 
            v-if="viewMode === 'month'"
            :schedules="currentMonthSchedules"
            :current-date="currentDate"
            :selected-date="selectedDate"
            @date-click="handleDateClick"
            @schedule-click="viewScheduleDetail"
            @schedule-create="handleScheduleCreate"
          />
          
          <!-- 周视图 -->
          <WeekView 
            v-if="viewMode === 'week'"
            :schedules="currentWeekSchedules"
            :current-date="currentDate"
            @schedule-click="viewScheduleDetail"
            @schedule-create="handleScheduleCreate"
            @schedule-update="handleScheduleUpdate"
          />
          
          <!-- 日视图 -->
          <DayView 
            v-if="viewMode === 'day'"
            :schedules="currentDaySchedules"
            :selected-date="selectedDate"
            @schedule-click="viewScheduleDetail"
            @schedule-create="handleScheduleCreate"
            @schedule-update="handleScheduleUpdate"
          />
        </div>
      </div>
    </div>

    <!-- 日程创建/编辑对话框 -->
    <ScheduleDialog
      v-model="dialogVisible"
      :schedule="currentSchedule"
      :mode="dialogMode"
      @confirm="handleScheduleSubmit"
      @cancel="handleDialogCancel"
    />

    <!-- 日程详情对话框 -->
    <ScheduleDetailDialog
      v-model="detailDialogVisible"
      :schedule-id="selectedScheduleId"
      @edit="handleEditSchedule"
      @delete="handleDeleteSchedule"
    />

    <!-- 搜索对话框 -->
    <ScheduleSearchDialog
      v-model="searchDialogVisible"
      @search="handleSearch"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useScheduleStore } from '@/stores/schedule'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Calendar, 
  ArrowLeft, 
  ArrowRight, 
  Plus, 
  More, 
  Location, 
  Clock,
  Bell,
  Warning,
  Refresh,
  Search,
  Download,
  Setting
} from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// 组件导入
import MonthView from './components/MonthView.vue'
import WeekView from './components/WeekView.vue'
import DayView from './components/DayView.vue'
import ScheduleDialog from './components/ScheduleDialog.vue'
import ScheduleDetailDialog from './components/ScheduleDetailDialog.vue'
import ScheduleSearchDialog from './components/ScheduleSearchDialog.vue'

// Store
const scheduleStore = useScheduleStore()

// 响应式数据
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const searchDialogVisible = ref(false)
const syncSettingsVisible = ref(false)
const dialogMode = ref('create') // create, edit
const currentSchedule = ref(null)
const selectedScheduleId = ref(null)

// 提醒和同步相关状态
const todayRemindersCount = ref(0)
const conflictsCount = ref(0)
const syncStatus = ref({
  enabled: false,
  syncing: false,
  text: '同步已禁用'
})

const syncSettings = ref({
  enabled: false,
  interval: 30,
  conflictResolution: 'MANUAL'
})

const showQuickActions = computed(() => {
  return todayRemindersCount.value > 0 || conflictsCount.value > 0 || syncStatus.value.enabled
})

// 计算属性
const viewMode = computed({
  get: () => scheduleStore.viewMode,
  set: (value) => scheduleStore.setViewMode(value)
})

const currentDate = computed(() => scheduleStore.currentDate)
const selectedDate = computed(() => scheduleStore.selectedDate)
const loading = computed(() => scheduleStore.loading)
const todaySchedules = computed(() => scheduleStore.todaySchedules)
const upcomingSchedules = computed(() => scheduleStore.upcomingSchedules)
const statistics = computed(() => scheduleStore.statistics)
const currentMonthSchedules = computed(() => scheduleStore.currentMonthSchedules)
const currentWeekSchedules = computed(() => scheduleStore.currentWeekSchedules)
const currentDaySchedules = computed(() => scheduleStore.currentDaySchedules)

const currentDateText = computed(() => {
  switch (viewMode.value) {
    case 'month':
      return currentDate.value.format('YYYY年MM月')
    case 'week':
      const weekStart = currentDate.value.startOf('week')
      const weekEnd = currentDate.value.endOf('week')
      return `${weekStart.format('MM月DD日')} - ${weekEnd.format('MM月DD日')}`
    case 'day':
      return selectedDate.value.format('YYYY年MM月DD日')
    default:
      return ''
  }
})

// 方法
const goToPrevious = () => {
  scheduleStore.goToPrevious()
}

const goToNext = () => {
  scheduleStore.goToNext()
}

const goToToday = () => {
  scheduleStore.goToToday()
}

const handleViewModeChange = (mode) => {
  scheduleStore.setViewMode(mode)
}

const handleDateClick = (date) => {
  scheduleStore.setSelectedDate(date)
  if (viewMode.value !== 'day') {
    scheduleStore.setViewMode('day')
  }
}

const showCreateDialog = () => {
  currentSchedule.value = null
  dialogMode.value = 'create'
  dialogVisible.value = true
}

const handleScheduleCreate = (dateTime) => {
  currentSchedule.value = {
    start_time: dateTime,
    end_time: dayjs(dateTime).add(1, 'hour').format('YYYY-MM-DD HH:mm:ss')
  }
  dialogMode.value = 'create'
  dialogVisible.value = true
}

const handleScheduleUpdate = (schedule) => {
  currentSchedule.value = schedule
  dialogMode.value = 'edit'
  dialogVisible.value = true
}

const handleEditSchedule = (schedule) => {
  currentSchedule.value = schedule
  dialogMode.value = 'edit'
  dialogVisible.value = true
  detailDialogVisible.value = false
}

const viewScheduleDetail = (scheduleId) => {
  selectedScheduleId.value = scheduleId
  detailDialogVisible.value = true
}

const handleScheduleSubmit = async (scheduleData) => {
  try {
    if (dialogMode.value === 'create') {
      await scheduleStore.createSchedule(scheduleData)
    } else {
      await scheduleStore.updateSchedule(currentSchedule.value.id, scheduleData)
    }
    dialogVisible.value = false
  } catch (error) {
    console.error('保存日程失败:', error)
  }
}

const handleDeleteSchedule = async (scheduleId) => {
  try {
    await ElMessageBox.confirm('确定要删除这个日程吗？', '确认删除', {
      type: 'warning'
    })
    
    await scheduleStore.deleteSchedule(scheduleId)
    detailDialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除日程失败:', error)
    }
  }
}

const handleDialogCancel = () => {
  dialogVisible.value = false
  currentSchedule.value = null
}

const handleMoreAction = (command) => {
  switch (command) {
    case 'search':
      searchDialogVisible.value = true
      break
    case 'sync':
      handleSync()
      break
    case 'export':
      handleExport()
      break
    case 'settings':
      syncSettingsVisible.value = true
      break
  }
}

const handleMenuCommand = (command) => {
  handleMoreAction(command)
}

const handleSearch = (searchParams) => {
  // TODO: 实现搜索功能
  console.log('搜索参数:', searchParams)
}

const handleSync = async () => {
  try {
    syncStatus.value.syncing = true
    syncStatus.value.text = '同步中...'
    
    await scheduleStore.syncSchedules()
    
    syncStatus.value.text = '同步完成'
    ElMessage.success('日程同步成功')
    
    // 刷新数据
    await scheduleStore.refreshCurrentView()
    
  } catch (error) {
    console.error('同步失败:', error)
    syncStatus.value.text = '同步失败'
    ElMessage.error('日程同步失败')
  } finally {
    syncStatus.value.syncing = false
    setTimeout(() => {
      if (syncStatus.value.enabled) {
        syncStatus.value.text = '自动同步已启用'
      }
    }, 3000)
  }
}

const saveSyncSettings = async () => {
  try {
    await scheduleStore.updateSyncSettings(syncSettings.value)
    
    syncStatus.value.enabled = syncSettings.value.enabled
    syncStatus.value.text = syncSettings.value.enabled ? '自动同步已启用' : '同步已禁用'
    
    syncSettingsVisible.value = false
    ElMessage.success('同步设置已保存')
  } catch (error) {
    console.error('保存同步设置失败:', error)
    ElMessage.error('保存同步设置失败')
  }
}

const handleExport = () => {
  // TODO: 实现导出功能
  ElMessage.info('导出功能开发中...')
}

const handleScheduleDetailFromSearch = (scheduleId) => {
  searchDialogVisible.value = false
  viewScheduleDetail(scheduleId)
}

const handleEditFromDetail = (schedule) => {
  detailDialogVisible.value = false
  handleEditSchedule(schedule)
}

const handleDeleteFromDetail = async (scheduleId) => {
  detailDialogVisible.value = false
  await handleDeleteSchedule(scheduleId)
}

// 初始化提醒和同步状态
const initializeRemindersAndSync = async () => {
  try {
    // 获取今日提醒数量
    const reminders = await scheduleStore.getTodayReminders()
    todayRemindersCount.value = reminders.length
    
    // 检查时间冲突
    const conflicts = await scheduleStore.checkScheduleConflicts()
    conflictsCount.value = conflicts.length
    
    // 获取同步设置
    const syncConfig = await scheduleStore.getSyncSettings()
    syncSettings.value = { ...syncConfig }
    syncStatus.value.enabled = syncConfig.enabled
    syncStatus.value.text = syncConfig.enabled ? '自动同步已启用' : '同步已禁用'
    
  } catch (error) {
    console.error('初始化提醒和同步状态失败:', error)
  }
}

const formatTime = (dateTime) => {
  return dayjs(dateTime).format('HH:mm')
}

const formatDate = (dateTime) => {
  return dayjs(dateTime).format('MM-DD')
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

// 生命周期
onMounted(async () => {
  await scheduleStore.init()
  await initializeRemindersAndSync()
})

// 监听路由变化
watch(() => selectedDate.value, () => {
  if (viewMode.value === 'day') {
    scheduleStore.refreshCurrentView()
  }
})
</script>

<style scoped>
.schedule-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #f5f7fa;
}

.schedule-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background: white;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.date-navigation {
  display: flex;
  align-items: center;
  gap: 16px;
}

.current-date {
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  min-width: 120px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.schedule-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.schedule-sidebar {
  width: 300px;
  background: white;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.sidebar-section {
  padding: 20px;
  border-bottom: 1px solid #f0f2f5;
}

.sidebar-section:last-child {
  border-bottom: none;
}

.section-title {
  margin: 0 0 16px 0;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.today-schedule-item,
.upcoming-schedule-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  margin-bottom: 8px;
  background: #f8f9fa;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.today-schedule-item:hover,
.upcoming-schedule-item:hover {
  background: #e3f2fd;
  transform: translateY(-1px);
}

.schedule-time {
  font-size: 12px;
  font-weight: 500;
  color: #409eff;
  min-width: 40px;
}

.schedule-date {
  font-size: 12px;
  font-weight: 500;
  color: #909399;
  min-width: 40px;
}

.schedule-info {
  flex: 1;
}

.schedule-title {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.schedule-location {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.schedule-status {
  font-size: 12px;
  padding: 2px 8px;
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

.statistics-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.stat-item {
  text-align: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stat-number {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  color: #909399;
}

.schedule-main {
  flex: 1;
  overflow: hidden;
}

.calendar-container {
  height: 100%;
  background: white;
}

.empty-state {
  text-align: center;
  padding: 20px;
  color: #909399;
}

.empty-state .el-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}
</style>