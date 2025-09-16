import request from '@/utils/request'

/**
 * 文件上传API
 */
export default {
  // 上传单个文件
  uploadFile(file, onUploadProgress) {
    const formData = new FormData()
    formData.append('file', file)
    
    return request({
      url: '/files/upload',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress
    })
  },

  // 批量上传文件
  uploadFiles(files, onUploadProgress) {
    const formData = new FormData()
    files.forEach(file => {
      formData.append('files', file)
    })
    
    return request({
      url: '/files/upload/batch',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress
    })
  },

  // 上传报销凭证
  uploadExpenseVoucher(applicationId, file, onUploadProgress) {
    const formData = new FormData()
    formData.append('file', file)
    formData.append('applicationId', applicationId)
    
    return request({
      url: '/files/upload/expense-voucher',
      method: 'post',
      data: formData,
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress
    })
  },

  // 删除文件
  deleteFile(fileId) {
    return request({
      url: `/files/${fileId}`,
      method: 'delete'
    })
  },

  // 批量删除文件
  deleteFiles(fileIds) {
    return request({
      url: '/files/batch-delete',
      method: 'post',
      data: { fileIds }
    })
  },

  // 下载文件
  downloadFile(fileId) {
    return request({
      url: `/files/${fileId}/download`,
      method: 'get',
      responseType: 'blob'
    })
  },

  // 预览文件
  previewFile(fileId) {
    return request({
      url: `/files/${fileId}/preview`,
      method: 'get'
    })
  },

  // 获取文件信息
  getFileInfo(fileId) {
    return request({
      url: `/files/${fileId}/info`,
      method: 'get'
    })
  },

  // 验证文件类型
  validateFileType(file) {
    return request({
      url: '/files/validate/type',
      method: 'post',
      data: {
        fileName: file.name,
        fileSize: file.size,
        fileType: file.type
      }
    })
  },

  // 验证文件大小
  validateFileSize(file) {
    return request({
      url: '/files/validate/size',
      method: 'post',
      data: {
        fileName: file.name,
        fileSize: file.size
      }
    })
  },

  // 获取上传配置
  getUploadConfig() {
    return request({
      url: '/files/upload/config',
      method: 'get'
    })
  },

  // 获取文件列表
  getFileList(params) {
    return request({
      url: '/files',
      method: 'get',
      params
    })
  },

  // 获取报销申请的附件列表
  getExpenseAttachments(applicationId) {
    return request({
      url: `/files/expense/${applicationId}/attachments`,
      method: 'get'
    })
  },

  // 生成文件访问URL
  generateFileUrl(fileId) {
    return request({
      url: `/files/${fileId}/url`,
      method: 'get'
    })
  }
}