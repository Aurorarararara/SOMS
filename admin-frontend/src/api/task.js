import request from '@/utils/request'

/**
 * 管理端任务管理API
 */
export default {
  // 获取所有任务列表
  getAllTasks(params) {
    return request({
      url: '/admin/tasks',
      method: 'get',
      params
    })
  },

  // 创建任务
  createTask(data) {
    return request({
      url: '/admin/tasks',
      method: 'post',
      data
    })
  },

  // 获取任务详情
  getTaskDetail(id) {
    return request({
      url: `/admin/tasks/${id}`,
      method: 'get'
    })
  },

  // 更新任务
  updateTask(id, data) {
    return request({
      url: `/admin/tasks/${id}`,
      method: 'put',
      data
    })
  },

  // 删除任务
  deleteTask(id) {
    return request({
      url: `/admin/tasks/${id}`,
      method: 'delete'
    })
  },

  // 批量分配任务
  batchAssignTasks(taskIds, assigneeId) {
    return request({
      url: '/admin/tasks/batch/assign',
      method: 'post',
      data: { taskIds, assigneeId }
    })
  },

  // 批量更新任务状态
  batchUpdateStatus(taskIds, status) {
    return request({
      url: '/admin/tasks/batch/status',
      method: 'post',
      data: { taskIds, status }
    })
  },

  // 获取任务统计
  getTaskStatistics() {
    return request({
      url: '/admin/tasks/statistics',
      method: 'get'
    })
  },

  // 按部门获取任务统计
  getTaskStatsByDepartment() {
    return request({
      url: '/admin/tasks/statistics/department',
      method: 'get'
    })
  },

  // 按用户获取任务统计
  getTaskStatsByUser() {
    return request({
      url: '/admin/tasks/statistics/user',
      method: 'get'
    })
  },

  // 获取逾期任务
  getOverdueTasks() {
    return request({
      url: '/admin/tasks/overdue',
      method: 'get'
    })
  },

  // 获取高优先级任务
  getHighPriorityTasks() {
    return request({
      url: '/admin/tasks/high-priority',
      method: 'get'
    })
  },

  // 导出任务数据
  exportTasks(params) {
    return request({
      url: '/admin/tasks/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  },

  // 搜索任务
  searchTasks(keyword) {
    return request({
      url: '/admin/tasks/search',
      method: 'get',
      params: { keyword }
    })
  }
}