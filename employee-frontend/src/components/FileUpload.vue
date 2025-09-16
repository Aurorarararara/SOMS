<template>
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :multiple="multiple"
      :accept="accept"
      :before-upload="beforeUpload"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :on-remove="handleRemove"
      :file-list="fileList"
      :auto-upload="autoUpload"
      :show-file-list="showFileList"
      :limit="limit"
      :on-exceed="handleExceed"
      drag
      class="upload-area"
    >
      <div class="upload-content">
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p class="upload-title">点击或拖拽文件到此区域上传</p>
          <p class="upload-hint">
            支持格式：{{ acceptText }}
            <br>
            单个文件大小不超过 {{ maxSize }}MB
            <span v-if="limit">，最多上传 {{ limit }} 个文件</span>
          </p>
        </div>
      </div>
    </el-upload>

    <!-- 文件列表 -->
    <div v-if="modelValue && modelValue.length > 0" class="file-list">
      <div class="list-header">
        <span>已上传文件 ({{ modelValue.length }})</span>
        <el-button v-if="multiple" size="small" @click="clearAll">清空全部</el-button>
      </div>
      <div class="file-items">
        <div
          v-for="(file, index) in modelValue"
          :key="file.id || index"
          class="file-item"
        >
          <div class="file-icon">
            <el-icon><Document /></el-icon>
          </div>
          <div class="file-info">
            <div class="file-name" :title="file.fileName || file.name">
              {{ file.fileName || file.name }}
            </div>
            <div class="file-meta">
              <span class="file-size">{{ formatFileSize(file.fileSize || file.size) }}</span>
              <span v-if="file.uploadTime" class="file-time">
                {{ formatTime(file.uploadTime) }}
              </span>
            </div>
          </div>
          <div class="file-actions">
            <el-button
              v-if="file.id"
              size="small"
              @click="previewFile(file)"
            >
              <el-icon><View /></el-icon>
            </el-button>
            <el-button
              v-if="file.id"
              size="small"
              @click="downloadFile(file)"
            >
              <el-icon><Download /></el-icon>
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="removeFile(index)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
          <div v-if="file.uploading" class="file-progress">
            <el-progress
              :percentage="file.progress || 0"
              :status="file.status"
              :stroke-width="4"
            />
          </div>
        </div>
      </div>
    </div>

    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <el-progress
        :percentage="uploadProgress"
        :status="uploadStatus"
        :stroke-width="6"
      >
        <template #default="{ percentage }">
          <span class="progress-text">{{ percentage }}%</span>
        </template>
      </el-progress>
      <p class="progress-info">正在上传文件，请稍候...</p>
    </div>

    <!-- 文件预览对话框 -->
    <FilePreviewDialog
      v-model="showPreviewDialog"
      :file-data="previewFileData"
    />
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  UploadFilled,
  Document,
  View,
  Download,
  Delete
} from '@element-plus/icons-vue'
import { getToken } from '@/utils/auth'
import fileApi from '@/api/file'
import FilePreviewDialog from './FilePreviewDialog.vue'

// Props
const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  multiple: {
    type: Boolean,
    default: true
  },
  accept: {
    type: String,
    default: '.jpg,.jpeg,.png,.pdf,.doc,.docx,.xls,.xlsx'
  },
  maxSize: {
    type: Number,
    default: 10 // MB
  },
  limit: {
    type: Number,
    default: 0 // 0表示无限制
  },
  autoUpload: {
    type: Boolean,
    default: true
  },
  showFileList: {
    type: Boolean,
    default: false
  },
  applicationId: {
    type: [String, Number],
    default: null
  }
})

// Emits
const emit = defineEmits([
  'update:modelValue',
  'upload-success',
  'upload-error',
  'upload-progress'
])

// 响应式数据
const uploadRef = ref()
const uploading = ref(false)
const uploadProgress = ref(0)
const uploadStatus = ref('')
const fileList = ref([])
const showPreviewDialog = ref(false)
const previewFileData = ref(null)

// 计算属性
const uploadUrl = computed(() => {
  return props.applicationId 
    ? '/api/files/upload/expense-voucher'
    : '/api/files/upload'
})

const uploadHeaders = computed(() => {
  return {
    'Authorization': `Bearer ${getToken()}`
  }
})

const acceptText = computed(() => {
  return props.accept.replace(/\./g, '').toUpperCase()
})

// 监听器
watch(() => props.modelValue, (newValue) => {
  // 同步文件列表
}, { deep: true })

// 方法
const beforeUpload = async (file) => {
  // 验证文件类型
  const isValidType = props.accept.split(',').some(type => {
    return file.name.toLowerCase().endsWith(type.toLowerCase())
  })
  
  if (!isValidType) {
    ElMessage.error(`文件格式不支持，请上传 ${acceptText.value} 格式的文件`)
    return false
  }
  
  // 验证文件大小
  const isValidSize = file.size / 1024 / 1024 < props.maxSize
  if (!isValidSize) {
    ElMessage.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }
  
  // 验证文件数量
  if (props.limit && props.modelValue.length >= props.limit) {
    ElMessage.error(`最多只能上传 ${props.limit} 个文件`)
    return false
  }
  
  try {
    // 调用后端验证API
    await fileApi.validateFileType(file)
    await fileApi.validateFileSize(file)
    return true
  } catch (error) {
    ElMessage.error('文件验证失败: ' + error.message)
    return false
  }
}

const handleProgress = (event, file) => {
  uploading.value = true
  uploadProgress.value = Math.round(event.percent)
  uploadStatus.value = ''
  
  // 更新文件进度
  const fileIndex = props.modelValue.findIndex(f => f.uid === file.uid)
  if (fileIndex > -1) {
    const updatedFiles = [...props.modelValue]
    updatedFiles[fileIndex] = {
      ...updatedFiles[fileIndex],
      uploading: true,
      progress: Math.round(event.percent)
    }
    emit('update:modelValue', updatedFiles)
  }
  
  emit('upload-progress', event, file)
}

const handleSuccess = (response, file) => {
  uploading.value = false
  uploadProgress.value = 100
  uploadStatus.value = 'success'
  
  if (response.code === 200) {
    const newFile = {
      id: response.data.id,
      fileName: response.data.fileName,
      fileSize: response.data.fileSize,
      filePath: response.data.filePath,
      uploadTime: response.data.uploadTime,
      uid: file.uid
    }
    
    const updatedFiles = [...props.modelValue]
    const existingIndex = updatedFiles.findIndex(f => f.uid === file.uid)
    
    if (existingIndex > -1) {
      updatedFiles[existingIndex] = newFile
    } else {
      updatedFiles.push(newFile)
    }
    
    emit('update:modelValue', updatedFiles)
    emit('upload-success', [newFile])
    ElMessage.success('文件上传成功')
  } else {
    handleError(new Error(response.message || '上传失败'), file)
  }
  
  // 重置进度
  setTimeout(() => {
    uploadProgress.value = 0
    uploadStatus.value = ''
  }, 2000)
}

const handleError = (error, file) => {
  uploading.value = false
  uploadStatus.value = 'exception'
  
  // 移除失败的文件
  const updatedFiles = props.modelValue.filter(f => f.uid !== file.uid)
  emit('update:modelValue', updatedFiles)
  
  emit('upload-error', error)
  ElMessage.error('文件上传失败: ' + error.message)
  
  // 重置进度
  setTimeout(() => {
    uploadProgress.value = 0
    uploadStatus.value = ''
  }, 2000)
}

const handleRemove = (file) => {
  const updatedFiles = props.modelValue.filter(f => f.uid !== file.uid)
  emit('update:modelValue', updatedFiles)
}

const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 个文件`)
}

const removeFile = async (index) => {
  try {
    const file = props.modelValue[index]
    
    if (file.id) {
      await ElMessageBox.confirm('确定要删除此文件吗？', '确认删除', {
        type: 'warning'
      })
      
      await fileApi.deleteFile(file.id)
    }
    
    const updatedFiles = [...props.modelValue]
    updatedFiles.splice(index, 1)
    emit('update:modelValue', updatedFiles)
    
    ElMessage.success('文件删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('文件删除失败')
    }
  }
}

const clearAll = async () => {
  try {
    await ElMessageBox.confirm('确定要清空所有文件吗？', '确认清空', {
      type: 'warning'
    })
    
    // 删除已上传的文件
    const uploadedFiles = props.modelValue.filter(f => f.id)
    if (uploadedFiles.length > 0) {
      const fileIds = uploadedFiles.map(f => f.id)
      await fileApi.deleteFiles(fileIds)
    }
    
    emit('update:modelValue', [])
    ElMessage.success('文件清空成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('文件清空失败')
    }
  }
}

const previewFile = async (file) => {
  try {
    const response = await fileApi.previewFile(file.id)
    previewFileData.value = {
      ...file,
      previewUrl: response.data.previewUrl
    }
    showPreviewDialog.value = true
  } catch (error) {
    ElMessage.error('文件预览失败')
  }
}

const downloadFile = async (file) => {
  try {
    const response = await fileApi.downloadFile(file.id)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.fileName
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('文件下载成功')
  } catch (error) {
    ElMessage.error('文件下载失败')
  }
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

// 暴露方法
defineExpose({
  clearFiles: clearAll,
  upload: () => uploadRef.value?.submit()
})
</script>

<style scoped>
.file-upload {
  width: 100%;
}

.upload-area {
  width: 100%;
}

.upload-area :deep(.el-upload) {
  width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
  width: 100%;
  height: 120px;
  border: 2px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.upload-area :deep(.el-upload-dragger:hover) {
  border-color: #409eff;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
}

.upload-icon {
  font-size: 48px;
  color: #c0c4cc;
  margin-bottom: 12px;
}

.upload-text {
  text-align: center;
}

.upload-title {
  font-size: 16px;
  color: #606266;
  margin: 0 0 8px 0;
}

.upload-hint {
  font-size: 12px;
  color: #909399;
  margin: 0;
  line-height: 1.4;
}

.file-list {
  margin-top: 20px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f5f7fa;
  border-bottom: 1px solid #ebeef5;
  font-weight: 600;
  color: #303133;
}

.file-items {
  max-height: 300px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.file-item:hover {
  background: #f8f9fa;
}

.file-item:last-child {
  border-bottom: none;
}

.file-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f9ff;
  border-radius: 4px;
  margin-right: 12px;
  color: #409eff;
  font-size: 16px;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 4px;
}

.file-meta {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}

.file-actions {
  display: flex;
  gap: 8px;
}

.file-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 0 16px 8px;
}

.upload-progress {
  margin-top: 16px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 4px;
}

.progress-text {
  font-size: 12px;
  color: #606266;
}

.progress-info {
  text-align: center;
  margin: 8px 0 0 0;
  font-size: 12px;
  color: #909399;
}
</style>