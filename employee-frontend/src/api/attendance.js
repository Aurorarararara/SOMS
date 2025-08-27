import request from '@/utils/request'

export const attendanceApi = {
  // 获取今日考勤记录
  getTodayRecord(date) {
    return request.get(`/attendance/today?date=${date}`)
  },

  // 上班打卡
  checkIn() {
    return request.post('/attendance/check-in')
  },

  // 下班打卡
  checkOut() {
    return request.post('/attendance/check-out')
  },

  // 手动打卡
  manualCheckIn(data) {
    return request.post('/attendance/manual', data)
  },

  // 获取考勤记录列表
  getRecords(params) {
    return request.get('/attendance/records', { params })
  },

  // 获取月度统计
  getMonthlyStats(year, month) {
    return request.get(`/attendance/stats/monthly?year=${year}&month=${month}`)
  },

  // 导出考勤记录
  exportRecords(month) {
    return request.get(`/attendance/export?month=${month}`, { responseType: 'blob' })
  }
}