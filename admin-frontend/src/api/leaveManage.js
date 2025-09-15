import request from '@/utils/request'

export const leaveManageApi = {
  // 分页查询所有请假申请（管理员）
  getLeaveApplications(params) {
    return request({
      url: '/admin/leave/applications',
      method: 'get',
      params
    })
  },

  // 获取请假申请详情
  getLeaveApplicationDetail(applicationId) {
    return request({
      url: `/admin/leave/applications/${applicationId}`,
      method: 'get'
    })
  },

  // 审批请假申请
  approveLeaveApplication(applicationId, data) {
    return request({
      url: `/admin/leave/applications/${applicationId}/approve`,
      method: 'post',
      data
    })
  },

  // 拒绝请假申请
  rejectLeaveApplication(applicationId, data) {
    return request({
      url: `/admin/leave/applications/${applicationId}/reject`,
      method: 'post',
      data
    })
  },

  // 获取请假审批统计
  getLeaveStatistics() {
    return request({
      url: '/admin/leave/statistics',
      method: 'get'
    })
  },

  // 获取请假统计报表数据
  getLeaveStatisticsReport(params) {
    return request({
      url: '/api/admin/leave/applications/statistics/report',
      method: 'get',
      params
    })
  },

  // 批量审批
  batchApprove(applicationIds, data) {
    return request({
      url: '/admin/leave/applications/batch-approve',
      method: 'post',
      data: {
        applicationIds,
        ...data
      }
    })
  },

  // 批量拒绝
  batchReject(applicationIds, data) {
    return request({
      url: '/admin/leave/applications/batch-reject',
      method: 'post',
      data: {
        applicationIds,
        ...data
      }
    })
  },

  // 导出请假申请数据
  exportLeaveApplications(params) {
    return request({
      url: '/admin/leave/applications/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}