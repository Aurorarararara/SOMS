import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import scheduleApi from '@/api/schedule'
import { ElMessage, ElNotification } from 'element-plus'
import dayjs from 'dayjs'

export const useScheduleStore = defineStore('schedule', () => {
  // 状态
  const schedules = ref([])
  const currentSchedule = ref(null)
  const loading = ref(false)
  const currentDate = ref(dayjs())
  const viewMode = ref('month') // month, week, day
  const selectedDate = ref(dayjs())
  const categories = ref([])
  const statistics = ref({})
  const upcomingSchedules = ref([])
  const todaySchedules = ref([])

  // 计算属性
  const currentMonthSchedules = computed(() => {
    const startOfMonth = currentDate.value.startOf('month')
    const endOfMonth = currentDate.value.endOf('month')
    
    return schedules.value.filter(schedule => {
      const scheduleDate = dayjs(schedule.start_time)
      return scheduleDate.isAfter(startOfMonth.subtract(1, 'day')) && 
             scheduleDate.isBefore(endOfMonth.add(1, 'day'))
    })
  })

  const currentWeekSchedules = computed(() => {
    const startOfWeek = currentDate.value.startOf('week')
    const endOfWeek = currentDate.value.endOf('week')
    
    return schedules.value.filter(schedule => {
      const scheduleDate = dayjs(schedule.start_time)
      return scheduleDate.isAfter(startOfWeek.subtract(1, 'day')) && 
             scheduleDate.isBefore(endOfWeek.add(1, 'day'))
    })
  })

  const currentDaySchedules = computed(() => {
    const targetDate = selectedDate.value.format('YYYY-MM-DD')
    
    return schedules.value.filter(schedule => {
      const scheduleDate = dayjs(schedule.start_time).format('YYYY-MM-DD')
      return scheduleDate === targetDate
    }).sort((a, b) => dayjs(a.start_time).valueOf() - dayjs(b.start_time).valueOf())
  })

  const schedulesByDate = computed(() => {
    const result = {}
    schedules.value.forEach(schedule => {
      const date = dayjs(schedule.start_time).format('YYYY-MM-DD')
      if (!result[date]) {
        result[date] = []
      }
      result[date].push(schedule)
    })
    return result
  })

  // 获取日程列表
  const fetchSchedules = async (startDate, endDate, includePrivate = true) => {
    try {
      loading.value = true
      const response = await scheduleApi.getUserSchedules({
        startDate: startDate || currentDate.value.startOf('month').format('YYYY-MM-DD'),
        endDate: endDate || currentDate.value.endOf('month').format('YYYY-MM-DD'),
        includePrivate
      })
      
      if (response.code === 200) {
        schedules.value = response.data || []
      }
    } catch (error) {
      console.error('获取日程列表失败:', error)
      ElMessage.error('获取日程列表失败')
    } finally {
      loading.value = false
    }
  }

  // 获取今日日程
  const fetchTodaySchedules = async () => {
    try {
      const response = await scheduleApi.getTodaySchedules()
      if (response.code === 200) {
        todaySchedules.value = response.data || []
      }
    } catch (error) {
      console.error('获取今日日程失败:', error)
    }
  }

  // 获取即将到来的日程
  const fetchUpcomingSchedules = async (hours = 24, limit = 10) => {
    try {
      const response = await scheduleApi.getUpcomingSchedules({ hours, limit })
      if (response.code === 200) {
        upcomingSchedules.value = response.data || []
      }
    } catch (error) {
      console.error('获取即将到来的日程失败:', error)
    }
  }

  // 创建日程
  const createSchedule = async (scheduleData) => {
    try {
      loading.value = true
      const response = await scheduleApi.createSchedule(scheduleData)
      
      if (response.code === 200) {
        ElMessage.success('日程创建成功')
        await refreshCurrentView()
        return response.data
      } else {
        throw new Error(response.message)
      }
    } catch (error) {
      console.error('创建日程失败:', error)
      ElMessage.error('创建日程失败: ' + error.message)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 更新日程
  const updateSchedule = async (id, scheduleData) => {
    try {
      loading.value = true
      const response = await scheduleApi.updateSchedule(id, scheduleData)
      
      if (response.code === 200) {
        ElMessage.success('日程更新成功')
        await refreshCurrentView()
        return response.data
      } else {
        throw new Error(response.message)
      }
    } catch (error) {
      console.error('更新日程失败:', error)
      ElMessage.error('更新日程失败: ' + error.message)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 删除日程
  const deleteSchedule = async (id) => {
    try {
      loading.value = true
      const response = await scheduleApi.deleteSchedule(id)
      
      if (response.code === 200) {
        ElMessage.success('日程删除成功')
        await refreshCurrentView()
        return true
      } else {
        throw new Error(response.message)
      }
    } catch (error) {
      console.error('删除日程失败:', error)
      ElMessage.error('删除日程失败: ' + error.message)
      throw error
    } finally {
      loading.value = false
    }
  }

  // 获取日程详情
  const fetchScheduleDetail = async (id) => {
    try {
      const response = await scheduleApi.getScheduleDetail(id)
      if (response.code === 200) {
        currentSchedule.value = response.data
        return response.data
      }
    } catch (error) {
      console.error('获取日程详情失败:', error)
      ElMessage.error('获取日程详情失败')
    }
  }

  // 检查日程冲突
  const checkScheduleConflict = async (startTime, endTime, excludeId = null) => {
    try {
      const response = await scheduleApi.checkScheduleConflict({
        startTime,
        endTime,
        excludeScheduleId: excludeId
      })
      
      if (response.code === 200) {
        return response.data || []
      }
      return []
    } catch (error) {
      console.error('检查日程冲突失败:', error)
      return []
    }
  }

  // 更新日程状态
  const updateScheduleStatus = async (id, status) => {
    try {
      const response = await scheduleApi.updateScheduleStatus(id, status)
      if (response.code === 200) {
        ElMessage.success('状态更新成功')
        await refreshCurrentView()
        return true
      }
    } catch (error) {
      console.error('更新日程状态失败:', error)
      ElMessage.error('更新状态失败')
      return false
    }
  }

  // 复制日程
  const copySchedule = async (id, newStartTime) => {
    try {
      const response = await scheduleApi.copySchedule(id, newStartTime)
      if (response.code === 200) {
        ElMessage.success('日程复制成功')
        await refreshCurrentView()
        return response.data
      }
    } catch (error) {
      console.error('复制日程失败:', error)
      ElMessage.error('复制日程失败')
    }
  }

  // 获取统计信息
  const fetchStatistics = async (startDate, endDate) => {
    try {
      const response = await scheduleApi.getScheduleStatistics({
        startDate: startDate || currentDate.value.startOf('month').format('YYYY-MM-DD'),
        endDate: endDate || currentDate.value.endOf('month').format('YYYY-MM-DD')
      })
      
      if (response.code === 200) {
        statistics.value = response.data || {}
      }
    } catch (error) {
      console.error('获取统计信息失败:', error)
    }
  }

  // 搜索日程
  const searchSchedules = async (searchParams) => {
    try {
      loading.value = true
      const response = await scheduleApi.searchSchedules(searchParams)
      
      if (response.code === 200) {
        return response.data
      }
      return { records: [], total: 0 }
    } catch (error) {
      console.error('搜索日程失败:', error)
      ElMessage.error('搜索失败')
      return { records: [], total: 0 }
    } finally {
      loading.value = false
    }
  }

  // 刷新当前视图
  const refreshCurrentView = async () => {
    let startDate, endDate
    
    switch (viewMode.value) {
      case 'month':
        startDate = currentDate.value.startOf('month').format('YYYY-MM-DD')
        endDate = currentDate.value.endOf('month').format('YYYY-MM-DD')
        break
      case 'week':
        startDate = currentDate.value.startOf('week').format('YYYY-MM-DD')
        endDate = currentDate.value.endOf('week').format('YYYY-MM-DD')
        break
      case 'day':
        startDate = selectedDate.value.format('YYYY-MM-DD')
        endDate = selectedDate.value.format('YYYY-MM-DD')
        break
    }
    
    await fetchSchedules(startDate, endDate)
    await fetchTodaySchedules()
    await fetchUpcomingSchedules()
    await fetchStatistics(startDate, endDate)
  }

  // 切换视图模式
  const setViewMode = (mode) => {
    viewMode.value = mode
    refreshCurrentView()
  }

  // 切换日期
  const setCurrentDate = (date) => {
    currentDate.value = dayjs(date)
    refreshCurrentView()
  }

  // 选择日期
  const setSelectedDate = (date) => {
    selectedDate.value = dayjs(date)
    if (viewMode.value === 'day') {
      refreshCurrentView()
    }
  }

  // 导航到今天
  const goToToday = () => {
    const today = dayjs()
    currentDate.value = today
    selectedDate.value = today
    refreshCurrentView()
  }

  // 上一个周期
  const goToPrevious = () => {
    switch (viewMode.value) {
      case 'month':
        currentDate.value = currentDate.value.subtract(1, 'month')
        break
      case 'week':
        currentDate.value = currentDate.value.subtract(1, 'week')
        break
      case 'day':
        selectedDate.value = selectedDate.value.subtract(1, 'day')
        currentDate.value = selectedDate.value
        break
    }
    refreshCurrentView()
  }

  // 下一个周期
  const goToNext = () => {
    switch (viewMode.value) {
      case 'month':
        currentDate.value = currentDate.value.add(1, 'month')
        break
      case 'week':
        currentDate.value = currentDate.value.add(1, 'week')
        break
      case 'day':
        selectedDate.value = selectedDate.value.add(1, 'day')
        currentDate.value = selectedDate.value
        break
    }
    refreshCurrentView()
  }

  // 处理日程提醒
  const handleScheduleReminder = (reminder) => {
    ElNotification({
      title: '日程提醒',
      message: reminder.message,
      type: 'info',
      duration: 0,
      showClose: true,
      onClick: () => {
        // 跳转到日程详情
        if (reminder.schedule_id) {
          fetchScheduleDetail(reminder.schedule_id)
        }
      }
    })
  }

  // 获取今日提醒
  const getTodayReminders = async () => {
    try {
      const response = await scheduleApi.getTodayReminders()
      if (response.code === 200) {
        return response.data || []
      }
      return []
    } catch (error) {
      console.error('获取今日提醒失败:', error)
      return []
    }
  }

  // 检查日程冲突
  const checkScheduleConflicts = async () => {
    try {
      const response = await scheduleApi.checkConflicts()
      if (response.code === 200) {
        return response.data || []
      }
      return []
    } catch (error) {
      console.error('检查日程冲突失败:', error)
      return []
    }
  }

  // 同步日程
  const syncSchedules = async () => {
    try {
      const response = await scheduleApi.syncSchedules()
      if (response.code === 200) {
        await refreshCurrentView()
        return response.data
      }
      throw new Error(response.message)
    } catch (error) {
      console.error('同步日程失败:', error)
      throw error
    }
  }

  // 获取同步设置
  const getSyncSettings = async () => {
    try {
      const response = await scheduleApi.getSyncSettings()
      if (response.code === 200) {
        return response.data || {
          enabled: false,
          interval: 30,
          conflictResolution: 'MANUAL'
        }
      }
      return {
        enabled: false,
        interval: 30,
        conflictResolution: 'MANUAL'
      }
    } catch (error) {
      console.error('获取同步设置失败:', error)
      return {
        enabled: false,
        interval: 30,
        conflictResolution: 'MANUAL'
      }
    }
  }

  // 更新同步设置
  const updateSyncSettings = async (settings) => {
    try {
      const response = await scheduleApi.updateSyncSettings(settings)
      if (response.code === 200) {
        return response.data
      }
      throw new Error(response.message)
    } catch (error) {
      console.error('更新同步设置失败:', error)
      throw error
    }
  }

  // 初始化
  const init = async () => {
    await refreshCurrentView()
  }

  return {
    // 状态
    schedules,
    currentSchedule,
    loading,
    currentDate,
    viewMode,
    selectedDate,
    categories,
    statistics,
    upcomingSchedules,
    todaySchedules,
    
    // 计算属性
    currentMonthSchedules,
    currentWeekSchedules,
    currentDaySchedules,
    schedulesByDate,
    
    // 方法
    fetchSchedules,
    fetchTodaySchedules,
    fetchUpcomingSchedules,
    createSchedule,
    updateSchedule,
    deleteSchedule,
    fetchScheduleDetail,
    checkScheduleConflict,
    updateScheduleStatus,
    copySchedule,
    fetchStatistics,
    searchSchedules,
    refreshCurrentView,
    setViewMode,
    setCurrentDate,
    setSelectedDate,
    goToToday,
    goToPrevious,
    goToNext,
    handleScheduleReminder,
    getTodayReminders,
    checkScheduleConflicts,
    syncSchedules,
    getSyncSettings,
    updateSyncSettings,
    init
  }
})