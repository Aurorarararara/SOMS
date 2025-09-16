import request from '@/utils/request'

export const attendanceApi = {
  // 获取考勤记录列表（分页）
  getAttendanceRecords(params) {
    return request({
      url: '/api/admin/attendance/records',
      method: 'get',
      params
    })
  },

  // 获取考勤记录详情
  getAttendanceDetail(recordId) {
    return request({
      url: `/api/admin/attendance/records/${recordId}`,
      method: 'get'
    })
  },

  // 更新考勤记录
  updateAttendanceRecord(recordId, data) {
    return request({
      url: `/api/admin/attendance/records/${recordId}`,
      method: 'put',
      data
    })
  },

  // 删除考勤记录
  deleteAttendanceRecord(recordId) {
    return request({
      url: `/api/admin/attendance/records/${recordId}`,
      method: 'delete'
    })
  },

  // 批量导入考勤记录
  importAttendanceRecords(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request({
      url: '/api/admin/attendance/records/import',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 导出考勤记录
  exportAttendanceRecords(params) {
    return request({
      url: '/api/admin/attendance/records/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

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

  // 考勤规则管理
  getAttendanceRules() {
    return request({
      url: '/api/admin/attendance/rules',
      method: 'get'
    })
  },

  // 创建考勤规则
  createAttendanceRule(data) {
    return request({
      url: '/api/admin/attendance/rules',
      method: 'post',
      data
    })
  },

  // 更新考勤规则
  updateAttendanceRule(ruleId, data) {
    return request({
      url: `/api/admin/attendance/rules/${ruleId}`,
      method: 'put',
      data
    })
  },

  // 删除考勤规则
  deleteAttendanceRule(ruleId) {
    return request({
      url: `/api/admin/attendance/rules/${ruleId}`,
      method: 'delete'
    })
  },

  // 启用/禁用考勤规则
  toggleAttendanceRule(ruleId, enabled) {
    return request({
      url: `/api/admin/attendance/rules/${ruleId}/toggle`,
      method: 'put',
      data: { enabled }
    })
  }
}