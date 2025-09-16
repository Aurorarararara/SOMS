import request from '@/utils/request'

/**
 * 管理端公告管理API
 */
export default {
  // 获取公告列表
  getAnnouncementList(params) {
    return request({
      url: '/api/admin/announcements',
      method: 'get',
      params
    })
  },

  // 创建公告
  createAnnouncement(data) {
    return request({
      url: '/api/admin/announcements',
      method: 'post',
      data
    })
  },

  // 获取公告详情
  getAnnouncementDetail(id) {
    return request({
      url: `/api/admin/announcements/${id}`,
      method: 'get'
    })
  },

  // 更新公告
  updateAnnouncement(id, data) {
    return request({
      url: `/api/admin/announcements/${id}`,
      method: 'put',
      data
    })
  },

  // 删除公告
  deleteAnnouncement(id) {
    return request({
      url: `/api/admin/announcements/${id}`,
      method: 'delete'
    })
  },

  // 批量删除公告
  batchDeleteAnnouncements(announcementIds) {
    return request({
      url: '/api/admin/announcements/batch/delete',
      method: 'delete',
      data: { announcementIds }
    })
  },

  // 发布公告
  publishAnnouncement(id) {
    return request({
      url: `/api/admin/announcements/${id}/publish`,
      method: 'put'
    })
  },

  // 撤回公告
  withdrawAnnouncement(id) {
    return request({
      url: `/api/admin/announcements/${id}/withdraw`,
      method: 'put'
    })
  },

  // 置顶公告
  pinAnnouncement(id) {
    return request({
      url: `/api/admin/announcements/${id}/pin`,
      method: 'put'
    })
  },

  // 取消置顶
  unpinAnnouncement(id) {
    return request({
      url: `/api/admin/announcements/${id}/unpin`,
      method: 'put'
    })
  },

  // 设置公告可见范围
  setAnnouncementVisibility(id, visibility) {
    return request({
      url: `/api/admin/announcements/${id}/visibility`,
      method: 'put',
      data: { visibility }
    })
  },

  // 获取公告统计
  getAnnouncementStatistics() {
    return request({
      url: '/api/admin/announcements/statistics',
      method: 'get'
    })
  },

  // 获取公告阅读统计
  getAnnouncementReadStats(id) {
    return request({
      url: `/api/admin/announcements/${id}/read-stats`,
      method: 'get'
    })
  },

  // 搜索公告
  searchAnnouncements(keyword) {
    return request({
      url: '/api/admin/announcements/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 获取公告分类
  getAnnouncementCategories() {
    return request({
      url: '/api/admin/announcements/categories',
      method: 'get'
    })
  },

  // 创建公告分类
  createAnnouncementCategory(data) {
    return request({
      url: '/api/admin/announcements/categories',
      method: 'post',
      data
    })
  },

  // 更新公告分类
  updateAnnouncementCategory(id, data) {
    return request({
      url: `/api/admin/announcements/categories/${id}`,
      method: 'put',
      data
    })
  },

  // 删除公告分类
  deleteAnnouncementCategory(id) {
    return request({
      url: `/api/admin/announcements/categories/${id}`,
      method: 'delete'
    })
  },

  // 导出公告数据
  exportAnnouncements(params) {
    return request({
      url: '/api/admin/announcements/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}