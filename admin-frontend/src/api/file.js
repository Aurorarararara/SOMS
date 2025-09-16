import request from '@/utils/request'

// 文件上传
export const uploadFile = (file, onUploadProgress) => {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/files/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

// 批量文件上传
export const uploadFiles = (files, onUploadProgress) => {
  const formData = new FormData()
  files.forEach(file => {
    formData.append('files', file)
  })
  
  return request({
    url: '/api/files/upload/batch',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    onUploadProgress
  })
}

// 文件下载
export const downloadFile = (fileId, fileName) => {
  return request({
    url: `/api/files/download/${fileId}`,
    method: 'get',
    responseType: 'blob',
    params: { fileName }
  })
}

// 获取文件信息
export const getFileInfo = (fileId) => {
  return request({
    url: `/api/files/${fileId}`,
    method: 'get'
  })
}

// 删除文件
export const deleteFile = (fileId) => {
  return request({
    url: `/api/files/${fileId}`,
    method: 'delete'
  })
}

// 获取文件列表
export const getFileList = (params) => {
  return request({
    url: '/api/files',
    method: 'get',
    params
  })
}

// 文件预览
export const previewFile = (fileId) => {
  return request({
    url: `/api/files/preview/${fileId}`,
    method: 'get',
    responseType: 'blob'
  })
}

// 获取文件预览URL
export const getPreviewUrl = (fileId) => {
  return `/api/files/preview/${fileId}`
}

// 获取文件下载URL
export const getDownloadUrl = (fileId, fileName) => {
  return `/api/files/download/${fileId}?fileName=${encodeURIComponent(fileName)}`
}

export default {
  uploadFile,
  uploadFiles,
  downloadFile,
  getFileInfo,
  deleteFile,
  getFileList,
  previewFile,
  getPreviewUrl,
  getDownloadUrl
}