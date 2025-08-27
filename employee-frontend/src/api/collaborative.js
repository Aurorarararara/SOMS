import request from '@/utils/request'

/**
 * 协同编辑相关API
 */
const collaborativeApi = {
  // 文档管理
  createDocument(data) {
    return request({
      url: '/collaborative/documents/create',
      method: 'post',
      data
    })
  },

  getDocument(documentId, userId) {
    return request({
      url: `/collaborative/documents/${documentId}`,
      method: 'get',
      params: { userId }
    })
  },

  getUserDocuments(userId) {
    return request({
      url: `/collaborative/documents/user/${userId}`,
      method: 'get'
    })
  },

  pageDocuments(params) {
    return request({
      url: '/collaborative/documents/page',
      method: 'get',
      params
    })
  },

  getDocumentsByType(documentType) {
    return request({
      url: `/collaborative/documents/type/${documentType}`,
      method: 'get'
    })
  },

  deleteDocument(documentId, userId) {
    return request({
      url: `/collaborative/documents/${documentId}`,
      method: 'delete',
      params: { userId }
    })
  },

  copyDocument(documentId, data) {
    return request({
      url: `/collaborative/documents/${documentId}/copy`,
      method: 'post',
      data
    })
  },

  // 会话管理
  joinSession(documentId, data) {
    return request({
      url: `/collaborative/documents/${documentId}/join`,
      method: 'post',
      data
    })
  },

  leaveSession(data) {
    return request({
      url: '/collaborative/documents/leave',
      method: 'post',
      data
    })
  },

  getActiveSessions(documentId) {
    return request({
      url: `/collaborative/documents/${documentId}/sessions`,
      method: 'get'
    })
  },

  // 内容同步
  syncDocument(documentId, data) {
    return request({
      url: `/collaborative/documents/${documentId}/sync`,
      method: 'post',
      data
    })
  },

  getDocumentVersion(documentId) {
    return request({
      url: `/collaborative/documents/${documentId}/version`,
      method: 'get'
    })
  },

  getDocumentOperations(documentId, fromSequence) {
    return request({
      url: `/collaborative/documents/${documentId}/operations`,
      method: 'get',
      params: { fromSequence }
    })
  },

  // 权限管理
  setDocumentPermission(documentId, data) {
    return request({
      url: `/collaborative/documents/${documentId}/permissions`,
      method: 'post',
      data
    })
  }
}

export default collaborativeApi