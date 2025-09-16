<template>
  <div class="expense-create">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Plus /></el-icon>
          创建报销申请
        </h1>
        <p class="page-subtitle">填写报销申请信息</p>
      </div>
      <div class="header-actions">
        <el-button @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <el-form
        ref="expenseFormRef"
        :model="expenseForm"
        :rules="expenseRules"
        label-width="120px"
        class="expense-form"
      >
        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>基本信息</span>
            </div>
          </template>
          
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="申请标题" prop="title">
                <el-input
                  v-model="expenseForm.title"
                  placeholder="请输入申请标题"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="报销类型" prop="type">
                <el-select v-model="expenseForm.type" placeholder="请选择报销类型" style="width: 100%">
                  <el-option label="差旅费" value="TRAVEL" />
                  <el-option label="餐费" value="MEAL" />
                  <el-option label="交通费" value="TRANSPORT" />
                  <el-option label="办公用品" value="OFFICE_SUPPLIES" />
                  <el-option label="培训费" value="TRAINING" />
                  <el-option label="其他" value="OTHER" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="申请事由" prop="reason">
            <el-input
              v-model="expenseForm.reason"
              type="textarea"
              :rows="4"
              placeholder="请详细说明报销事由"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-card>

        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>报销明细</span>
              <el-button type="primary" size="small" @click="addExpenseItem">
                <el-icon><Plus /></el-icon>
                添加明细
              </el-button>
            </div>
          </template>

          <div class="expense-items">
            <div v-for="(item, index) in expenseForm.items" :key="index" class="expense-item">
              <el-row :gutter="20" align="middle">
                <el-col :span="6">
                  <el-form-item :prop="`items.${index}.description`" :rules="itemRules.description">
                    <el-input
                      v-model="item.description"
                      placeholder="费用描述"
                      maxlength="100"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item :prop="`items.${index}.amount`" :rules="itemRules.amount">
                    <el-input-number
                      v-model="item.amount"
                      :min="0.01"
                      :max="999999.99"
                      :precision="2"
                      placeholder="金额"
                      style="width: 100%"
                      @change="calculateTotal"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <el-form-item :prop="`items.${index}.date`" :rules="itemRules.date">
                    <el-date-picker
                      v-model="item.date"
                      type="date"
                      placeholder="发生日期"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item :prop="`items.${index}.category`" :rules="itemRules.category">
                    <el-select v-model="item.category" placeholder="费用类别" style="width: 100%">
                      <el-option label="交通费" value="TRANSPORT" />
                      <el-option label="住宿费" value="ACCOMMODATION" />
                      <el-option label="餐费" value="MEAL" />
                      <el-option label="材料费" value="MATERIAL" />
                      <el-option label="其他" value="OTHER" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="4">
                  <div class="item-actions">
                    <el-button
                      type="danger"
                      size="small"
                      :disabled="expenseForm.items.length <= 1"
                      @click="removeExpenseItem(index)"
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </div>
                </el-col>
              </el-row>
            </div>
          </div>

          <div class="total-amount">
            <span class="total-label">总金额：</span>
            <span class="total-value">¥{{ totalAmount.toFixed(2) }}</span>
          </div>
        </el-card>

        <el-card class="form-card">
          <template #header>
            <div class="card-header">
              <span>附件上传</span>
            </div>
          </template>

          <el-upload
            ref="uploadRef"
            :file-list="fileList"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :before-upload="beforeUpload"
            :auto-upload="false"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><upload-filled /></el-icon>
            <div class="el-upload__text">
              将文件拖到此处，或<em>点击上传</em>
            </div>
            <template #tip>
              <div class="el-upload__tip">
                支持 jpg/png/pdf 文件，且不超过 10MB
              </div>
            </template>
          </el-upload>
        </el-card>

        <div class="form-actions">
          <el-button @click="goBack">取消</el-button>
          <el-button @click="saveDraft" :loading="saving">保存草稿</el-button>
          <el-button type="primary" @click="submitApplication" :loading="submitting">
            提交申请
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  ArrowLeft,
  Delete,
  UploadFilled
} from '@element-plus/icons-vue'
import expenseApi from '@/api/expense'

const router = useRouter()
const expenseFormRef = ref()
const uploadRef = ref()
const saving = ref(false)
const submitting = ref(false)
const fileList = ref([])

// 表单数据
const expenseForm = reactive({
  title: '',
  type: '',
  reason: '',
  items: [
    {
      description: '',
      amount: null,
      date: '',
      category: ''
    }
  ]
})

// 表单验证规则
const expenseRules = {
  title: [
    { required: true, message: '请输入申请标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择报销类型', trigger: 'change' }
  ],
  reason: [
    { required: true, message: '请输入申请事由', trigger: 'blur' },
    { min: 10, max: 500, message: '事由长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 明细项验证规则
const itemRules = {
  description: [
    { required: true, message: '请输入费用描述', trigger: 'blur' }
  ],
  amount: [
    { required: true, message: '请输入金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' }
  ],
  date: [
    { required: true, message: '请选择发生日期', trigger: 'change' }
  ],
  category: [
    { required: true, message: '请选择费用类别', trigger: 'change' }
  ]
}

// 计算总金额
const totalAmount = computed(() => {
  return expenseForm.items.reduce((total, item) => {
    return total + (item.amount || 0)
  }, 0)
})

// 添加明细项
const addExpenseItem = () => {
  expenseForm.items.push({
    description: '',
    amount: null,
    date: '',
    category: ''
  })
}

// 删除明细项
const removeExpenseItem = (index) => {
  if (expenseForm.items.length > 1) {
    expenseForm.items.splice(index, 1)
    calculateTotal()
  }
}

// 计算总金额
const calculateTotal = () => {
  // 触发响应式更新
}

// 文件上传相关
const handleFileChange = (file, fileList) => {
  // 处理文件变化
}

const handleFileRemove = (file, fileList) => {
  // 处理文件删除
}

const beforeUpload = (file) => {
  const isValidType = ['image/jpeg', 'image/png', 'application/pdf'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('只能上传 JPG/PNG/PDF 格式的文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 返回列表
const goBack = () => {
  router.push('/expense')
}

// 保存草稿
const saveDraft = async () => {
  if (!expenseFormRef.value) return
  
  try {
    await expenseFormRef.value.validate()
    
    saving.value = true
    
    const formData = {
      ...expenseForm,
      totalAmount: totalAmount.value,
      status: 'DRAFT'
    }
    
    await expenseApi.createApplication(formData)
    
    ElMessage.success('草稿保存成功')
    router.push('/expense')
    
  } catch (error) {
    console.error('保存草稿失败:', error)
    ElMessage.error(error.message || '保存草稿失败，请重试')
  } finally {
    saving.value = false
  }
}

// 提交申请
const submitApplication = async () => {
  if (!expenseFormRef.value) return
  
  try {
    await expenseFormRef.value.validate()
    
    if (totalAmount.value <= 0) {
      ElMessage.error('报销总金额必须大于0')
      return
    }
    
    await ElMessageBox.confirm(
      '确定要提交这个报销申请吗？提交后将无法修改。',
      '确认提交',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    submitting.value = true
    
    const formData = {
      ...expenseForm,
      totalAmount: totalAmount.value,
      status: 'PENDING'
    }
    
    await expenseApi.createApplication(formData)
    
    ElMessage.success('申请提交成功')
    router.push('/expense')
    
  } catch (error) {
    if (error !== 'cancel') {
      console.error('提交申请失败:', error)
      ElMessage.error(error.message || '提交申请失败，请重试')
    }
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 组件挂载时的初始化逻辑
})
</script>

<style scoped>
.expense-create {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-content {
  flex: 1;
}

.page-title {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
}

.title-icon {
  margin-right: 8px;
  color: #409eff;
}

.page-subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.content-container {
  max-width: 1200px;
  margin: 0 auto;
}

.expense-form {
  width: 100%;
}

.form-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.expense-items {
  margin-bottom: 20px;
}

.expense-item {
  margin-bottom: 15px;
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 6px;
  border: 1px solid #e9ecef;
}

.item-actions {
  display: flex;
  justify-content: center;
}

.total-amount {
  text-align: right;
  padding: 15px 0;
  border-top: 2px solid #e9ecef;
}

.total-label {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.total-value {
  font-size: 20px;
  font-weight: 700;
  color: #f56c6c;
  margin-left: 10px;
}

.form-actions {
  text-align: center;
  padding: 30px 0;
}

.form-actions .el-button {
  margin: 0 10px;
  min-width: 100px;
}

:deep(.el-upload-dragger) {
  width: 100%;
}

:deep(.el-form-item__error) {
  position: static;
  margin-top: 5px;
}
</style>