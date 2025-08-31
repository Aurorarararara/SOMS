<template>
  <div class="task-create">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h1>创建任务</h1>
    </div>

    <div class="create-content">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="120px"
        v-loading="loading"
      >
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card class="form-card">
              <template #header>
                <span>基本信息</span>
              </template>
              
              <el-form-item label="任务标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入任务标题" />
              </el-form-item>
              
              <el-form-item label="任务描述" prop="description">
                <el-input
                  v-model="form.description"
                  type="textarea"
                  :rows="4"
                  placeholder="请输入任务描述"
                />
              </el-form-item>
              
              <el-form-item label="优先级" prop="priority">
                <el-select v-model="form.priority" placeholder="请选择优先级">
                  <el-option label="低" value="low" />
                  <el-option label="中" value="medium" />
                  <el-option label="高" value="high" />
                  <el-option label="紧急" value="urgent" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="负责人" prop="assigneeId">
                <el-select
                  v-model="form.assigneeId"
                  placeholder="请选择负责人"
                  filterable
                  remote
                  :remote-method="searchUsers"
                  :loading="userLoading"
                >
                  <el-option
                    v-for="user in userOptions"
                    :key="user.id"
                    :label="user.name"
                    :value="user.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="开始时间" prop="startDate">
                    <el-date-picker
                      v-model="form.startDate"
                      type="date"
                      placeholder="选择开始时间"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="截止时间" prop="dueDate">
                    <el-date-picker
                      v-model="form.dueDate"
                      type="date"
                      placeholder="选择截止时间"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-form-item label="预计工时" prop="estimatedHours">
                <el-input-number
                  v-model="form.estimatedHours"
                  :min="0"
                  :max="999"
                  placeholder="预计工时（小时）"
                />
              </el-form-item>
              
              <el-form-item label="标签">
                <el-select
                  v-model="form.tags"
                  multiple
                  filterable
                  allow-create
                  placeholder="请选择或输入标签"
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
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="form-card">
              <template #header>
                <span>任务设置</span>
              </template>
              
              <el-form-item label="紧急任务">
                <el-switch v-model="form.isUrgent" />
                <div class="form-item-tip">标记为紧急任务将优先显示</div>
              </el-form-item>
              
              <el-form-item label="允许重新分配">
                <el-switch v-model="form.allowReassign" />
                <div class="form-item-tip">允许其他人重新分配此任务</div>
              </el-form-item>
              
              <el-form-item label="更新时通知">
                <el-switch v-model="form.notifyOnUpdate" />
                <div class="form-item-tip">任务状态变更时发送通知</div>
              </el-form-item>
            </el-card>
            
            <el-card class="form-card" style="margin-top: 20px;">
              <template #header>
                <span>操作</span>
              </template>
              
              <div class="action-buttons">
                <el-button type="primary" @click="submitForm" :loading="submitting">
                  创建任务
                </el-button>
                <el-button @click="resetForm">重置</el-button>
                <el-button @click="goBack">取消</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import * as taskApi from '@/api/taskApi'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const submitting = ref(false)
const userLoading = ref(false)
const formRef = ref(null)
const userOptions = ref([])
const tagOptions = ref(['前端开发', '后端开发', '测试', '设计', '文档', '优化', '修复'])

// 表单数据
const form = reactive({
  title: '',
  description: '',
  priority: 'medium',
  assigneeId: null,
  startDate: null,
  dueDate: null,
  estimatedHours: null,
  tags: [],
  isUrgent: false,
  allowReassign: true,
  notifyOnUpdate: true
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入任务描述', trigger: 'blur' }
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' }
  ],
  assigneeId: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  dueDate: [
    { required: true, message: '请选择截止时间', trigger: 'change' }
  ]
}

// 搜索用户
const searchUsers = async (query) => {
  if (!query) {
    userOptions.value = []
    return
  }
  
  userLoading.value = true
  try {
    // TODO: 实现用户搜索API
    // const response = await userApi.searchUsers(query)
    // userOptions.value = response.data
    
    // 模拟数据
    userOptions.value = [
      { id: 1, name: '张三' },
      { id: 2, name: '李四' },
      { id: 3, name: '王五' }
    ].filter(user => user.name.includes(query))
  } catch (error) {
    console.error('搜索用户失败:', error)
  } finally {
    userLoading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    submitting.value = true
    const response = await taskApi.createTask(form)
    
    if (response.code === 200) {
      ElMessage.success('任务创建成功')
      router.push('/admin/tasks')
    }
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('创建任务失败:', error)
      ElMessage.error('创建任务失败')
    }
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.task-create {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.form-card {
  margin-bottom: 20px;
}

.form-item-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.action-buttons .el-button {
  width: 100%;
}
</style>
