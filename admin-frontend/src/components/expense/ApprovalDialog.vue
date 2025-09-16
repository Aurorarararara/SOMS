<template>
  <el-dialog
    v-model="visible"
    title="审批报销申请"
    width="600px"
    :before-close="handleClose"
  >
    <div class="approval-dialog">
      <!-- 申请信息 -->
      <div class="application-info">
        <h4>申请信息</h4>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="申请人">{{ applicationData.employeeName }}</el-descriptions-item>
          <el-descriptions-item label="部门">{{ applicationData.departmentName }}</el-descriptions-item>
          <el-descriptions-item label="申请金额">¥{{ applicationData.totalAmount }}</el-descriptions-item>
          <el-descriptions-item label="申请时间">{{ formatDate(applicationData.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="申请事由" :span="2">
            {{ applicationData.reason }}
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 审批操作 -->
      <div class="approval-action">
        <h4>审批操作</h4>
        <el-form ref="approvalFormRef" :model="approvalForm" :rules="approvalRules" label-width="80px">
          <el-form-item label="审批结果" prop="status">
            <el-radio-group v-model="approvalForm.status">
              <el-radio :label="'APPROVED'" size="large">通过</el-radio>
              <el-radio :label="'REJECTED'" size="large">拒绝</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="审批意见" prop="comment">
            <el-input
              v-model="approvalForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审批意见"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-form>
      </div>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          提交审批
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import expenseApi from '@/api/expense'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  applicationData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const visible = ref(false)
const loading = ref(false)
const approvalFormRef = ref()

// 审批表单
const approvalForm = reactive({
  status: 'APPROVED',
  comment: ''
})

// 表单验证规则
const approvalRules = {
  status: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入审批意见', trigger: 'blur' },
    { min: 5, max: 500, message: '审批意见长度在 5 到 500 个字符', trigger: 'blur' }
  ]
}

// 监听显示状态
watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    resetForm()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 重置表单
const resetForm = () => {
  approvalForm.status = 'APPROVED'
  approvalForm.comment = ''
  if (approvalFormRef.value) {
    approvalFormRef.value.clearValidate()
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

// 关闭对话框
const handleClose = () => {
  visible.value = false
}

// 提交审批
const handleSubmit = async () => {
  if (!approvalFormRef.value) return
  
  try {
    await approvalFormRef.value.validate()
    
    await ElMessageBox.confirm(
      `确定要${approvalForm.status === 'APPROVED' ? '通过' : '拒绝'}这个报销申请吗？`,
      '确认审批',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    loading.value = true
    
    const approvalData = {
      applicationId: props.applicationData.id,
      status: approvalForm.status,
      comment: approvalForm.comment
    }
    
    await expenseApi.approveApplication(approvalData)
    
    ElMessage.success('审批提交成功')
    emit('success')
    handleClose()
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('审批失败:', error)
      ElMessage.error(error.message || '审批失败，请重试')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.approval-dialog {
  padding: 10px 0;
}

.application-info,
.approval-action {
  margin-bottom: 20px;
}

.application-info h4,
.approval-action h4 {
  margin: 0 0 15px 0;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.dialog-footer {
  text-align: right;
}

:deep(.el-descriptions__label) {
  font-weight: 600;
}

:deep(.el-radio) {
  margin-right: 20px;
}
</style>