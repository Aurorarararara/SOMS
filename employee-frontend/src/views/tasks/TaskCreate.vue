<template>
  <div class="task-create">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Plus /></el-icon>
          创建新任务
        </h1>
        <p class="page-subtitle">创建并分配新的工作任务</p>
      </div>
      <div class="header-actions">
        <el-button @click="$router.go(-1)">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <el-card class="task-form-card">
        <el-form 
          ref="taskFormRef" 
          :model="taskForm" 
          :rules="taskRules" 
          label-width="100px"
          size="large"
        >
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="任务标题" prop="title">
                <el-input 
                  v-model="taskForm.title" 
                  placeholder="请输入任务标题"
                  maxlength="100"
                  show-word-limit
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="优先级" prop="priority">
                <el-select v-model="taskForm.priority" placeholder="选择优先级" style="width: 100%">
                  <el-option label="紧急" value="urgent" />
                  <el-option label="高" value="high" />
                  <el-option label="中" value="medium" />
                  <el-option label="低" value="low" />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="负责人" prop="assignee">
                <el-select 
                  v-model="taskForm.assignee" 
                  placeholder="选择负责人" 
                  style="width: 100%"
                  filterable
                  remote
                  :remote-method="searchUsers"
                  :loading="loadingUsers"
                >
                  <el-option
                    v-for="user in users"
                    :key="user.id"
                    :label="user.name"
                    :value="user.id"
                  >
                    <div class="user-option">
                      <el-avatar :size="24" :src="user.avatar">
                        {{ user.name?.charAt(0) }}
                      </el-avatar>
                      <span>{{ user.name }}</span>
                      <span class="user-dept">{{ user.department }}</span>
                    </div>
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="截止时间" prop="dueDate">
                <el-date-picker
                  v-model="taskForm.dueDate"
                  type="datetime"
                  placeholder="选择截止时间"
                  style="width: 100%"
                  :disabled-date="disabledDate"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="任务描述" prop="description">
            <el-input
              v-model="taskForm.description"
              type="textarea"
              :rows="6"
              placeholder="请详细描述任务内容、要求和注意事项..."
              maxlength="500"
              show-word-limit
            />
          </el-form-item>

          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item label="任务标签">
                <el-select
                  v-model="taskForm.tags"
                  multiple
                  filterable
                  allow-create
                  placeholder="添加任务标签"
                  style="width: 100%"
                >
                  <el-option
                    v-for="tag in predefinedTags"
                    :key="tag"
                    :label="tag"
                    :value="tag"
                  />
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="预估工时">
                <el-input-number
                  v-model="taskForm.estimatedHours"
                  :min="0.5"
                  :max="100"
                  :step="0.5"
                  placeholder="预估完成工时"
                  style="width: 100%"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="附件">
            <el-upload
              v-model:file-list="taskForm.attachments"
              :action="uploadUrl"
              :headers="uploadHeaders"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              multiple
              :limit="5"
              drag
            >
              <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
              <div class="el-upload__text">
                将文件拖到此处，或<em>点击上传</em>
              </div>
              <template #tip>
                <div class="el-upload__tip">
                  支持jpg/png/gif/pdf/doc/docx/xls/xlsx格式，单个文件不超过10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>

          <el-form-item>
            <div class="form-actions">
              <el-button type="primary" size="large" @click="submitForm" :loading="submitting">
                <el-icon><Check /></el-icon>
                创建任务
              </el-button>
              <el-button size="large" @click="saveDraft" :loading="savingDraft">
                <el-icon><Document /></el-icon>
                保存草稿
              </el-button>
              <el-button size="large" @click="resetForm">
                <el-icon><Refresh /></el-icon>
                重置
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, ArrowLeft, Check, Document, Refresh, UploadFilled
} from '@element-plus/icons-vue'

const router = useRouter()

// 响应式数据
const taskFormRef = ref()
const submitting = ref(false)
const savingDraft = ref(false)
const loadingUsers = ref(false)
const users = ref([])

// 表单数据
const taskForm = reactive({
  title: '',
  description: '',
  priority: 'medium',
  assignee: '',
  dueDate: '',
  tags: [],
  estimatedHours: 8,
  attachments: []
})

// 表单验证规则
const taskRules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  assignee: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  dueDate: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入任务描述', trigger: 'blur' },
    { min: 10, max: 500, message: '描述长度在 10 到 500 个字符', trigger: 'blur' }
  ]
}

// 预定义标签
const predefinedTags = ref([
  '紧急', '重要', '开发', '测试', '设计', '文档', '会议', '培训'
])

// 上传配置
const uploadUrl = ref('/api/upload')
const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('token')}`
})

// 方法
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7
}

const searchUsers = async (query) => {
  if (query) {
    loadingUsers.value = true
    try {
      // 模拟API调用
      await new Promise(resolve => setTimeout(resolve, 300))
      users.value = [
        { id: 1, name: '张三', department: '开发部', avatar: '' },
        { id: 2, name: '李四', department: '测试部', avatar: '' },
        { id: 3, name: '王五', department: '产品部', avatar: '' },
        { id: 4, name: '赵六', department: '设计部', avatar: '' }
      ].filter(user => user.name.includes(query))
    } catch (error) {
      ElMessage.error('搜索用户失败')
    } finally {
      loadingUsers.value = false
    }
  }
}

const handleUploadSuccess = (response, file) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error, file) => {
  ElMessage.error('文件上传失败')
}

const submitForm = async () => {
  if (!taskFormRef.value) return
  
  try {
    await taskFormRef.value.validate()
    submitting.value = true
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('任务创建成功')
    router.push('/tasks')
  } catch (error) {
    if (error !== false) {
      ElMessage.error('创建任务失败')
    }
  } finally {
    submitting.value = false
  }
}

const saveDraft = async () => {
  savingDraft.value = true
  try {
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 800))
    ElMessage.success('草稿保存成功')
  } catch (error) {
    ElMessage.error('保存草稿失败')
  } finally {
    savingDraft.value = false
  }
}

const resetForm = () => {
  ElMessageBox.confirm('确认重置表单吗？', '确认重置', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    taskFormRef.value?.resetFields()
    taskForm.attachments = []
    ElMessage.success('表单已重置')
  }).catch(() => {
    // 用户取消
  })
}

// 生命周期
onMounted(() => {
  // 初始化用户列表
  searchUsers('')
})
</script>

<style scoped>
.task-create {
  padding: 24px;
  background: #f5f7fa;
  min-height: calc(100vh - 100px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.header-content {
  flex: 1;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

.title-icon {
  color: #1890ff;
  background: rgba(24, 144, 255, 0.1);
  padding: 8px;
  border-radius: 8px;
}

.page-subtitle {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.content-container {
  max-width: 1000px;
  margin: 0 auto;
}

.task-form-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

:deep(.el-card__body) {
  padding: 32px;
}

.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-dept {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.form-actions {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-top: 32px;
}

:deep(.el-upload-dragger) {
  width: 100%;
  height: 120px;
}

:deep(.el-form-item__label) {
  font-weight: 600;
  color: #333;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  font-family: inherit;
}

:deep(.el-button) {
  border-radius: 8px;
  font-weight: 500;
}

:deep(.el-date-editor.el-input) {
  border-radius: 8px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .task-create {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .header-actions {
    width: 100%;
    justify-content: flex-end;
  }
  
  :deep(.el-card__body) {
    padding: 20px;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>