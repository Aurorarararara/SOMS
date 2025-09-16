<template>
  <el-dialog
    v-model="dialogVisible"
    title="报销申请详情"
    width="80%"
    :before-close="handleClose"
    class="approval-detail-dialog"
  >
    <div v-if="applicationData" class="detail-container">
      <!-- 基本信息 -->
      <div class="info-section">
        <h3 class="section-title">
          <el-icon><InfoFilled /></el-icon>
          基本信息
        </h3>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">申请编号：</span>
            <span class="value">{{ applicationData.applicationNo }}</span>
          </div>
          <div class="info-item">
            <span class="label">申请标题：</span>
            <span class="value">{{ applicationData.title }}</span>
          </div>
          <div class="info-item">
            <span class="label">申请人：</span>
            <span class="value">{{ applicationData.applicantName }}</span>
          </div>
          <div class="info-item">
            <span class="label">部门：</span>
            <span class="value">{{ applicationData.departmentName }}</span>
          </div>
          <div class="info-item">
            <span class="label">报销类型：</span>
            <el-tag :type="getTypeTagType(applicationData.type)">
              {{ getTypeText(applicationData.type) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">紧急程度：</span>
            <el-tag :type="getUrgencyTagType(applicationData.urgency)">
              {{ getUrgencyText(applicationData.urgency) }}
            </el-tag>
          </div>
          <div class="info-item">
            <span class="label">申请时间：</span>
            <span class="value">{{ formatTime(applicationData.submitTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">当前状态：</span>
            <el-tag :type="getStatusTagType(applicationData.status)">
              {{ getStatusText(applicationData.status) }}
            </el-tag>
          </div>
        </div>
        <div v-if="applicationData.description" class="description">
          <span class="label">申请说明：</span>
          <p class="description-text">{{ applicationData.description }}</p>
        </div>
      </div>

      <!-- 报销明细 -->
      <div class="detail-section">
        <h3 class="section-title">
          <el-icon><List /></el-icon>
          报销明细
        </h3>
        <el-table :data="applicationData.details" class="detail-table">
          <el-table-column prop="expenseDate" label="费用日期" width="120">
            <template #default="{ row }">
              {{ formatDate(row.expenseDate) }}
            </template>
          </el-table-column>
          <el-table-column prop="category" label="费用类别" width="120" />
          <el-table-column prop="description" label="费用说明" min-width="200" show-overflow-tooltip />
          <el-table-column prop="amount" label="金额" width="120">
            <template #default="{ row }">
              <span class="amount">¥{{ formatAmount(row.amount) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="remarks" label="备注" min-width="150" show-overflow-tooltip />
        </el-table>
        <div class="total-amount">
          <span class="total-label">总计金额：</span>
          <span class="total-value">¥{{ formatAmount(applicationData.totalAmount) }}</span>
        </div>
      </div>

      <!-- 附件信息 -->
      <div v-if="applicationData.attachments && applicationData.attachments.length > 0" class="attachment-section">
        <h3 class="section-title">
          <el-icon><Paperclip /></el-icon>
          附件信息
        </h3>
        <div class="attachment-list">
          <div
            v-for="(file, index) in applicationData.attachments"
            :key="file.id || index"
            class="attachment-item"
          >
            <div class="file-icon">
              <el-icon><Document /></el-icon>
            </div>
            <div class="file-info">
              <div class="file-name" :title="file.fileName">
                {{ file.fileName }}
              </div>
              <div class="file-meta">
                <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
                <span class="file-time">{{ formatTime(file.uploadTime) }}</span>
              </div>
            </div>
            <div class="file-actions">
              <el-button size="small" @click="previewFile(file)">
                <el-icon><View /></el-icon>
                预览
              </el-button>
              <el-button size="small" @click="downloadFile(file)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <!-- 审批历史 -->
      <div v-if="approvalHistory.length > 0" class="history-section">
        <h3 class="section-title">
          <el-icon><Clock /></el-icon>
          审批历史
        </h3>
        <el-timeline class="approval-timeline">
          <el-timeline-item
            v-for="(record, index) in approvalHistory"
            :key="index"
            :timestamp="formatTime(record.approvalTime)"
            :type="getTimelineType(record.result)"
          >
            <div class="timeline-content">
              <div class="timeline-header">
                <span class="approver">{{ record.approverName }}</span>
                <el-tag :type="getResultTagType(record.result)" size="small">
                  {{ getResultText(record.result) }}
                </el-tag>
              </div>
              <div v-if="record.comment" class="timeline-comment">
                {{ record.comment }}
              </div>
            </div>
          </el-timeline-item>
        </el-timeline>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">关闭</el-button>
        <el-button
          v-if="applicationData && applicationData.status === 'PENDING'"
          type="primary"
          @click="handleApproval"
        >
          <el-icon><Edit /></el-icon>
          审批
        </el-button>
      </div>
    </template>

    <!-- 文件预览对话框 -->
    <FilePreviewDialog
      v-model="showPreviewDialog"
      :file-data="previewFileData"
    />
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  InfoFilled,
  List,
  Paperclip,
  Document,
  View,
  Download,
  Clock,
  Edit
} from '@element-plus/icons-vue'
import expenseApi from '@/api/expense'
import fileApi from '@/api/file'
import FilePreviewDialog from '@/components/FilePreviewDialog.vue'

// Props
const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  applicationData: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'approval'])

// 响应式数据
const approvalHistory = ref([])
const showPreviewDialog = ref(false)
const previewFileData = ref(null)

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 监听器
watch(() => props.applicationData, (newData) => {
  if (newData && props.modelValue) {
    loadApprovalHistory()
  }
}, { immediate: true })

watch(() => props.modelValue, (visible) => {
  if (visible && props.applicationData) {
    loadApprovalHistory()
  }
})

// 方法
const loadApprovalHistory = async () => {
  if (!props.applicationData?.id) return
  
  try {
    const response = await expenseApi.getApprovalHistory(props.applicationData.id)
    approvalHistory.value = response.data
  } catch (error) {
    console.error('加载审批历史失败:', error)
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

const handleApproval = () => {
  emit('approval', props.applicationData)
}

const handleClose = () => {
  dialogVisible.value = false
}

// 工具方法
const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝',
    'WITHDRAWN': '已撤回'
  }
  return statusMap[status] || status
}

const getStatusTagType = (status) => {
  const typeMap = {
    'DRAFT': 'info',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'WITHDRAWN': 'info'
  }
  return typeMap[status] || 'info'
}

const getTypeText = (type) => {
  const typeMap = {
    'TRAVEL': '差旅费',
    'MEAL': '餐费',
    'TRANSPORT': '交通费',
    'OFFICE': '办公用品',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

const getTypeTagType = (type) => {
  const typeMap = {
    'TRAVEL': 'primary',
    'MEAL': 'success',
    'TRANSPORT': 'warning',
    'OFFICE': 'info',
    'OTHER': 'default'
  }
  return typeMap[type] || 'default'
}

const getUrgencyText = (urgency) => {
  const urgencyMap = {
    'LOW': '普通',
    'MEDIUM': '紧急',
    'HIGH': '非常紧急'
  }
  return urgencyMap[urgency] || urgency
}

const getUrgencyTagType = (urgency) => {
  const typeMap = {
    'LOW': 'info',
    'MEDIUM': 'warning',
    'HIGH': 'danger'
  }
  return typeMap[urgency] || 'info'
}

const getResultText = (result) => {
  const resultMap = {
    'APPROVED': '通过',
    'REJECTED': '拒绝',
    'PENDING': '待审批'
  }
  return resultMap[result] || result
}

const getResultTagType = (result) => {
  const typeMap = {
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PENDING': 'warning'
  }
  return typeMap[result] || 'info'
}

const getTimelineType = (result) => {
  const typeMap = {
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PENDING': 'warning'
  }
  return typeMap[result] || 'primary'
}

const formatAmount = (amount) => {
  return Number(amount || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

const formatTime = (time) => {
  return new Date(time).toLocaleString('zh-CN')
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatFileSize = (size) => {
  if (!size) return '0 B'
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}
</script>

<style scoped>
.approval-detail-dialog {
  --el-dialog-margin-top: 5vh;
}

.approval-detail-dialog :deep(.el-dialog) {
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.approval-detail-dialog :deep(.el-dialog__body) {
  flex: 1;
  padding: 0;
  overflow-y: auto;
}

.detail-container {
  padding: 20px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #f0f0f0;
}

.info-section,
.detail-section,
.attachment-section,
.history-section {
  margin-bottom: 32px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
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
  min-width: 80px;
}

.value {
  color: #303133;
  flex: 1;
}

.description {
  margin-top: 16px;
}

.description-text {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 4px;
  margin: 8px 0 0 0;
  line-height: 1.6;
  color: #606266;
}

.detail-table {
  width: 100%;
  margin-bottom: 16px;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.total-amount {
  text-align: right;
  padding: 12px 0;
  border-top: 2px solid #f0f0f0;
}

.total-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.total-value {
  font-size: 18px;
  font-weight: 700;
  color: #f56c6c;
  margin-left: 8px;
}

.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.file-icon {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e1f3ff;
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

.approval-timeline {
  padding-left: 0;
}

.timeline-content {
  padding-bottom: 16px;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.approver {
  font-weight: 600;
  color: #303133;
}

.timeline-comment {
  background: #f8f9fa;
  padding: 8px 12px;
  border-radius: 4px;
  color: #606266;
  line-height: 1.5;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px;
  border-top: 1px solid #ebeef5;
}

@media (max-width: 768px) {
  .approval-detail-dialog {
    --el-dialog-margin-top: 2vh;
  }
  
  .approval-detail-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 2vh auto;
  }
  
  .detail-container {
    padding: 16px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .attachment-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .file-actions {
    width: 100%;
    justify-content: flex-end;
  }
}
</style>