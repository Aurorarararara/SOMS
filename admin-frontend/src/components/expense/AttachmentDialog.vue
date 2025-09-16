<template>
  <el-dialog
    v-model="dialogVisible"
    title="附件预览"
    width="70%"
    :before-close="handleClose"
    destroy-on-close
  >
    <div v-loading="loading" class="attachment-container">
      <div v-if="attachments && attachments.length > 0" class="attachment-list">
        <div 
          v-for="(attachment, index) in attachments" 
          :key="index" 
          class="attachment-item"
          :class="{ active: currentIndex === index }"
          @click="selectAttachment(index)"
        >
          <div class="attachment-info">
            <el-icon class="file-icon">
              <Document v-if="!isImage(attachment.fileName)" />
              <Picture v-else />
            </el-icon>
            <div class="file-details">
              <div class="file-name">{{ attachment.fileName }}</div>
              <div class="file-size">{{ formatFileSize(attachment.fileSize) }}</div>
            </div>
          </div>
          <div class="attachment-actions">
            <el-button 
              type="primary" 
              size="small" 
              @click.stop="downloadAttachment(attachment)"
            >
              <el-icon><Download /></el-icon>
              下载
            </el-button>
          </div>
        </div>
      </div>
      
      <div v-if="currentAttachment" class="preview-container">
        <div class="preview-header">
          <h4>{{ currentAttachment.fileName }}</h4>
          <div class="preview-actions">
            <el-button-group>
              <el-button 
                :disabled="currentIndex === 0"
                @click="previousAttachment"
              >
                <el-icon><ArrowLeft /></el-icon>
                上一个
              </el-button>
              <el-button 
                :disabled="currentIndex === attachments.length - 1"
                @click="nextAttachment"
              >
                下一个
                <el-icon><ArrowRight /></el-icon>
              </el-button>
            </el-button-group>
          </div>
        </div>
        
        <div class="preview-content">
          <!-- 图片预览 -->
          <div v-if="isImage(currentAttachment.fileName)" class="image-preview">
            <img 
              :src="getPreviewUrl(currentAttachment)"
              :alt="currentAttachment.fileName"
              @load="handleImageLoad"
              @error="handleImageError"
            />
          </div>
          
          <!-- PDF预览 -->
          <div v-else-if="isPdf(currentAttachment.fileName)" class="pdf-preview">
            <iframe 
              :src="getPreviewUrl(currentAttachment)"
              width="100%"
              height="600px"
              frameborder="0"
            ></iframe>
          </div>
          
          <!-- 其他文件类型 -->
          <div v-else class="file-preview">
            <div class="file-icon-large">
              <el-icon size="80"><Document /></el-icon>
            </div>
            <div class="file-info">
              <h3>{{ currentAttachment.fileName }}</h3>
              <p>文件大小: {{ formatFileSize(currentAttachment.fileSize) }}</p>
              <p>此文件类型不支持预览，请下载后查看</p>
              <el-button 
                type="primary" 
                size="large"
                @click="downloadAttachment(currentAttachment)"
              >
                <el-icon><Download /></el-icon>
                下载文件
              </el-button>
            </div>
          </div>
        </div>
      </div>
      
      <div v-else class="empty-state">
        <el-empty description="暂无附件" />
      </div>
    </div>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Picture, Download, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import fileApi from '@/api/file'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  attachments: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

// 响应式数据
const loading = ref(false)
const currentIndex = ref(0)

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const currentAttachment = computed(() => {
  if (!props.attachments || props.attachments.length === 0) return null
  return props.attachments[currentIndex.value]
})

// 方法
const selectAttachment = (index) => {
  currentIndex.value = index
}

const previousAttachment = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
  }
}

const nextAttachment = () => {
  if (currentIndex.value < props.attachments.length - 1) {
    currentIndex.value++
  }
}

const isImage = (fileName) => {
  if (!fileName) return false
  const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp']
  const extension = fileName.toLowerCase().substring(fileName.lastIndexOf('.'))
  return imageExtensions.includes(extension)
}

const isPdf = (fileName) => {
  if (!fileName) return false
  return fileName.toLowerCase().endsWith('.pdf')
}

const getPreviewUrl = (attachment) => {
  if (!attachment || !attachment.filePath) return ''
  // 构建预览URL，这里假设后端提供了文件预览接口
  return `${import.meta.env.VITE_API_BASE_URL}/api/files/preview/${attachment.id}`
}

const downloadAttachment = async (attachment) => {
  try {
    loading.value = true
    const response = await fileApi.downloadFile(attachment.id)
    
    // 创建下载链接
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = attachment.fileName
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('文件下载成功')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  } finally {
    loading.value = false
  }
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  const units = ['B', 'KB', 'MB', 'GB']
  let index = 0
  let fileSize = size
  
  while (fileSize >= 1024 && index < units.length - 1) {
    fileSize /= 1024
    index++
  }
  
  return `${fileSize.toFixed(1)} ${units[index]}`
}

const handleImageLoad = () => {
  // 图片加载成功
}

const handleImageError = () => {
  ElMessage.error('图片加载失败')
}

const handleClose = () => {
  dialogVisible.value = false
  currentIndex.value = 0
}

// 监听器
watch(() => props.attachments, () => {
  currentIndex.value = 0
})
</script>

<style scoped>
.attachment-container {
  display: flex;
  height: 600px;
}

.attachment-list {
  width: 300px;
  border-right: 1px solid #EBEEF5;
  padding-right: 16px;
  margin-right: 16px;
  overflow-y: auto;
}

.attachment-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border: 1px solid #EBEEF5;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.attachment-item:hover {
  border-color: #409EFF;
  background-color: #F0F9FF;
}

.attachment-item.active {
  border-color: #409EFF;
  background-color: #E1F3FF;
}

.attachment-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.file-icon {
  font-size: 24px;
  color: #606266;
  margin-right: 12px;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #909399;
}

.attachment-actions {
  margin-left: 8px;
}

.preview-container {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #EBEEF5;
}

.preview-header h4 {
  margin: 0;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.preview-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.image-preview {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-preview img {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  border-radius: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.pdf-preview {
  width: 100%;
  height: 100%;
}

.file-preview {
  text-align: center;
  padding: 40px;
}

.file-icon-large {
  margin-bottom: 20px;
  color: #C0C4CC;
}

.file-info h3 {
  margin: 0 0 12px 0;
  color: #303133;
}

.file-info p {
  margin: 8px 0;
  color: #606266;
}

.empty-state {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 滚动条样式 */
.attachment-list::-webkit-scrollbar {
  width: 6px;
}

.attachment-list::-webkit-scrollbar-track {
  background: #F5F7FA;
  border-radius: 3px;
}

.attachment-list::-webkit-scrollbar-thumb {
  background: #C0C4CC;
  border-radius: 3px;
}

.attachment-list::-webkit-scrollbar-thumb:hover {
  background: #A4A9AE;
}
</style>