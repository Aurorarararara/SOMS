<template>
  <el-dialog
    v-model="dialogVisible"
    title="报销申请详情"
    width="900px"
    :close-on-click-modal="false"
  >
    <div v-loading="loading" class="expense-detail">
      <div v-if="applicationData" class="detail-content">
        <!-- 基本信息 -->
        <div class="info-section">
          <h3 class="section-title">
            <el-icon><InfoFilled /></el-icon>
            基本信息
          </h3>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>申请编号：</label>
                <span>{{ applicationData.applicationNumber }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>申请状态：</label>
                <el-tag :type="getStatusType(applicationData.status)">
                  {{ getStatusText(applicationData.status) }}
                </el-tag>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>申请标题：</label>
                <span>{{ applicationData.title }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>报销类型：</label>
                <span>{{ getExpenseTypeText(applicationData.expenseType) }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>申请金额：</label>
                <span class="amount">¥{{ applicationData.totalAmount.toFixed(2) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>发生日期：</label>
                <span>{{ formatDate(applicationData.expenseDate) }}</span>
              </div>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="24">
              <div class="info-item">
                <label>申请说明：</label>
                <p class="description">{{ applicationData.description }}</p>
              </div>
            </el-col>
          </el-row>
          <el-row :gutter="20">
            <el-col :span="12">
              <div class="info-item">
                <label>创建时间：</label>
                <span>{{ formatDateTime(applicationData.createTime) }}</span>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="info-item">
                <label>更新时间：</label>
                <span>{{ formatDateTime(applicationData.updateTime) }}</span>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 报销明细 -->
        <div class="info-section">
          <h3 class="section-title">
            <el-icon><List /></el-icon>
            报销明细
          </h3>
          <div v-if="applicationData.expenseItems && applicationData.expenseItems.length > 0">
            <el-table :data="applicationData.expenseItems" border>
              <el-table-column prop="itemName" label="项目名称" min-width="150" />
              <el-table-column prop="amount" label="金额" width="120">
                <template #default="scope">
                  <span class="amount">¥{{ scope.row.amount.toFixed(2) }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="description" label="说明" min-width="200" />
              <el-table-column prop="expenseDate" label="发生日期" width="120">
                <template #default="scope">
                  {{ formatDate(scope.row.expenseDate) }}
                </template>
              </el-table-column>
            </el-table>
            <div class="total-amount">
              <strong>总计：¥{{ applicationData.totalAmount.toFixed(2) }}</strong>
            </div>
          </div>
          <div v-else class="empty-data">
            <el-empty description="暂无明细数据" :image-size="80" />
          </div>
        </div>

        <!-- 附件信息 -->
        <div class="info-section">
          <h3 class="section-title">
            <el-icon><Paperclip /></el-icon>
            附件信息
          </h3>
          <div v-if="applicationData.attachments && applicationData.attachments.length > 0">
            <div class="attachments-grid">
              <div
                v-for="attachment in applicationData.attachments"
                :key="attachment.id"
                class="attachment-item"
              >
                <div class="attachment-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="attachment-info">
                  <div class="attachment-name">{{ attachment.fileName }}</div>
                  <div class="attachment-size">{{ formatFileSize(attachment.fileSize) }}</div>
                </div>
                <div class="attachment-actions">
                  <el-button size="small" @click="previewFile(attachment)">
                    <el-icon><View /></el-icon>
                    预览
                  </el-button>
                  <el-button size="small" @click="downloadFile(attachment)">
                    <el-icon><Download /></el-icon>
                    下载
                  </el-button>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="empty-data">
            <el-empty description="暂无附件" :image-size="80" />
          </div>
        </div>

        <!-- 审批历史 -->
        <div class="info-section">
          <h3 class="section-title">
            <el-icon><Clock /></el-icon>
            审批历史
          </h3>
          <div v-if="approvalHistory && approvalHistory.length > 0">
            <el-timeline>
              <el-timeline-item
                v-for="(record, index) in approvalHistory"
                :key="index"
                :timestamp="formatDateTime(record.createTime)"
                placement="top"
              >
                <el-card>
                  <div class="approval-record">
                    <div class="record-header">
                      <span class="approver">{{ record.approverName }}</span>
                      <el-tag :type="getApprovalStatusType(record.status)">
                        {{ getApprovalStatusText(record.status) }}
                      </el-tag>
                    </div>
                    <div v-if="record.comment" class="record-comment">
                      <strong>审批意见：</strong>{{ record.comment }}
                    </div>
                  </div>
                </el-card>
              </el-timeline-item>
            </el-timeline>
          </div>
          <div v-else class="empty-data">
            <el-empty description="暂无审批记录" :image-size="80" />
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button
          v-if="applicationData && applicationData.status === 'DRAFT'"
          type="primary"
          @click="editApplication"
        >
          编辑申请
        </el-button>
        <el-button
          v-if="applicationData && applicationData.status === 'PENDING'"
          type="warning"
          @click="withdrawApplication"
        >
          撤回申请
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
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  InfoFilled,
  List,
  Paperclip,
  Clock,
  Document,
  View,
  Download
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
  applicationId: {
    type: [String, Number],
    default: null
  }
})

// Emits
const emit = defineEmits(['update:modelValue', 'edit', 'withdraw'])

// 响应式数据
const loading = ref(false)
const applicationData = ref(null)
const approvalHistory = ref([])
const showPreviewDialog = ref(false)
const previewFileData = ref(null)

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

// 监听器
watch(() => props.applicationId, (newId) => {
  if (newId && props.modelValue) {
    loadApplicationDetail()
    loadApprovalHistory()
  }
}, { immediate: true })

watch(() => props.modelValue, (visible) => {
  if (visible && props.applicationId) {
    loadApplicationDetail()
    loadApprovalHistory()
  }
})

// 方法
const loadApplicationDetail = async () => {
  try {
    loading.value = true
    const response = await expenseApi.getApplicationDetail(props.applicationId)
    applicationData.value = response.data
  } catch (error) {
    ElMessage.error('加载申请详情失败')
  } finally {
    loading.value = false
  }
}

const loadApprovalHistory = async () => {
  try {
    const response = await expenseApi.getApprovalHistory(props.applicationId)
    approvalHistory.value = response.data
  } catch (error) {
    console.error('加载审批历史失败:', error)
  }
}

const editApplication = () => {
  emit('edit', applicationData.value)
  dialogVisible.value = false
}

const withdrawApplication = async () => {
  try {
    await ElMessageBox.confirm('确定要撤回此申请吗？', '确认撤回', {
      type: 'warning'
    })
    
    await expenseApi.withdrawApplication(props.applicationId)
    ElMessage.success('申请撤回成功')
    emit('withdraw')
    dialogVisible.value = false
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('撤回申请失败')
    }
  }
}

const previewFile = async (attachment) => {
  try {
    const response = await fileApi.previewFile(attachment.id)
    previewFileData.value = {
      ...attachment,
      previewUrl: response.data.previewUrl
    }
    showPreviewDialog.value = true
  } catch (error) {
    ElMessage.error('文件预览失败')
  }
}

const downloadFile = async (attachment) => {
  try {
    const response = await fileApi.downloadFile(attachment.id)
    const blob = new Blob([response.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = attachment.fileName
    link.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('文件下载成功')
  } catch (error) {
    ElMessage.error('文件下载失败')
  }
}

const getStatusType = (status) => {
  const statusMap = {
    'DRAFT': '',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return statusMap[status] || ''
}

const getStatusText = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'PENDING': '待审批',
    'APPROVED': '已通过',
    'REJECTED': '已拒绝'
  }
  return statusMap[status] || status
}

const getExpenseTypeText = (type) => {
  const typeMap = {
    'TRAVEL': '差旅费',
    'MEAL': '餐费',
    'TRANSPORT': '交通费',
    'ACCOMMODATION': '住宿费',
    'OFFICE_SUPPLIES': '办公用品',
    'TRAINING': '培训费',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

const getApprovalStatusType = (status) => {
  const statusMap = {
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'PENDING': 'warning'
  }
  return statusMap[status] || ''
}

const getApprovalStatusText = (status) => {
  const statusMap = {
    'APPROVED': '通过',
    'REJECTED': '拒绝',
    'PENDING': '待审批'
  }
  return statusMap[status] || status
}

const formatDate = (date) => {
  return new Date(date).toLocaleDateString('zh-CN')
}

const formatDateTime = (date) => {
  return new Date(date).toLocaleString('zh-CN')
}

const formatFileSize = (size) => {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / (1024 * 1024)).toFixed(1) + ' MB'
}
</script>

<style scoped>
.expense-detail {
  padding: 20px 0;
}

.detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.info-section {
  margin-bottom: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.section-title {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
  padding-bottom: 10px;
  border-bottom: 2px solid #409eff;
}

.section-title .el-icon {
  margin-right: 8px;
  color: #409eff;
}

.info-item {
  display: flex;
  align-items: flex-start;
  margin-bottom: 12px;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  min-width: 100px;
  margin-right: 12px;
}

.info-item span {
  color: #303133;
}

.description {
  margin: 0;
  padding: 8px 12px;
  background: white;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  line-height: 1.6;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.total-amount {
  text-align: right;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 2px solid #409eff;
  font-size: 16px;
}

.empty-data {
  text-align: center;
  padding: 20px;
}

.attachments-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 12px;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.attachment-icon {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f9ff;
  border-radius: 4px;
  margin-right: 12px;
  color: #409eff;
  font-size: 20px;
}

.attachment-info {
  flex: 1;
  min-width: 0;
}

.attachment-name {
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.attachment-size {
  color: #909399;
  font-size: 12px;
}

.attachment-actions {
  display: flex;
  gap: 8px;
}

.approval-record {
  padding: 0;
}

.record-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.approver {
  font-weight: 600;
  color: #303133;
}

.record-comment {
  color: #606266;
  line-height: 1.6;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 12px;
}
</style>