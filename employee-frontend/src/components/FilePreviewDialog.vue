<template>
  <el-dialog
    v-model="dialogVisible"
    title="文件预览"
    width="80%"
    :before-close="handleClose"
    class="file-preview-dialog"
  >
    <div v-if="fileData" class="preview-container">
      <!-- 文件信息 -->
      <div class="file-info">
        <div class="info-item">
          <span class="label">文件名：</span>
          <span class="value">{{ fileData.fileName }}</span>
        </div>
        <div class="info-item">
          <span class="label">文件大小：</span>
          <span class="value">{{ formatFileSize(fileData.fileSize) }}</span>
        </div>
        <div class="info-item">
          <span class="label">上传时间：</span>
          <span class="value">{{ formatTime(fileData.uploadTime) }}</span>
        </div>
      </div>

      <!-- 预览内容 -->
      <div class="preview-content">
        <!-- 图片预览 -->
        <div v-if="isImage" class="image-preview">
          <img
            :src="previewUrl"
            :alt="fileData.fileName"
            @load="handleImageLoad"
            @error="handleImageError"
          />
        </div>

        <!-- PDF预览 -->
        <div v-else-if="isPdf" class="pdf-preview">
          <iframe
            :src="previewUrl"
            frameborder="0"
            width="100%"
            height="600px"
          ></iframe>
        </div>

        <!-- 文档预览 -->
        <div v-else-if="isDocument" class="document-preview">
          <div class="document-placeholder">
            <el-icon class="document-icon"><Document /></el-icon>
            <p class="document-text">{{ fileData.fileName }}</p>
            <p class="document-hint">此文件类型不支持在线预览，请下载后查看</p>
            <el-button type="primary" @click="downloadFile">
              <el-icon><Download /></el-icon>
              下载文件
            </el-button>
          </div>
        </div>

        <!-- 其他文件类型 -->
        <div v-else class="unsupported-preview">
          <div class="unsupported-placeholder">
            <el-icon class="file-icon"><Files /></el-icon>
            <p class="file-text">{{ fileData.fileName }}</p>
            <p class="file-hint">不支持预览此文件类型</p>
            <el-button type="primary" @click="downloadFile">
              <el-icon><Download /></el-icon>
              下载文件
            </el-button>
          </div>
        </div>

        <!-- 加载状态 -->
        <div v-if="loading" class="loading-container">
          <el-loading
            element-loading-text="正在加载预览..."
            element-loading-spinner="el-icon-loading"
            element-loading-background="rgba(0, 0, 0, 0.8)"
          />
        </div>

        <!-- 错误状态 -->
        <div v-if="error" class="error-container">
          <el-icon class="error-icon"><Warning /></el-icon>
          <p class="error-text">{{ error }}</p>
          <el-button @click="retryPreview">重试</el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button type="primary" @click="downloadFile">
          <el-icon><Download /></el-icon>
          下载
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Document,
  Download,
  Files,
  Warning
} from '@element-plus/icons-vue'
import fileApi from '@/api/file'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  fileData: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue'])

// 响应式数据
const loading = ref(false)
const error = ref('')
const previewUrl = ref('')

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const fileExtension = computed(() => {
  if (!props.fileData?.fileName) return ''
  return props.fileData.fileName.split('.').pop().toLowerCase()
})

const isImage = computed(() => {
  const imageExts = ['jpg', 'jpeg', 'png', 'gif', 'bmp', 'webp']
  return imageExts.includes(fileExtension.value)
})

const isPdf = computed(() => {
  return fileExtension.value === 'pdf'
})

const isDocument = computed(() => {
  const docExts = ['doc', 'docx', 'xls', 'xlsx', 'ppt', 'pptx', 'txt']
  return docExts.includes(fileExtension.value)
})

// 监听器
watch(() => props.fileData, (newData) => {
  if (newData && props.modelValue) {
    loadPreview()
  }
}, { immediate: true })

watch(() => props.modelValue, (visible) => {
  if (visible && props.fileData) {
    loadPreview()
  } else {
    resetPreview()
  }
})

// 方法
const loadPreview = async () => {
  if (!props.fileData?.id) return
  
  loading.value = true
  error.value = ''
  
  try {
    if (props.fileData.previewUrl) {
      // 如果已有预览URL，直接使用
      previewUrl.value = props.fileData.previewUrl
    } else {
      // 否则请求预览URL
      const response = await fileApi.previewFile(props.fileData.id)
      previewUrl.value = response.data.previewUrl
    }
  } catch (err) {
    error.value = '预览加载失败: ' + (err.message || '未知错误')
    console.error('Preview load error:', err)
  } finally {
    loading.value = false
  }
}

const resetPreview = () => {
  previewUrl.value = ''
  loading.value = false
  error.value = ''
}

const retryPreview = () => {
  loadPreview()
}

const handleImageLoad = () => {
  loading.value = false
}

const handleImageError = () => {
  error.value = '图片加载失败'
  loading.value = false
}

const downloadFile = async () => {
  if (!props.fileData?.id) {
    ElMessage.error('文件信息不完整')
    return
  }
  
  try {
    const response = await fileApi.downloadFile(props.fileData.id)
    
    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = props.fileData.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('文件下载成功')
  } catch (error) {
    ElMessage.error('文件下载失败: ' + error.message)
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}

const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.file-preview-dialog {
  --el-dialog-margin-top: 5vh;
}

.file-preview-dialog :deep(.el-dialog) {
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.file-preview-dialog :deep(.el-dialog__body) {
  flex: 1;
  padding: 20px;
  overflow: hidden;
}

.preview-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.file-info {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 6px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}

.value {
  color: #303133;
}

.preview-content {
  flex: 1;
  position: relative;
  border: 1px solid #ebeef5;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.image-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 4px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pdf-preview {
  width: 100%;
  height: 600px;
}

.document-preview,
.unsupported-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.document-placeholder,
.unsupported-placeholder {
  text-align: center;
  padding: 40px;
}

.document-icon,
.file-icon {
  font-size: 64px;
  color: #c0c4cc;
  margin-bottom: 16px;
}

.document-text,
.file-text {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  word-break: break-all;
}

.document-hint,
.file-hint {
  font-size: 14px;
  color: #909399;
  margin: 0 0 20px 0;
}

.loading-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.9);
}

.error-container {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fff;
}

.error-icon {
  font-size: 48px;
  color: #f56c6c;
  margin-bottom: 16px;
}

.error-text {
  font-size: 14px;
  color: #606266;
  margin: 0 0 20px 0;
  text-align: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .file-preview-dialog {
    --el-dialog-margin-top: 2vh;
  }
  
  .file-preview-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2vh auto;
  }
  
  .file-info {
    flex-direction: column;
    gap: 12px;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
}
</style>