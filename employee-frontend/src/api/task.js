import request from '@/utils/request'

/**
 * 任务管理API
 */
export default {
  // 创建任务
  createTask(data) {
    return request({
      url: '/tasks',
      method: 'post',
      data
    })
  },

  // 更新任务
  updateTask(id, data) {
    return request({
      url: `/tasks/${id}`,
      method: 'put',
      data
    })
  },

  // 获取任务详情
  getTaskDetail(id) {
    return request({
      url: `/api/tasks/${id}`,
      method: 'get'
    })
  },

  // 删除任务
  deleteTask(id) {
    return request({
      url: `/api/tasks/${id}`,
      method: 'delete'
    })
  },

  // 分页查询任务
  getTaskPage(data) {
    return request({
      url: '/tasks/page',
      method: 'post',
      data
    })
  },

  // 分配任务
  assignTask(id, assigneeId) {
    return request({
      url: `/tasks/${id}/assign`,
      method: 'post',
      params: { assigneeId }
    })
  },

  // 更新任务进度
  updateProgress(id, progress) {
    return request({
      url: `/tasks/${id}/progress`,
      method: 'post',
      params: { progress }
    })
  },

  // 完成任务
  completeTask(id) {
    return request({
      url: `/tasks/${id}/complete`,
      method: 'post'
    })
  },

  // 复制任务
  duplicateTask(id) {
    return request({
      url: `/tasks/${id}/duplicate`,
      method: 'post'
    })
  },

  // 获取任务统计
  getTaskStats() {
    return request({
      url: '/tasks/stats',
      method: 'get'
    })
  },

  // 获取分配给我的任务
  getAssignedTasks() {
    return request({
      url: '/tasks/assigned',
      method: 'get'
    })
  },

  // 获取我创建的任务
  getCreatedTasks() {
    return request({
      url: '/tasks/created',
      method: 'get'
    })
  },

  // 获取即将到期的任务
  getUpcomingTasks(days = 7) {
    return request({
      url: '/tasks/upcoming',
      method: 'get',
      params: { days }
    })
  },

  // 获取逾期任务
  getOverdueTasks() {
    return request({
      url: '/tasks/overdue',
      method: 'get'
    })
  },

  // 批量分配任务
  batchAssignTasks(taskIds, assigneeId) {
    return request({
      url: '/tasks/batch/assign',
      method: 'post',
      params: { taskIds, assigneeId }
    })
  },

  // 批量更新任务状态
  batchUpdateStatus(taskIds, status) {
    return request({
      url: '/tasks/batch/status',
      method: 'post',
      params: { taskIds, status }
    })
  },

  // 添加任务评论
  addComment(id, data) {
    return request({
      url: `/tasks/${id}/comments`,
      method: 'post',
      data
    })
  },

  // 获取任务评论
  getTaskComments(id, current = 1, size = 20) {
    return request({
      url: `/tasks/${id}/comments`,
      method: 'get',
      params: { current, size }
    })
  },

  // 回复评论
  replyComment(commentId, content) {
    return request({
      url: `/tasks/comments/${commentId}/reply`,
      method: 'post',
      params: { content }
    })
  },

  // 更新评论
  updateComment(commentId, content) {
    return request({
      url: `/tasks/comments/${commentId}`,
      method: 'put',
      params: { content }
    })
  },

  // 删除评论
  deleteComment(commentId) {
    return request({
      url: `/tasks/comments/${commentId}`,
      method: 'delete'
    })
  },

  // 获取评论的回复
  getCommentReplies(commentId) {
    return request({
      url: `/tasks/comments/${commentId}/replies`,
      method: 'get'
    })
  },

  // 获取我被@的评论
  getMentionedComments() {
    return request({
      url: '/tasks/comments/mentions',
      method: 'get'
    })
  }
}