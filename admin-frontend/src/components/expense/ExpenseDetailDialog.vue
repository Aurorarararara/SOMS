<template>
  <el-dialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="80%"
    :before-close="handleClose"
    destroy-on-close
  >
    <div v-loading="loading" class="expense-detail">
      <div v-if="expenseData" class="detail-content">
        <!-- 基本信息 -->
        <el-card class="info-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
              <el-tag :type="getStatusType(expenseData.status)">{{ getStatusText(expenseData.status) }}</el-tag>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <label>申请编号：</label>
                <span>{{ expenseData.id }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>申请人：</label>
                <span>{{ expenseData.applicantName }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>所属部门：</label>
                <span>{{ expenseData.departmentName }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>报销标题：</label>
                <span>{{ expenseData.title }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>报销金额：</label>
                <span class="amount">¥{{ expenseData.totalAmount?.toFixed(2) || '0.00' }}</span>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <label>申请时间：</label>
                <span>{{ formatDate(expenseData.createdAt) }}</span>
              </div>
            </el-col>
            <el-col :span="24">
              <div class="info-item">
                <label>报销说明：</label>
                <span>{{ expenseData.description || '无' }}</span>
              </div>
            </el-col>
          </el-row>
        </el-card>

        <!-- 报销明细 -->
        <el-card class="detail-card" shadow="never">
          <template #header>
            <span>报销明细</span>
          </template>
          
          <el-table :data="expenseData.items" border style="width: 100%">
            <el-table-column prop="category" label="费用类别" width="120" />
            <el-table-column prop="description" label="费用说明" min-width="200" />
            <el-table-column prop="amount" label="金额" width="120" align="right">
              <template #default="{ row }">
                <span class="amount">¥{{ row.amount?.toFixed(2) || '0.00' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="date" label="发生日期" width="120">
              <template #default="{ row }">
                {{ formatDate(row.date) }}
              </template>
            </el-table-column>
            <el-table-column label="凭证" width="100" align="center">
              <template #default="{ row }">
                <el-button 
                  v-if="row.attachments && row.attachments.length > 0"
                  type="primary" 
                  size="small" 
                  @click="viewAttachments(row.attachments)"
                >
                  查看凭证
                </el-button>
                <span v-else>无</span>
              </template>
            </el-table-column>
          </el-table>
          
          <div class="total-row">
            <strong>总计：¥{{ expenseData.totalAmount?.toFixed(2) || '0.00' }}</strong>
          </div>
        </el-card>

        <!-- 审批历史 -->
        <el-card v-if="approvalHistory.length > 0" class="history-card" shadow="never">
          <template #header>
            <span>审批历史</span>
          </template>
          
          <el-timeline>
            <el-timeline-item
              v-for="(item, index) in approvalHistory"
              :key="index"
              :timestamp="formatDate(item.createdAt)"
              :type="getTimelineType(item.action)"
            >
              <div class="timeline-content">
                <div class="timeline-header">
                  <span class="action">{{ getActionText(item.action) }}</span>
                  <span class="approver">{{ item.approverName }}</span>
                </div>
                <div v-if="item.comment" class="timeline-comment">
                  {{ item.comment }}
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <!-- 管理员审批操作 -->
        <el-card v-if="isAdmin && showApproval && expenseData.status === 'PENDING'" class="approval-card" shadow="never">
          <template #header>
            <span>审批操作</span>
          </template>
          
          <el-form :model="approvalForm" label-width="80px">
            <el-form-item label="审批意见">
              <el-input
                v-model="approvalForm.comment"
                type="textarea"
                :rows="4"
                placeholder="请输入审批意见"
              />
            </el-form-item>
            <el-form-item>
              <el-button type="success" @click="handleApprove">
                <el-icon><Check /></el-icon>
                通过
              </el-button>
              <el-button type="danger" @click="handleReject">
                <el-icon><Close /></el-icon>
                拒绝
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>
    </div>

    <!-- 附件预览对话框 -->
    <AttachmentDialog
      v-model="attachmentDialogVisible"
      :attachments="selectedAttachments"
    />
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import AttachmentDialog from './AttachmentDialog.vue'
import expenseApi from '@/api/expense'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  expenseId: {
    type: [String, Number],
    default: null
  },
  isAdmin: {
    type: Boolean,
    default: false
  },
  showApproval: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'refresh'])

// 响应式数据
const loading = ref(false)
const expenseData = ref(null)
const approvalHistory = ref([])
const attachmentDialogVisible = ref(false)
const selectedAttachments = ref([])

// 审批表单
const approvalForm = reactive({
  comment: ''
})

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const dialogTitle = computed(() => {
  if (!expenseData.value) return '报销详情'
  return `报销详情 - ${expenseData.value.title}`
})

// 状态映射
const statusMap = {
  DRAFT: { text: '草稿', type: 'info' },
  PENDING: { text: '待审批', type: 'warning' },
  APPROVED: { text: '已通过', type: 'success' },
  REJECTED: { text: '已拒绝', type: 'danger' },
  WITHDRAWN: { text: '已撤回', type: 'info' }
}

const actionMap = {
  SUBMIT: '提交申请',
  APPROVE: '审批通过',
  REJECT: '审批拒绝',
  WITHDRAW: '撤回申请'
}

// 方法
const loadExpenseDetail = async () => {
  if (!props.expenseId) return
  
  loading.value = true
  try {
    const api = props.isAdmin ? expenseApi.getApplicationDetailForAdmin : expenseApi.getApplicationDetail
    const response = await api(props.expenseId)
    expenseData.value = response.data
    
    // 加载审批历史
    await loadApprovalHistory()
  } catch (error) {
    console.error('加载报销详情失败:', error)
    ElMessage.error('加载报销详情失败')
  } finally {
    loading.value = false
  }
}

const loadApprovalHistory = async () => {
  try {
    const response = await expenseApi.getApprovalHistory(props.expenseId)
    approvalHistory.value = response.data || []
  } catch (error) {
    console.error('加载审批历史失败:', error)
  }
}

const handleApprove = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要通过这个报销申请吗？',
      '确认审批',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await expenseApi.approveApplication(props.expenseId, {
      comment: approvalForm.comment
    })
    
    ElMessage.success('审批通过')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  }
}

const handleReject = async () => {
  if (!approvalForm.comment.trim()) {
    ElMessage.warning('拒绝申请时必须填写审批意见')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      '确定要拒绝这个报销申请吗？',
      '确认审批',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    await expenseApi.rejectApplication(props.expenseId, {
      comment: approvalForm.comment
    })
    
    ElMessage.success('审批拒绝')
    emit('refresh')
    handleClose()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审批失败:', error)
      ElMessage.error('审批失败')
    }
  }
}

const viewAttachments = (attachments) => {
  selectedAttachments.value = attachments
  attachmentDialogVisible.value = true
}

const handleClose = () => {
  dialogVisible.value = false
  expenseData.value = null
  approvalHistory.value = []
  approvalForm.comment = ''
}

const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || status
}

const getActionText = (action) => {
  return actionMap[action] || action
}

const getTimelineType = (action) => {
  switch (action) {
    case 'APPROVE':
      return 'success'
    case 'REJECT':
      return 'danger'
    case 'SUBMIT':
      return 'primary'
    default:
      return 'info'
  }
}

const formatDate = (dateString) => {
  if (!dateString) return '-'
  return new Date(dateString).toLocaleString('zh-CN')
}

// 监听器
watch(() => props.modelValue, (newVal) => {
  if (newVal && props.expenseId) {
    loadExpenseDetail()
  }
})

watch(() => props.expenseId, (newVal) => {
  if (newVal && props.modelValue) {
    loadExpenseDetail()
  }
})
</script>

<style scoped>
.expense-detail {
  min-height: 400px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card,
.detail-card,
.history-card,
.approval-card {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-item {
  margin-bottom: 16px;
}

.info-item label {
  font-weight: 600;
  color: #606266;
  margin-right: 8px;
}

.amount {
  font-weight: 600;
  color: #E6A23C;
}

.total-row {
  text-align: right;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #EBEEF5;
  font-size: 16px;
}

.timeline-content {
  padding-left: 10px;
}

.timeline-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 4px;
}

.action {
  font-weight: 600;
  color: #303133;
}

.approver {
  color: #606266;
  font-size: 14px;
}

.timeline-comment {
  color: #909399;
  font-size: 14px;
  line-height: 1.5;
}
</style>