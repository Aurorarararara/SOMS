import request from '@/utils/request'

export const performanceApi = {
  // 获取绩效统计数据
  getPerformanceStatistics(params) {
    return request({
      url: '/api/admin/performance/statistics',
      method: 'get',
      params
    })
  },

  // 获取部门绩效统计
  getDepartmentPerformance(departmentId, params) {
    return request({
      url: `/api/admin/performance/statistics/departments/${departmentId}`,
      method: 'get',
      params
    })
  },

  // 导出绩效统计
  exportStatistics(params) {
    return request({
      url: '/api/admin/performance/statistics/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}