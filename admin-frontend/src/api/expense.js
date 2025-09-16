import request from '@/utils/request'

/**
 * 报销管理API
 */
export default {
  // 管理员API - 获取所有报销申请
  getAllApplications(params) {
    return request({
      url: '/api/admin/expense/applications',
      method: 'get',
      params
    })
  },

  // 管理员API - 获取待审批申请
  getPendingApplications(params) {
    return request({
      url: '/api/admin/expense/applications/pending',
      method: 'get',
      params
    })
  },

  // 管理员API - 获取申请详情
  getApplicationDetailForAdmin(id) {
    return request({
      url: `/api/admin/expense/applications/${id}`,
      method: 'get'
    })
  },

  // 管理员API - 审批申请
  approveApplication(id, data) {
    return request({
      url: `/api/admin/expense/applications/${id}/approve`,
      method: 'post',
      data
    })
  },

  // 管理员API - 拒绝申请
  rejectApplication(id, data) {
    return request({
      url: `/api/admin/expense/applications/${id}/reject`,
      method: 'post',
      data
    })
  },

  // 管理员API - 批量审批申请
  batchApproveApplications(data) {
    return request({
      url: '/api/admin/expense/applications/batch-approve',
      method: 'post',
      data
    })
  },

  // 管理员API - 批量拒绝申请
  batchRejectApplications(data) {
    return request({
      url: '/api/admin/expense/applications/batch-reject',
      method: 'post',
      data
    })
  },

  // 管理员API - 获取统计数据
  getAdminStatistics(params) {
    return request({
      url: '/api/admin/expense/statistics',
      method: 'get',
      params
    })
  },

  // 管理员API - 获取部门排名
  getDepartmentRanking(params) {
    return request({
      url: '/api/admin/expense/department-ranking',
      method: 'get',
      params
    })
  },

  // 管理员API - 获取员工排名
  getEmployeeRanking(params) {
    return request({
      url: '/api/admin/expense/employee-ranking',
      method: 'get',
      params
    })
  },

  // 管理员API - 导出所有申请数据
  exportAllApplications(params) {
    return request({
      url: '/api/admin/expense/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  // 管理员API - 获取审批工作量统计
  getApprovalWorkload(params) {
    return request({
      url: '/api/admin/expense/approval-workload',
      method: 'get',
      params
    })
  },

  // 管理员API - 设置申请优先级
  setApplicationPriority(id, data) {
    return request({
      url: `/api/admin/expense/applications/${id}/priority`,
      method: 'put',
      data
    })
  },

  // 管理员API - 删除申请
  deleteApplication(id) {
    return request({
      url: `/api/admin/expense/applications/${id}`,
      method: 'delete'
    })
  },

  // 管理员API - 获取审批历史
  getApprovalHistory(id) {
    return request({
      url: `/api/admin/expense/applications/${id}/approval-history`,
      method: 'get'
    })
  }
}