import request from '@/utils/request'

/**
 * 日程管理API
 */
export default {
  // 创建日程
  createSchedule(data) {
    return request({
      url: '/schedule/create',
      method: 'post',
      data
    })
  },

  // 更新日程
  updateSchedule(id, data) {
    return request({
      url: `/schedule/update/${id}`,
      method: 'put',
      data
    })
  },

  // 删除日程
  deleteSchedule(id) {
    return request({
      url: `/schedule/delete/${id}`,
      method: 'delete'
    })
  },

  // 获取日程详情
  getScheduleDetail(id) {
    return request({
      url: `/schedule/detail/${id}`,
      method: 'get'
    })
  },

  // 获取用户日程列表
  getUserSchedules(params) {
    return request({
      url: '/schedule/list',
      method: 'get',
      params
    })
  },

  // 获取今日日程
  getTodaySchedules() {
    return request({
      url: '/schedule/today',
      method: 'get'
    })
  },

  // 获取即将到来的日程
  getUpcomingSchedules(params) {
    return request({
      url: '/schedule/upcoming',
      method: 'get',
      params
    })
  },

  // 搜索日程
  searchSchedules(params) {
    return request({
      url: '/schedule/search',
      method: 'get',
      params
    })
  },

  // 检查日程冲突
  checkScheduleConflict(params) {
    return request({
      url: '/schedule/conflict-check',
      method: 'get',
      params
    })
  },

  // 获取日程统计
  getScheduleStatistics(params) {
    return request({
      url: '/schedule/statistics',
      method: 'get',
      params
    })
  },

  // 更新日程状态
  updateScheduleStatus(id, status) {
    return request({
      url: `/schedule/status/${id}`,
      method: 'put',
      params: { status }
    })
  },

  // 复制日程
  copySchedule(id, newStartTime) {
    return request({
      url: `/schedule/copy/${id}`,
      method: 'post',
      params: { newStartTime }
    })
  },

  // 批量删除日程
  batchDeleteSchedules(scheduleIds) {
    return request({
      url: '/schedule/batch-delete',
      method: 'delete',
      data: scheduleIds
    })
  },

  // 导出日程
  exportSchedules(params) {
    return request({
      url: '/schedule/export',
      method: 'get',
      params
    })
  },

  // 获取日程参与者
  getScheduleParticipants(id) {
    return request({
      url: `/schedule/participants/${id}`,
      method: 'get'
    })
  },

  // 更新参与状态
  updateParticipantStatus(id, status, notes) {
    return request({
      url: `/schedule/participant-status/${id}`,
      method: 'put',
      params: { status, notes }
    })
  },

  // 获取今日提醒
  getTodayReminders() {
    return request({
      url: '/schedule/reminders/today',
      method: 'get'
    })
  },

  // 检查日程冲突
  checkConflicts() {
    return request({
      url: '/schedule/conflicts',
      method: 'get'
    })
  },

  // 同步日程
  syncSchedules() {
    return request({
      url: '/schedule/sync',
      method: 'post'
    })
  },

  // 获取同步设置
  getSyncSettings() {
    return request({
      url: '/schedule/sync/settings',
      method: 'get'
    })
  },

  // 更新同步设置
  updateSyncSettings(settings) {
    return request({
      url: '/schedule/sync/settings',
      method: 'put',
      data: settings
    })
  }
}