<template>
  <el-dialog
    v-model="dialogVisible"
    title="审批报销申请"
    width="600px"
    :before-close="handleClose"
    class="approval-dialog"
  >
    <div v-if="applicationData" class="approval-container">
      <!-- 申请信息摘要 -->
      <div class="application-summary">
        <h4 class="summary-title">申请信息</h4>
        <div class="summary-content">
          <div class="summary-item">
            <span class="label">申请编号：</span>
            <span class="value">{{ applicationData.applicationNo }}</span>
          </div>
          <div class="summary-item">
            <span class="label">申请标题：</span>
            <span class="value">{{ applicationData.title }}</span>
          </div>
          <div class="summary-item">
            <span class="label">申请人：</span>
            <span class="value">{{ applicationData.applicantName }}</span>
          </div>
          <div class="summary-item">
            <span class="label">总金额：</span>
            <span class="value amount">¥{{ formatAmount(applicationData.totalAmount) }}</span>
          </div>
        </div>
      </div>

      <!-- 审批表单 -->
      <el-form
        ref="formRef"
        :model="approvalForm"
        :rules="formRules"
        label-width="100px"
        class="approval-form"
      >
        <el-form-item label="审批结果" prop="result" required>
          <el-radio-group v-model="approvalForm.result" @change="handleResultChange">
            <el-radio value="APPROVED">
              <el-icon color="#67c23a"><Check /></el-icon>
              通过
            </el-radio>
            <el-radio value="REJECTED">
              <el-icon color="#f56c6c"><Close /></el-icon>
              拒绝
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item
          :label="approvalForm.result === 'APPROVED' ? '审批意见' : '拒绝理由'"
          prop="comment"
          :required="approvalForm.result === 'REJECTED'"
        >
          <el-input
            v-model="approvalForm.comment"
            type="textarea"
            :rows="4"
            :placeholder="getCommentPlaceholder()"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>

        <!-- 审批金额调整 (仅通过时显示) -->
        <el-form-item
          v-if="approvalForm.result === 'APPROVED'"
          label="审批金额"
          prop="approvedAmount"
        >
          <div class="amount-adjustment">
            <el-input-number
              v-model="approvalForm.approvedAmount"
              :min="0"
              :max="applicationData.totalAmount"
              :precision="2"
              :step="0.01"
              style="width: 200px"
            />
            <div class="amount-info">
              <div class="original-amount">
                申请金额: ¥{{ formatAmount(applicationData.totalAmount) }}
              </div>
              <div v-if="amountDifference !== 0" class="amount-difference">
                <span :class="amountDifference > 0 ? 'increase' : 'decrease'">
                  {{ amountDifference > 0 ? '+' : '' }}¥{{ formatAmount(Math.abs(amountDifference)) }}
                </span>
              </div>
            </div>
          </div>
        </el-form-item>

        <!-- 审批级别 -->
        <el-form-item label="审批级别" prop="approvalLevel">
          <el-select v-model="approvalForm.approvalLevel" placeholder="请选择审批级别">
            <el-option label="一级审批" value="LEVEL_1" />
            <el-option label="二级审批" value="LEVEL_2" />
            <el-option label="三级审批" value="LEVEL_3" />
          </el-select>
        </el-form-item>

        <!-- 后续处理 -->
        <el-form-item v-if="approvalForm.result === 'APPROVED'" label="后续处理">
          <el-checkbox-group v-model="approvalForm.nextActions">
            <el-checkbox value="NOTIFY_FINANCE">通知财务部门</el-checkbox>
            <el-checkbox value="NOTIFY_APPLICANT">通知申请人</el-checkbox>
            <el-checkbox value="AUTO_REIMBURSE">自动打款</el-checkbox>
          </el-checkbox-group>
        </el-form-item>

        <!-- 抄送人员 -->
        <el-form-item label="抄送人员">
          <el-select
            v-model="approvalForm.ccUsers"
            multiple
            filterable
            placeholder="选择抄送人员"
            style="width: 100%"
          >
            <el-option
              v-for="user in ccUserOptions"
              :key="user.id"
              :label="user.name"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 审批预览 -->
      <div v-if="approvalForm.result" class="approval-preview">
        <h4 class="preview-title">审批预览</h4>
        <div class="preview-content">
          <div class="preview-item">
            <span class="label">审批结果：</span>
            <el-tag :type="approvalForm.result === 'APPROVED' ? 'success' : 'danger'">
              {{ approvalForm.result === 'APPROVED' ? '通过' : '拒绝' }}
            </el-tag>
          </div>
          <div v-if="approvalForm.result === 'APPROVED'" class="preview-item">
            <span class="label">审批金额：</span>
            <span class="value amount">¥{{ formatAmount(approvalForm.approvedAmount) }}</span>
          </div>
          <div v-if="approvalForm.comment" class="preview-item">
            <span class="label">审批意见：</span>
            <span class="value">{{ approvalForm.comment }}</span>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button
          type="primary"
          :loading="submitting"
          @click="handleSubmit"
        >
          <el-icon><Check /></el-icon>
          确认审批
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Check, Close } from '@element-plus/icons-vue'
import expenseApi from '@/api/expense'
import userApi from '@/api/user'

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
const emit = defineEmits(['update:modelValue', 'success'])

// 响应式数据
const formRef = ref()
const submitting = ref(false)
const ccUserOptions = ref([])

const approvalForm = reactive({
  result: '',
  comment: '',
  approvedAmount: 0,
  approvalLevel: 'LEVEL_1',
  nextActions: ['NOTIFY_APPLICANT'],
  ccUsers: []
})

// 表单验证规则
const formRules = {
  result: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ],
  comment: [
    {
      validator: (rule, value, callback) => {
        if (approvalForm.result === 'REJECTED' && !value) {
          callback(new Error('拒绝时必须填写拒绝理由'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  approvedAmount: [
    {
      validator: (rule, value, callback) => {
        if (approvalForm.result === 'APPROVED') {
          if (value === null || value === undefined || value < 0) {
            callback(new Error('请输入有效的审批金额'))
          } else if (value > props.applicationData?.totalAmount) {
            callback(new Error('审批金额不能超过申请金额'))
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const amountDifference = computed(() => {
  if (!props.applicationData) return 0
  return approvalForm.approvedAmount - props.applicationData.totalAmount
})

// 监听器
watch(() => props.applicationData, (newData) => {
  if (newData) {
    resetForm()
  }
}, { immediate: true })

watch(() => props.modelValue, (visible) => {
  if (visible) {
    loadCcUsers()
  }
})

// 生命周期
onMounted(() => {
  loadCcUsers()
})

// 方法
const resetForm = () => {
  if (!props.applicationData) return
  
  Object.assign(approvalForm, {
    result: '',
    comment: '',
    approvedAmount: props.applicationData.totalAmount,
    approvalLevel: 'LEVEL_1',
    nextActions: ['NOTIFY_APPLICANT'],
    ccUsers: []
  })
  
  // 清除表单验证
  if (formRef.value) {
    formRef.value.clearValidate()
  }
}

const loadCcUsers = async () => {
  try {
    const response = await userApi.getUserList({ role: 'MANAGER' })
    ccUserOptions.value = response.data.records || []
  } catch (error) {
    console.error('加载抄送人员失败:', error)
  }
}

const handleResultChange = (result) => {
  if (result === 'APPROVED') {
    approvalForm.nextActions = ['NOTIFY_APPLICANT']
  } else {
    approvalForm.nextActions = []
  }
}

const getCommentPlaceholder = () => {
  if (approvalForm.result === 'APPROVED') {
    return '请输入审批意见（可选）'
  } else if (approvalForm.result === 'REJECTED') {
    return '请输入拒绝理由（必填）'
  }
  return '请先选择审批结果'
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    // 表单验证
    await formRef.value.validate()
    
    // 确认审批
    const confirmMessage = approvalForm.result === 'APPROVED' 
      ? `确定要通过此报销申请吗？审批金额：¥${formatAmount(approvalForm.approvedAmount)}`
      : '确定要拒绝此报销申请吗？'
    
    await ElMessageBox.confirm(confirmMessage, '确认审批', {
      type: 'warning'
    })
    
    submitting.value = true
    
    // 提交审批
    const approvalData = {
      applicationId: props.applicationData.id,
      result: approvalForm.result,
      comment: approvalForm.comment,
      approvedAmount: approvalForm.result === 'APPROVED' ? approvalForm.approvedAmount : null,
      approvalLevel: approvalForm.approvalLevel,
      nextActions: approvalForm.nextActions,
      ccUsers: approvalForm.ccUsers
    }
    
    await expenseApi.approveApplication(approvalData)
    
    ElMessage.success('审批成功')
    emit('success')
    handleClose()
    
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('审批失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
}

// 工具方法
const formatAmount = (amount) => {
  return Number(amount || 0).toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}
</script>

<style scoped>
.approval-dialog :deep(.el-dialog) {
  border-radius: 8px;
}

.approval-container {
  padding: 0;
}

.application-summary {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 6px;
  margin-bottom: 24px;
}

.summary-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.summary-content {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 8px;
}

.summary-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.value {
  color: #303133;
  flex: 1;
}

.amount {
  font-weight: 600;
  color: #f56c6c;
}

.approval-form {
  margin-bottom: 24px;
}

.approval-form :deep(.el-radio) {
  display: flex;
  align-items: center;
  margin-right: 24px;
  margin-bottom: 0;
}

.approval-form :deep(.el-radio__label) {
  display: flex;
  align-items: center;
  gap: 4px;
}

.amount-adjustment {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.amount-info {
  flex: 1;
}

.original-amount {
  font-size: 12px;
  color: #909399;
  margin-bottom: 4px;
}

.amount-difference {
  font-size: 12px;
  font-weight: 600;
}

.amount-difference .increase {
  color: #f56c6c;
}

.amount-difference .decrease {
  color: #67c23a;
}

.approval-preview {
  background: #f0f9ff;
  border: 1px solid #b3d8ff;
  border-radius: 6px;
  padding: 16px;
}

.preview-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.preview-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.preview-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

@media (max-width: 768px) {
  .approval-dialog :deep(.el-dialog) {
    width: 95% !important;
    margin: 5vh auto;
  }
  
  .summary-content {
    grid-template-columns: 1fr;
  }
  
  .summary-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .amount-adjustment {
    flex-direction: column;
    gap: 12px;
  }
  
  .approval-form :deep(.el-radio) {
    margin-right: 16px;
  }
}
</style>