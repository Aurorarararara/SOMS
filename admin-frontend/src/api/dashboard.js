import request from '@/utils/request'

export const dashboardApi = {
  // 获取总体统计数据
  getOverallStats() {
    return request({
      url: '/admin/dashboard/stats',
      method: 'get'
    })
  },

  // 获取考勤统计数据（按月）
  getAttendanceStats(month) {
    return request({
      url: '/admin/dashboard/attendance-stats',
      method: 'get',
      params: { month }
    })
  },

  // 获取部门人员分布数据
  getDepartmentDistribution() {
    return request({
      url: '/admin/dashboard/department-distribution',
      method: 'get'
    })
  },

  // 获取最新公告
  getRecentAnnouncements(limit = 5) {
    return request({
      url: '/admin/announcements/recent',
      method: 'get',
      params: { limit }
    })
  },

  // 获取待处理任务
  getPendingTasks(limit = 5) {
    return request({
      url: '/admin/tasks/pending',
      method: 'get',
      params: { limit }
    })
  },

  // 获取系统消息
  getSystemMessages(limit = 5) {
    return request({
      url: '/admin/messages/system',
      method: 'get',
      params: { limit }
    })
  }
}