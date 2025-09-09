import request from '@/utils/request'

/**
 * 管理端文档管理API
 */
export default {
  // 获取所有文档列表
  getAllDocuments(params) {
    return request({
      url: '/admin/documents',
      method: 'get',
      params
    })
  },

  // 创建文档
  createDocument(data) {
    return request({
      url: '/admin/documents',
      method: 'post',
      data
    })
  },

  // 获取文档详情
  getDocumentDetail(id) {
    return request({
      url: `/admin/documents/${id}`,
      method: 'get'
    })
  },

  // 更新文档
  updateDocument(id, data) {
    return request({
      url: `/admin/documents/${id}`,
      method: 'put',
      data
    })
  },

  // 删除文档
  deleteDocument(id) {
    return request({
      url: `/admin/documents/${id}`,
      method: 'delete'
    })
  },

  // 批量删除文档
  batchDeleteDocuments(documentIds) {
    return request({
      url: '/admin/documents/batch/delete',
      method: 'delete',
      data: { documentIds }
    })
  },

  // 上传文档
  uploadDocument(formData) {
    return request({
      url: '/admin/documents/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },

  // 下载文档
  downloadDocument(id) {
    return request({
      url: `/admin/documents/${id}/download`,
      method: 'get',
      responseType: 'blob'
    })
  },

  // 搜索文档
  searchDocuments(keyword) {
    return request({
      url: '/admin/documents/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 获取文档统计
  getDocumentStatistics() {
    return request({
      url: '/admin/documents/statistics',
      method: 'get'
    })
  },

  // 按部门获取文档统计
  getDocumentStatsByDepartment() {
    return request({
      url: '/admin/documents/statistics/department',
      method: 'get'
    })
  },

  // 按用户获取文档统计
  getDocumentStatsByUser() {
    return request({
      url: '/admin/documents/statistics/user',
      method: 'get'
    })
  },

  // 获取文档分类
  getDocumentCategories() {
    return request({
      url: '/admin/documents/categories',
      method: 'get'
    })
  },

  // 创建文档分类
  createDocumentCategory(data) {
    return request({
      url: '/admin/documents/categories',
      method: 'post',
      data
    })
  },

  // 更新文档分类
  updateDocumentCategory(id, data) {
    return request({
      url: `/admin/documents/categories/${id}`,
      method: 'put',
      data
    })
  },

  // 删除文档分类
  deleteDocumentCategory(id) {
    return request({
      url: `/admin/documents/categories/${id}`,
      method: 'delete'
    })
  },

  // 设置文档权限
  setDocumentPermissions(id, permissions) {
    return request({
      url: `/admin/documents/${id}/permissions`,
      method: 'put',
      data: { permissions }
    })
  },

  // 获取文档权限
  getDocumentPermissions(id) {
    return request({
      url: `/admin/documents/${id}/permissions`,
      method: 'get'
    })
  },

  // 审核文档
  auditDocument(id, status, comment) {
    return request({
      url: `/admin/documents/${id}/audit`,
      method: 'put',
      data: { status, comment }
    })
  },

  // 获取待审核文档
  getPendingDocuments() {
    return request({
      url: '/admin/documents/pending',
      method: 'get'
    })
  },

  // 导出文档列表
  exportDocuments(params) {
    return request({
      url: '/admin/documents/export',
      method: 'get',
      params,
      responseType: 'blob'
    })
  }
}