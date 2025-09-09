import request from '@/utils/request'

/**
 * 文档管理API
 */
export default {
  // 获取文档列表
  getDocumentList(params) {
    return request({
      url: '/api/documents',
      method: 'get',
      params
    })
  },

  // 创建文档
  createDocument(data) {
    return request({
      url: '/api/documents',
      method: 'post',
      data
    })
  },

  // 获取文档详情
  getDocumentDetail(id) {
    return request({
      url: `/api/documents/${id}`,
      method: 'get'
    })
  },

  // 更新文档
  updateDocument(id, data) {
    return request({
      url: `/api/documents/${id}`,
      method: 'put',
      data
    })
  },

  // 删除文档
  deleteDocument(id) {
    return request({
      url: `/api/documents/${id}`,
      method: 'delete'
    })
  },

  // 上传文档
  uploadDocument(formData) {
    return request({
      url: '/api/documents/upload',
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
      url: `/api/documents/${id}/download`,
      method: 'get',
      responseType: 'blob'
    })
  },

  // 搜索文档
  searchDocuments(keyword) {
    return request({
      url: '/api/documents/search',
      method: 'get',
      params: { keyword }
    })
  },

  // 获取文档分类
  getDocumentCategories() {
    return request({
      url: '/api/documents/categories',
      method: 'get'
    })
  },

  // 分享文档
  shareDocument(id, data) {
    return request({
      url: `/api/documents/${id}/share`,
      method: 'post',
      data
    })
  },

  // 收藏文档
  favoriteDocument(id) {
    return request({
      url: `/api/documents/${id}/favorite`,
      method: 'post'
    })
  },

  // 取消收藏
  unfavoriteDocument(id) {
    return request({
      url: `/api/documents/${id}/unfavorite`,
      method: 'delete'
    })
  },

  // 获取收藏的文档
  getFavoriteDocuments() {
    return request({
      url: '/api/documents/favorites',
      method: 'get'
    })
  },

  // 获取最近访问的文档
  getRecentDocuments() {
    return request({
      url: '/api/documents/recent',
      method: 'get'
    })
  }
}