<template>
  <el-dialog 
    v-model="dialogVisible"
    :title="isEdit ? '编辑任务' : '新建任务'"
    width="650px"
    @close="handleClose"
  >
    <el-form 
      ref="formRef" 
      :model="formData" 
      :rules="formRules"
      label-width="100px"
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="任务标题" prop="title">
            <el-input 
              v-model="formData.title" 
              placeholder="请输入任务标题"
              maxlength="100"
              show-word-limit
            />
          </el-form-item>
        </el-col>
        
        <el-col :span="12">
          <el-form-item label="优先级" prop="priority">
            <el-select v-model="formData.priority" style="width: 100%">
              <el-option 
                v-for="item in priorityOptions" 
                :key="item.value"
                :label="item.label" 
                :value="item.value"
              >
                <span :style="{ color: item.color }">{{ item.label }}</span>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="任务分配" prop="assigneeId">
            <el-select 
              v-model="formData.assigneeId" 
              filterable
              remote
              placeholder="选择负责人"
              style="width: 100%"
              :remote-method="searchUsers"
              :loading="userLoading"
            >
              <el-option
                v-for="user in userOptions"
                :key="user.id"
                :label="user.name"
                :value="user.id"
              >
                <div class="user-option">
                  <el-avatar :size="24" :src="user.avatar">
                    {{ user.name.charAt(0) }}
                  </el-avatar>
                  <span>{{ user.name }} ({{ user.department }})</span>
                </div>
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="开始时间" prop="startDate">
            <el-date-picker
              v-model="formData.startDate"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>

        <el-col :span="12">
          <el-form-item label="截止时间" prop="dueDate">
            <el-date-picker
              v-model="formData.dueDate"
              type="datetime"
              placeholder="选择截止时间"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="任务标签">
            <el-select 
              v-model="formData.tags" 
              multiple
              filterable
              allow-create
              placeholder="选择或创建标签"
              style="width: 100%"
            >
              <el-option
                v-for="tag in tagOptions"
                :key="tag"
                :label="tag"
                :value="tag"
              />
            </el-select>
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="任务描述" prop="description">
            <el-input
              v-model="formData.description"
              type="textarea"
              :rows="4"
              placeholder="请输入任务描述"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="附件">
            <el-upload
              v-model:file-list="formData.attachments"
              :action="uploadUrl"
              :headers="uploadHeaders"
              multiple
              :before-upload="beforeUpload"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
            >
              <el-button type="primary">
                <el-icon><Upload /></el-icon>
                上传文件
              </el-button>
              <template #tip>
                <div class="upload-tip">
                  支持上传 doc、docx、pdf、图片等文件，单个文件不超过10MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
        </el-col>

        <el-col :span="24" v-if="isEdit">
          <el-form-item label="任务进度">
            <el-slider
              v-model="formData.progress"
              :step="5"
              show-stops
              :format-tooltip="formatProgress"
              style="margin: 0 12px"
            />
          </el-form-item>
        </el-col>

        <el-col :span="24">
          <el-form-item label="高级设置">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-checkbox v-model="formData.isUrgent">紧急任务</el-checkbox>
              </el-col>
              <el-col :span="8">
                <el-checkbox v-model="formData.allowReassign">允许重新分配</el-checkbox>
              </el-col>
              <el-col :span="8">
                <el-checkbox v-model="formData.notifyOnUpdate">更新时通知</el-checkbox>
              </el-col>
            </el-row>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSave" :loading="saving">
          {{ isEdit ? '更新任务' : '创建任务' }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Upload } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: Boolean,
  task: Object
})

const emit = defineEmits(['update:modelValue', 'save'])

// 响应式数据
const formRef = ref()
const saving = ref(false)
const userLoading = ref(false)

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => props.task && props.task.id)

// 表单数据
const formData = ref({
  title: '',
  priority: 'medium',
  assigneeId: '',
  startDate: '',
  dueDate: '',
  description: '',
  tags: [],
  attachments: [],
  progress: 0,
  isUrgent: false,
  allowReassign: true,
  notifyOnUpdate: true
})

// 优先级选项
const priorityOptions = [
  { label: '低', value: 'low', color: '#52c41a' },
  { label: '中', value: 'medium', color: '#1890ff' },
  { label: '高', value: 'high', color: '#faad14' },
  { label: '紧急', value: 'urgent', color: '#ff4d4f' }
]

// 用户选项
const userOptions = ref([
  { id: 1, name: '张三', department: '技术部', avatar: '' },
  { id: 2, name: '李四', department: '产品部', avatar: '' },
  { id: 3, name: '王五', department: '设计部', avatar: '' }
])

// 标签选项
const tagOptions = ref(['前端开发', '后端开发', '测试', '设计', '产品', '运维'])

// 表单验证规则
const formRules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  assigneeId: [
    { required: true, message: '请分配负责人', trigger: 'change' }
  ],
  dueDate: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 上传配置
const uploadUrl = ref('/api/upload')
const uploadHeaders = ref({
  Authorization: `Bearer ${localStorage.getItem('employee_token')}`
})

// 方法
const searchUsers = (query) => {
  if (query) {
    userLoading.value = true
    // 模拟搜索用户API调用
    setTimeout(() => {
      userLoading.value = false
    }, 500)
  }
}

const formatProgress = (value) => `${value}%`

const beforeUpload = (file) => {
  const isValidType = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'image/jpeg', 'image/png'].includes(file.type)
  const isLt10M = file.size / 1024 / 1024 < 10

  if (!isValidType) {
    ElMessage.error('只能上传 doc、docx、pdf、jpg、png 格式的文件!')
    return false
  }
  if (!isLt10M) {
    ElMessage.error('上传文件大小不能超过 10MB!')
    return false
  }
  return true
}

const handleUploadSuccess = (response, file) => {
  ElMessage.success('文件上传成功')
}

const handleUploadError = (error) => {
  ElMessage.error('文件上传失败')
}

const handleSave = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    saving.value = true
    
    // 模拟保存API调用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    emit('save', formData.value)
    handleClose()
  } catch (error) {
    console.error('表单验证失败:', error)
  } finally {
    saving.value = false
  }
}

const handleClose = () => {
  formRef.value?.resetFields()
  emit('update:modelValue', false)
}

// 监听task变化，初始化表单数据
watch(() => props.task, (newTask) => {
  if (newTask) {
    Object.assign(formData.value, {
      ...newTask,
      tags: newTask.tags || [],
      attachments: newTask.attachments || []
    })
  } else {
    // 重置表单
    Object.assign(formData.value, {
      title: '',
      priority: 'medium',
      assigneeId: '',
      startDate: '',
      dueDate: '',
      description: '',
      tags: [],
      attachments: [],
      progress: 0,
      isUrgent: false,
      allowReassign: true,
      notifyOnUpdate: true
    })
  }
}, { immediate: true })
</script>

<style scoped>
.user-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.upload-tip {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
}

.dialog-footer {
  text-align: right;
}
</style>