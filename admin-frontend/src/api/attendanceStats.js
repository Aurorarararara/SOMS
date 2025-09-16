import request from '@/utils/request'

export const attendanceStatsApi = {
  // 获取考勤统计数据
  getAttendanceStatistics(params) {
    return request({
      url: '/api/admin/attendance/statistics',
      method: 'get',
      params
    })
  },

  // 获取部门考勤统计
  getDepartmentStatistics(departmentId, params) {
    return request({
      url: `/api/admin/attendance/departments/${departmentId}/statistics`,
      method: 'get',
      params
    })
  },

  // 导出考勤统计
  exportStatistics(params) {
    return request({
      url: '/api/admin/attendance/statistics/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}