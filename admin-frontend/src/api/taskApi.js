import request from '@/utils/request'

/**
 * 管理端任务管理API
 */

// ==================== 基础任务操作 ====================

/**
 * 创建任务
 */
export function createTask(data) {
  return request({
    url: '/admin/tasks',
    method: 'post',
    data
  })
}

/**
 * 更新任务
 */
export function updateTask(id, data) {
  return request({
    url: `/admin/tasks/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除任务
 */
export function deleteTask(id) {
  return request({
    url: `/admin/tasks/${id}`,
    method: 'delete'
  })
}

/**
 * 获取任务详情
 */
export function getTaskDetail(id) {
  return request({
    url: `/admin/tasks/${id}`,
    method: 'get'
  })
}

/**
 * 分页查询任务
 */
export function getTaskList(params) {
  return request({
    url: '/admin/tasks/page',
    method: 'post',
    data: params
  })
}

/**
 * 获取待处理任务
 */
export function getPendingTasks(limit = 5) {
  return request({
    url: '/admin/tasks/pending',
    method: 'get',
    params: { limit }
  })
}

/**
 * 分配任务
 */
export function assignTask(id, assigneeId) {
  return request({
    url: `/admin/tasks/${id}/assign`,
    method: 'post',
    params: { assigneeId }
  })
}

/**
 * 更新任务进度
 */
export function updateTaskProgress(id, progress) {
  return request({
    url: `/admin/tasks/${id}/progress`,
    method: 'put',
    params: { progress }
  })
}

/**
 * 完成任务
 */
export function completeTask(id) {
  return request({
    url: `/admin/tasks/${id}/complete`,
    method: 'put'
  })
}

/**
 * 复制任务
 */
export function duplicateTask(id) {
  return request({
    url: `/admin/tasks/${id}/duplicate`,
    method: 'post'
  })
}

// ==================== 任务查询 ====================

/**
 * 获取分配的任务
 */
export function getAssignedTasks(userId) {
  return request({
    url: '/admin/tasks/assigned',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取创建的任务
 */
export function getCreatedTasks(userId) {
  return request({
    url: '/admin/tasks/created',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取即将到期的任务
 */
export function getUpcomingTasks(days = 7) {
  return request({
    url: '/admin/tasks/upcoming',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取逾期任务
 */
export function getOverdueTasks() {
  return request({
    url: '/admin/tasks/overdue',
    method: 'get'
  })
}

/**
 * 获取任务统计
 */
export function getTaskStats(userId) {
  return request({
    url: '/admin/tasks/stats',
    method: 'get',
    params: { userId }
  })
}

// ==================== 批量操作 ====================

/**
 * 批量分配任务
 */
export function batchAssignTasks(taskIds, assigneeId) {
  return request({
    url: '/admin/tasks/batch/assign',
    method: 'post',
    data: { taskIds, assigneeId }
  })
}

/**
 * 批量更新任务状态
 */
export function batchUpdateTaskStatus(taskIds, status) {
  return request({
    url: '/admin/tasks/batch/status',
    method: 'post',
    data: { taskIds, status }
  })
}

// ==================== 管理端专用统计 ====================

/**
 * 获取全局任务统计
 */
export function getGlobalTaskStats() {
  return request({
    url: '/admin/tasks/stats/global',
    method: 'get'
  })
}

/**
 * 获取部门任务分布统计
 */
export function getDepartmentTaskDistribution() {
  return request({
    url: '/admin/tasks/stats/department-distribution',
    method: 'get'
  })
}

/**
 * 获取用户工作量排行
 */
export function getUserWorkloadRanking(limit = 10) {
  return request({
    url: '/admin/tasks/stats/user-ranking',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取按部门统计的任务
 */
export function getTaskStatsByDepartment(departmentId) {
  return request({
    url: '/admin/tasks/stats/department',
    method: 'get',
    params: { departmentId }
  })
}

/**
 * 获取按状态统计的任务
 */
export function getTaskStatsByStatus() {
  return request({
    url: '/admin/tasks/stats/status',
    method: 'get'
  })
}

/**
 * 获取工作量统计
 */
export function getWorkloadStats(userId) {
  return request({
    url: '/admin/tasks/stats/workload',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取任务完成率统计
 */
export function getCompletionRateStats(userId, days = 30) {
  return request({
    url: '/admin/tasks/stats/completion-rate',
    method: 'get',
    params: { userId, days }
  })
}

/**
 * 获取任务趋势统计
 */
export function getTaskTrendStats(days = 30) {
  return request({
    url: '/admin/tasks/stats/trend',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取优先级分布统计
 */
export function getPriorityDistributionStats(userId) {
  return request({
    url: '/admin/tasks/stats/priority',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取任务效率统计
 */
export function getTaskEfficiencyStats(userId) {
  return request({
    url: '/admin/tasks/stats/efficiency',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取逾期任务统计
 */
export function getOverdueTaskStats(userId) {
  return request({
    url: '/admin/tasks/stats/overdue',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取任务分配统计
 */
export function getTaskAssignmentStats() {
  return request({
    url: '/admin/tasks/stats/assignment',
    method: 'get'
  })
}

// ==================== 任务评论 ====================

/**
 * 获取任务评论
 */
export function getTaskComments(taskId) {
  return request({
    url: `/admin/tasks/${taskId}/comments`,
    method: 'get'
  })
}

/**
 * 添加任务评论
 */
export function addTaskComment(taskId, content) {
  return request({
    url: `/admin/tasks/${taskId}/comments`,
    method: 'post',
    data: { content }
  })
}

/**
 * 回复评论
 */
export function replyComment(commentId, content) {
  return request({
    url: `/admin/tasks/comments/${commentId}/reply`,
    method: 'post',
    params: { content }
  })
}

/**
 * 更新评论
 */
export function updateComment(commentId, content) {
  return request({
    url: `/admin/tasks/comments/${commentId}`,
    method: 'put',
    params: { content }
  })
}

/**
 * 删除评论
 */
export function deleteComment(commentId) {
  return request({
    url: `/admin/tasks/comments/${commentId}`,
    method: 'delete'
  })
}

// ==================== 导出功能 ====================

/**
 * 导出任务数据
 */
export function exportTasks(params, format = 'excel') {
  return request({
    url: '/admin/tasks/export',
    method: 'post',
    data: params,
    params: { format },
    responseType: 'blob'
  })
}

// ==================== 高级统计分析 ====================

/**
 * 获取效率分析统计
 */
export function getEfficiencyAnalysis(days = 30) {
  return request({
    url: '/admin/tasks/efficiency-analysis',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取月度任务趋势统计
 */
export function getMonthlyTaskTrendStats(months = 6) {
  return request({
    url: '/admin/tasks/monthly-trend-stats',
    method: 'get',
    params: { months }
  })
}

/**
 * 获取工作量趋势统计
 */
export function getWorkloadTrendStats(days = 30) {
  return request({
    url: '/admin/tasks/workload-trend-stats',
    method: 'get',
    params: { days }
  })
}

/**
 * 获取详细任务趋势分析
 */
export function getDetailedTaskTrendStats(days = 30) {
  return request({
    url: '/admin/tasks/detailed-trend-stats',
    method: 'get',
    params: { days }
  })
}

/**
 * 按周统计任务趋势
 */
export function getWeeklyTaskTrendStats(weeks = 12) {
  return request({
    url: '/admin/tasks/weekly-trend-stats',
    method: 'get',
    params: { weeks }
  })
}

/**
 * 按部门统计任务趋势
 */
export function getDepartmentTaskTrendStats(days = 30) {
  return request({
    url: '/admin/tasks/department-trend-stats',
    method: 'get',
    params: { days }
  })
}
