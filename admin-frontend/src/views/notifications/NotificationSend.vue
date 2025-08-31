<template>
  <div class="notification-send">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h1>发送通知</h1>
    </div>

    <div class="send-content">
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
                <span>通知内容</span>
              </template>
              
              <el-form-item label="通知标题" prop="title">
                <el-input v-model="form.title" placeholder="请输入通知标题" />
              </el-form-item>
              
              <el-form-item label="通知内容" prop="content">
                <el-input
                  v-model="form.content"
                  type="textarea"
                  :rows="6"
                  placeholder="请输入通知内容"
                />
              </el-form-item>
              
              <el-form-item label="通知类型" prop="notificationType">
                <el-select v-model="form.notificationType" placeholder="请选择通知类型">
                  <el-option label="系统通知" value="system_notice" />
                  <el-option label="任务分配" value="task_assigned" />
                  <el-option label="任务更新" value="task_updated" />
                  <el-option label="紧急通知" value="urgent_notice" />
                  <el-option label="公告通知" value="announcement" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="优先级" prop="priority">
                <el-radio-group v-model="form.priority">
                  <el-radio label="normal">普通</el-radio>
                  <el-radio label="urgent">紧急</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="相关任务" v-if="form.notificationType.startsWith('task_')">
                <el-select
                  v-model="form.relatedId"
                  placeholder="请选择相关任务"
                  filterable
                  remote
                  :remote-method="searchTasks"
                  :loading="taskLoading"
                  clearable
                >
                  <el-option
                    v-for="task in taskOptions"
                    :key="task.id"
                    :label="task.title"
                    :value="task.id"
                  />
                </el-select>
              </el-form-item>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="form-card">
              <template #header>
                <span>接收人设置</span>
              </template>
              
              <el-form-item label="发送方式" prop="sendType">
                <el-radio-group v-model="form.sendType" @change="handleSendTypeChange">
                  <el-radio label="all">全体用户</el-radio>
                  <el-radio label="department">按部门</el-radio>
                  <el-radio label="users">指定用户</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="选择部门" v-if="form.sendType === 'department'" prop="departmentIds">
                <el-select v-model="form.departmentIds" multiple placeholder="请选择部门">
                  <el-option
                    v-for="dept in departmentOptions"
                    :key="dept.id"
                    :label="dept.name"
                    :value="dept.id"
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="选择用户" v-if="form.sendType === 'users'" prop="userIds">
                <el-select
                  v-model="form.userIds"
                  multiple
                  placeholder="请选择用户"
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
              
              <el-form-item label="预计接收人数">
                <el-tag type="info">{{ estimatedRecipients }} 人</el-tag>
              </el-form-item>
            </el-card>
            
            <el-card class="form-card" style="margin-top: 20px;">
              <template #header>
                <span>发送设置</span>
              </template>
              
              <el-form-item label="立即发送">
                <el-switch v-model="form.sendImmediately" />
              </el-form-item>
              
              <el-form-item label="定时发送" v-if="!form.sendImmediately">
                <el-date-picker
                  v-model="form.scheduledTime"
                  type="datetime"
                  placeholder="选择发送时间"
                  style="width: 100%"
                />
              </el-form-item>
              
              <el-form-item label="邮件通知">
                <el-switch v-model="form.sendEmail" />
                <div class="form-item-tip">同时发送邮件通知</div>
              </el-form-item>
              
              <el-form-item label="短信通知">
                <el-switch v-model="form.sendSms" />
                <div class="form-item-tip">发送短信通知（仅紧急通知）</div>
              </el-form-item>
            </el-card>
            
            <el-card class="form-card" style="margin-top: 20px;">
              <template #header>
                <span>操作</span>
              </template>
              
              <div class="action-buttons">
                <el-button type="primary" @click="sendNotification" :loading="sending">
                  {{ form.sendImmediately ? '立即发送' : '定时发送' }}
                </el-button>
                <el-button @click="previewNotification">预览</el-button>
                <el-button @click="saveDraft">保存草稿</el-button>
                <el-button @click="resetForm">重置</el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-form>
    </div>

    <!-- 预览对话框 -->
    <el-dialog v-model="showPreview" title="通知预览" width="600px">
      <div class="notification-preview">
        <div class="preview-header">
          <h3>{{ form.title }}</h3>
          <el-tag :type="form.priority === 'urgent' ? 'danger' : 'info'">
            {{ form.priority === 'urgent' ? '紧急' : '普通' }}
          </el-tag>
        </div>
        <div class="preview-content">
          {{ form.content }}
        </div>
        <div class="preview-meta">
          <p><strong>通知类型：</strong>{{ getNotificationTypeText(form.notificationType) }}</p>
          <p><strong>预计接收人数：</strong>{{ estimatedRecipients }} 人</p>
          <p><strong>发送时间：</strong>{{ form.sendImmediately ? '立即发送' : formatTime(form.scheduledTime) }}</p>
        </div>
      </div>
      <template #footer>
        <el-button @click="showPreview = false">关闭</el-button>
        <el-button type="primary" @click="sendNotification" :loading="sending">确认发送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import * as notificationApi from '@/api/notificationApi'

const router = useRouter()

// 响应式数据
const loading = ref(false)
const sending = ref(false)
const taskLoading = ref(false)
const userLoading = ref(false)
const showPreview = ref(false)
const formRef = ref(null)

const taskOptions = ref([])
const userOptions = ref([])
const departmentOptions = ref([
  { id: 1, name: '技术部' },
  { id: 2, name: '产品部' },
  { id: 3, name: '运营部' },
  { id: 4, name: '人事部' }
])

// 表单数据
const form = reactive({
  title: '',
  content: '',
  notificationType: 'system_notice',
  priority: 'normal',
  relatedId: null,
  sendType: 'all',
  departmentIds: [],
  userIds: [],
  sendImmediately: true,
  scheduledTime: null,
  sendEmail: false,
  sendSms: false
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入通知标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入通知内容', trigger: 'blur' }
  ],
  notificationType: [
    { required: true, message: '请选择通知类型', trigger: 'change' }
  ],
  sendType: [
    { required: true, message: '请选择发送方式', trigger: 'change' }
  ]
}

// 计算属性
const estimatedRecipients = computed(() => {
  switch (form.sendType) {
    case 'all':
      return 100 // 模拟全体用户数量
    case 'department':
      return form.departmentIds.length * 20 // 模拟每个部门20人
    case 'users':
      return form.userIds.length
    default:
      return 0
  }
})

// 搜索任务
const searchTasks = async (query) => {
  if (!query) {
    taskOptions.value = []
    return
  }
  
  taskLoading.value = true
  try {
    // TODO: 实现任务搜索API
    // const response = await taskApi.searchTasks(query)
    // taskOptions.value = response.data
    
    // 模拟数据
    taskOptions.value = [
      { id: 1, title: '系统优化任务' },
      { id: 2, title: '界面重构任务' }
    ].filter(task => task.title.includes(query))
  } catch (error) {
    console.error('搜索任务失败:', error)
  } finally {
    taskLoading.value = false
  }
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

// 处理发送方式变化
const handleSendTypeChange = (value) => {
  if (value !== 'department') {
    form.departmentIds = []
  }
  if (value !== 'users') {
    form.userIds = []
  }
}

// 发送通知
const sendNotification = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    sending.value = true
    
    let response
    if (form.sendType === 'all') {
      response = await notificationApi.broadcastSystemNotification(form)
    } else if (form.sendType === 'department') {
      // 按部门发送
      for (const deptId of form.departmentIds) {
        await notificationApi.sendNotificationToDepartment(deptId, form)
      }
      response = { code: 200 }
    } else {
      response = await notificationApi.batchSendNotifications({
        ...form,
        recipientIds: form.userIds
      })
    }
    
    if (response.code === 200) {
      ElMessage.success('通知发送成功')
      router.push('/admin/notifications')
    }
  } catch (error) {
    if (error !== false) { // 不是表单验证错误
      console.error('发送通知失败:', error)
      ElMessage.error('发送通知失败')
    }
  } finally {
    sending.value = false
    showPreview.value = false
  }
}

// 预览通知
const previewNotification = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    showPreview.value = true
  } catch (error) {
    // 表单验证失败
  }
}

// 保存草稿
const saveDraft = () => {
  // TODO: 实现保存草稿功能
  ElMessage.success('草稿保存成功')
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

// 工具方法
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getNotificationTypeText = (type) => {
  const texts = {
    'system_notice': '系统通知',
    'task_assigned': '任务分配',
    'task_updated': '任务更新',
    'urgent_notice': '紧急通知',
    'announcement': '公告通知'
  }
  return texts[type] || '未知类型'
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.notification-send {
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

.notification-preview {
  padding: 20px;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.preview-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.preview-content {
  margin-bottom: 20px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
}

.preview-meta p {
  margin: 8px 0;
  color: #909399;
  font-size: 14px;
}
</style>
