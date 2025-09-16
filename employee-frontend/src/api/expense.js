import request from '@/utils/request'

/**
 * 报销申请API
 */
export default {
  // 创建报销申请
  createApplication(data) {
    return request({
      url: '/api/expense/applications',
      method: 'post',
      data
    })
  },

  // 更新报销申请
  updateApplication(id, data) {
    return request({
      url: `/api/expense/applications/${id}`,
      method: 'put',
      data
    })
  },

  // 获取报销申请详情
  getApplicationDetail(id) {
    return request({
      url: `/api/expense/applications/${id}`,
      method: 'get'
    })
  },

  // 删除报销申请
  deleteApplication(id) {
    return request({
      url: `/api/expense/applications/${id}`,
      method: 'delete'
    })
  },

  // 提交报销申请
  submitApplication(id) {
    return request({
      url: `/api/expense/applications/${id}/submit`,
      method: 'post'
    })
  },

  // 撤回报销申请
  withdrawApplication(id) {
    return request({
      url: `/api/expense/applications/${id}/withdraw`,
      method: 'post'
    })
  },

  // 分页查询员工的报销申请
  getEmployeeApplications(params) {
    return request({
      url: '/api/expense/applications',
      method: 'get',
      params
    })
  },

  // 获取报销申请审批历史
  getApprovalHistory(id) {
    return request({
      url: `/api/expense/applications/${id}/approval-history`,
      method: 'get'
    })
  },

  // 获取员工报销统计
  getExpenseStatistics(params) {
    return request({
      url: '/api/expense/statistics',
      method: 'get',
      params
    })
  },

  // 导出员工报销数据
  exportApplications(params) {
    return request({
      url: '/api/expense/export',
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

  // 管理员API - 获取所有申请
  getAllApplications(params) {
    return request({
      url: '/api/admin/expense/applications',
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

  // 管理员API - 批量审批
  batchApproveApplications(data) {
    return request({
      url: '/api/admin/expense/applications/batch-approve',
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

  // 管理员API - 获取部门排行
  getDepartmentRanking(params) {
    return request({
      url: '/api/admin/expense/statistics/department-ranking',
      method: 'get',
      params
    })
  },

  // 管理员API - 获取员工排行
  getEmployeeRanking(params) {
    return request({
      url: '/api/admin/expense/statistics/employee-ranking',
      method: 'get',
      params
    })
  },

  // 管理员API - 导出数据
  exportAllApplications(params) {
    return request({
      url: '/api/admin/expense/export',
      method: 'get',
      params
    })
  },

  // 管理员API - 获取审批工作量
  getApprovalWorkload(params) {
    return request({
      url: '/api/admin/expense/statistics/approval-workload',
      method: 'get',
      params
    })
  },

  // 管理员API - 设置申请优先级
  setApplicationPriority(id, data) {
    return request({
      url: `/admin/expense/applications/${id}/priority`,
      method: 'put',
      data
    })
  }
}