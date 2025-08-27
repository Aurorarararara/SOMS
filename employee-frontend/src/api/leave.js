import request from '@/utils/request'

export const leaveApi = {
  // 提交请假申请
  submitLeave(data) {
    return request.post('/leave/apply', data)
  },

  // 修改请假申请
  updateLeave(id, data) {
    return request.put(`/leave/${id}`, data)
  },

  // 撤销请假申请
  cancelLeave(id) {
    return request.delete(`/leave/${id}`)
  },

  // 获取请假记录列表
  getLeaveRecords(params) {
    return request.get('/leave/applications', { params })
  },

  // 获取请假详情
  getLeaveDetail(id) {
    return request.get(`/leave/${id}`)
  },

  // 获取年度请假统计
  getYearlyStatistics(year) {
    return request.get(`/leave/statistics/yearly?year=${year}`)
  },
}