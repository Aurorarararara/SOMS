<template>
  <el-dialog
    v-model="dialogVisible"
    :title="isEdit ? '编辑报销申请' : '新建报销申请'"
    width="800px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      class="expense-form"
    >
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="申请标题" prop="title">
            <el-input v-model="formData.title" placeholder="请输入申请标题" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="报销类型" prop="expenseType">
            <el-select v-model="formData.expenseType" placeholder="请选择报销类型" style="width: 100%">
              <el-option label="差旅费" value="TRAVEL" />
              <el-option label="餐费" value="MEAL" />
              <el-option label="交通费" value="TRANSPORT" />
              <el-option label="住宿费" value="ACCOMMODATION" />
              <el-option label="办公用品" value="OFFICE_SUPPLIES" />
              <el-option label="培训费" value="TRAINING" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>

      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="申请金额" prop="totalAmount">
            <el-input-number
              v-model="formData.totalAmount"
              :min="0"
              :precision="2"
              style="width: 100%"
              placeholder="请输入申请金额"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="发生日期" prop="expenseDate">
            <el-date-picker
              v-model="formData.expenseDate"
              type="date"
              placeholder="请选择发生日期"
              style="width: 100%"
            />
          </el-form-item>
        </el-col>
      </el-row>

      <el-form-item label="申请说明" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请详细说明报销事由和用途"
        />
      </el-form-item>

      <!-- 报销明细 -->
      <el-form-item label="报销明细">
        <div class="expense-items">
          <div class="items-header">
            <span>明细项目</span>
            <el-button type="primary" size="small" @click="addExpenseItem">
              <el-icon><Plus /></el-icon>
              添加明细
            </el-button>
          </div>
          
          <div v-if="formData.expenseItems.length === 0" class="empty-items">
            <el-empty description="暂无明细项目" :image-size="80" />
          </div>
          
          <div v-else class="items-list">
            <div
              v-for="(item, index) in formData.expenseItems"
              :key="index"
              class="expense-item"
            >
              <el-row :gutter="12" align="middle">
                <el-col :span="6">
                  <el-input v-model="item.itemName" placeholder="项目名称" />
                </el-col>
                <el-col :span="4">
                  <el-input-number
                    v-model="item.amount"
                    :min="0"
                    :precision="2"
                    placeholder="金额"
                    style="width: 100%"
                    @change="updateTotalAmount"
                  />
                </el-col>
                <el-col :span="6">
                  <el-input v-model="item.description" placeholder="说明" />
                </el-col>
                <el-col :span="6">
                  <el-date-picker
                    v-model="item.expenseDate"
                    type="date"
                    placeholder="发生日期"
                    style="width: 100%"
                  />
                </el-col>
                <el-col :span="2">
                  <el-button
                    type="danger"
                    size="small"
                    @click="removeExpenseItem(index)"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-col>
              </el-row>
            </div>
          </div>
        </div>
      </el-form-item>

      <!-- 附件上传 -->
      <el-form-item label="上传凭证">
        <FileUpload
          v-model="formData.attachments"
          :multiple="true"
          :accept="'.jpg,.jpeg,.png,.pdf,.doc,.docx'"
          :max-size="10"
          @upload-success="handleUploadSuccess"
          @upload-error="handleUploadError"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="info" @click="saveDraft" :loading="saving">保存草稿</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitting">
          {{ isEdit ? '更新申请' : '提交申请' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import expenseApi from '@/api/expense'
import FileUpload from '@/components/FileUpload.vue'

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
const saving = ref(false)
const submitting = ref(false)

// 表单数据
const formData = reactive({
  title: '',
  expenseType: '',
  totalAmount: 0,
  expenseDate: '',
  description: '',
  expenseItems: [],
  attachments: []
})

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入申请标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  expenseType: [
    { required: true, message: '请选择报销类型', trigger: 'change' }
  ],
  totalAmount: [
    { required: true, message: '请输入申请金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '申请金额必须大于0', trigger: 'blur' }
  ],
  expenseDate: [
    { required: true, message: '请选择发生日期', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入申请说明', trigger: 'blur' },
    { min: 10, max: 500, message: '说明长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 计算属性
const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => {
  return props.applicationData && props.applicationData.id
})

// 监听器
watch(() => props.applicationData, (newData) => {
  if (newData) {
    Object.assign(formData, {
      title: newData.title || '',
      expenseType: newData.expenseType || '',
      totalAmount: newData.totalAmount || 0,
      expenseDate: newData.expenseDate || '',
      description: newData.description || '',
      expenseItems: newData.expenseItems || [],
      attachments: newData.attachments || []
    })
  }
}, { immediate: true })

watch(() => props.modelValue, (visible) => {
  if (visible && !props.applicationData) {
    resetForm()
  }
})

// 方法
const resetForm = () => {
  Object.assign(formData, {
    title: '',
    expenseType: '',
    totalAmount: 0,
    expenseDate: '',
    description: '',
    expenseItems: [],
    attachments: []
  })
  nextTick(() => {
    formRef.value?.clearValidate()
  })
}

const addExpenseItem = () => {
  formData.expenseItems.push({
    itemName: '',
    amount: 0,
    description: '',
    expenseDate: ''
  })
}

const removeExpenseItem = (index) => {
  formData.expenseItems.splice(index, 1)
  updateTotalAmount()
}

const updateTotalAmount = () => {
  const total = formData.expenseItems.reduce((sum, item) => {
    return sum + (item.amount || 0)
  }, 0)
  formData.totalAmount = total
}

const handleUploadSuccess = (files) => {
  formData.attachments = [...formData.attachments, ...files]
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  ElMessage.error('文件上传失败: ' + error.message)
}

const saveDraft = async () => {
  try {
    saving.value = true
    const submitData = {
      ...formData,
      status: 'DRAFT'
    }
    
    if (isEdit.value) {
      await expenseApi.updateApplication(props.applicationData.id, submitData)
      ElMessage.success('草稿保存成功')
    } else {
      await expenseApi.createApplication(submitData)
      ElMessage.success('草稿创建成功')
    }
    
    emit('success')
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

const submitForm = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return
    
    if (formData.expenseItems.length === 0) {
      ElMessage.warning('请至少添加一个报销明细')
      return
    }
    
    submitting.value = true
    const submitData = {
      ...formData,
      status: 'PENDING'
    }
    
    if (isEdit.value) {
      await expenseApi.updateApplication(props.applicationData.id, submitData)
      ElMessage.success('申请更新成功')
    } else {
      await expenseApi.createApplication(submitData)
      ElMessage.success('申请提交成功')
    }
    
    emit('success')
  } catch (error) {
    ElMessage.error('提交失败')
  } finally {
    submitting.value = false
  }
}

const handleClose = () => {
  dialogVisible.value = false
}
</script>

<style scoped>
.expense-form {
  padding: 20px 0;
}

.expense-items {
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 16px;
}

.items-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: 600;
}

.empty-items {
  text-align: center;
  padding: 20px;
}

.items-list {
  max-height: 300px;
  overflow-y: auto;
}

.expense-item {
  padding: 12px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 12px;
  background: #fafafa;
}

.expense-item:last-child {
  margin-bottom: 0;
}

.dialog-footer {
  text-align: right;
}

.dialog-footer .el-button {
  margin-left: 12px;
}
</style>