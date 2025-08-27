<template>
  <div class="my-tasks-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <h1 class="page-title">我的待办任务</h1>
      <p class="page-description">处理分配给您的审批任务</p>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card pending-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><Clock /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ taskStats.pending }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card overdue-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ taskStats.overdue }}</div>
              <div class="stat-label">已超时</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ taskStats.today }}</div>
              <div class="stat-label">今日到期</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card completed-card">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon size="32"><CircleCheck /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ taskStats.completed }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务列表 -->
    <el-card class="task-list-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">待办任务列表</span>
          <div class="header-actions">
            <el-button 
              type="text" 
              icon="Refresh" 
              @click="loadMyTasks"
              :loading="loading"
            >
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <div class="task-filters">
        <el-row :gutter="16">
          <el-col :span="6">
            <el-select v-model="filters.status" placeholder="任务状态" clearable>
              <el-option label="待处理" value="PENDING" />
              <el-option label="已完成" value="COMPLETED" />
              <el-option label="已超时" value="OVERDUE" />
            </el-select>
          </el-col>
          <el-col :span="6">
            <el-select v-model="filters.priority" placeholder="紧急程度" clearable>
              <el-option label="紧急" value="HIGH" />
              <el-option label="普通" value="NORMAL" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-col>
          <el-col :span="8">
            <el-input 
              v-model="filters.keyword" 
              placeholder="搜索任务标题或内容"
              prefix-icon="Search"
              clearable
            />
          </el-col>
          <el-col :span="4">
            <el-button type="primary" icon="Search" @click="handleSearch">
              搜索
            </el-button>
          </el-col>
        </el-row>
      </div>

      <div class="task-list" v-loading="loading">
        <div 
          v-for="task in filteredTasks" 
          :key="task.id"
          class="task-item"
          :class="{ 
            'overdue': isOverdue(task.dueTime),
            'urgent': isUrgent(task.dueTime)
          }"
        >
          <div class="task-content">
            <div class="task-header">
              <div class="task-title">
                <h3>{{ task.title }}</h3>
                <div class="task-badges">
                  <el-tag v-if="isOverdue(task.dueTime)" type="danger" size="small">
                    已超时
                  </el-tag>
                  <el-tag v-else-if="isUrgent(task.dueTime)" type="warning" size="small">
                    即将到期
                  </el-tag>
                  <el-tag :type="getBusinessTypeTag(task.businessType)" size="small">
                    {{ getBusinessTypeLabel(task.businessType) }}
                  </el-tag>
                </div>
              </div>
              <div class="task-meta">
                <span class="task-time">
                  <el-icon><Clock /></el-icon>
                  {{ formatTime(task.createTime) }}
                </span>
                <span v-if="task.dueTime" class="task-due">
                  截止: {{ formatTime(task.dueTime) }}
                </span>
              </div>
            </div>

            <div class="task-body">
              <div class="task-description">
                {{ task.content || '暂无描述' }}
              </div>
              
              <div class="task-info">
                <div class="info-item">
                  <span class="info-label">申请人:</span>
                  <span class="info-value">{{ task.initiatorName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">流程:</span>
                  <span class="info-value">{{ task.templateName }}</span>
                </div>
                <div class="info-item">
                  <span class="info-label">当前节点:</span>
                  <span class="info-value">{{ task.nodeName }}</span>
                </div>
              </div>
            </div>

            <div class="task-actions">
              <el-button type="primary" size="small" @click="handleTask(task)">
                处理
              </el-button>
              <el-button type="info" size="small" @click="viewDetail(task)">
                详情
              </el-button>
              <el-button type="text" size="small" @click="viewHistory(task)">
                历史
              </el-button>
            </div>
          </div>
        </div>

        <div v-if="filteredTasks.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无待办任务" />
        </div>
      </div>
    </el-card>

    <!-- 任务处理对话框 -->
    <el-dialog
      v-model="showTaskDialog"
      :title="'处理任务: ' + currentTask?.title"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="currentTask" class="task-detail">
        <div class="detail-section">
          <h4>申请信息</h4>
          <div class="detail-content">
            <p><strong>申请人:</strong> {{ currentTask.initiatorName }}</p>
            <p><strong>申请时间:</strong> {{ formatTime(currentTask.createTime) }}</p>
            <p><strong>申请内容:</strong></p>
            <div class="content-box">
              {{ currentTask.content || '暂无内容' }}
            </div>
          </div>
        </div>

        <div class="detail-section">
          <h4>审批操作</h4>
          <el-form ref="taskFormRef" :model="taskForm" :rules="taskFormRules" label-width="80px">
            <el-form-item label="审批结果" prop="action">
              <el-radio-group v-model="taskForm.action">
                <el-radio label="APPROVED">
                  <el-icon color="#67C23A"><CircleCheck /></el-icon>
                  同意
                </el-radio>
                <el-radio label="REJECTED">
                  <el-icon color="#F56C6C"><CircleClose /></el-icon>
                  拒绝
                </el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="审批意见" prop="comment">
              <el-input
                v-model="taskForm.comment"
                type="textarea"
                :rows="4"
                placeholder="请输入审批意见"
                maxlength="200"
                show-word-limit
              />
            </el-form-item>
          </el-form>
        </div>
      </div>

      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showTaskDialog = false">取消</el-button>
          <el-button 
            type="primary" 
            :loading="processing" 
            @click="submitTaskDecision"
          >
            提交审批
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="任务详情"
      width="800px"
    >
      <div v-if="taskDetail" class="task-detail-content">
        <!-- 任务详情内容 -->
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务标题">{{ taskDetail.title }}</el-descriptions-item>
          <el-descriptions-item label="申请人">{{ taskDetail.initiatorName }}</el-descriptions-item>
          <el-descriptions-item label="流程模板">{{ taskDetail.templateName }}</el-descriptions-item>
          <el-descriptions-item label="当前节点">{{ taskDetail.nodeName }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(taskDetail.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="到期时间">{{ taskDetail.dueTime ? formatTime(taskDetail.dueTime) : '无限制' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="detail-section">
          <h4>申请内容</h4>
          <div class="content-box">
            {{ taskDetail.content || '暂无内容' }}
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 流程历史对话框 -->
    <el-dialog
      v-model="showHistoryDialog"
      title="流程历史"
      width="800px"
    >
      <div class="history-content">
        <el-timeline>
          <el-timeline-item
            v-for="(item, index) in workflowHistory"
            :key="index"
            :timestamp="formatTime(item.createTime)"
            placement="top"
            :color="getHistoryColor(item.action)"
          >
            <el-card>
              <div class="history-item">
                <div class="history-header">
                  <span class="history-action">{{ item.actionName }}</span>
                  <span class="history-operator">{{ item.operatorName || '系统' }}</span>
                </div>
                <div v-if="item.comment" class="history-comment">
                  {{ item.comment }}
                </div>
              </div>
            </el-card>
          </el-timeline-item>
        </el-timeline>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Clock, Warning, Calendar, CircleCheck, CircleClose, 
  Refresh, Search 
} from '@element-plus/icons-vue'
import { getMyPendingTasks, processTask, getInstanceHistory } from '@/api/workflow'
import { useUserStore } from '@/stores/user'

// 响应式数据
const loading = ref(false)
const processing = ref(false)
const tasks = ref([])
const userStore = useUserStore()
const taskStats = reactive({
  pending: 0,
  overdue: 0,
  today: 0,
  completed: 0
})

const filters = reactive({
  status: '',
  priority: '',
  keyword: ''
})

const showTaskDialog = ref(false)
const showDetailDialog = ref(false)
const showHistoryDialog = ref(false)
const currentTask = ref(null)
const taskDetail = ref(null)
const workflowHistory = ref([])

const taskFormRef = ref(null)
const taskForm = reactive({
  action: '',
  comment: ''
})

const taskFormRules = {
  action: [
    { required: true, message: '请选择审批结果', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入审批意见', trigger: 'blur' },
    { min: 5, max: 200, message: '审批意见长度在5-200个字符', trigger: 'blur' }
  ]
}

// 计算属性
const filteredTasks = computed(() => {
  let result = [...tasks.value]
  
  if (filters.status) {
    if (filters.status === 'OVERDUE') {
      result = result.filter(task => isOverdue(task.dueTime))
    } else {
      result = result.filter(task => task.status === filters.status)
    }
  }
  
  if (filters.keyword) {
    const keyword = filters.keyword.toLowerCase()
    result = result.filter(task => 
      task.title.toLowerCase().includes(keyword) ||
      (task.content && task.content.toLowerCase().includes(keyword))
    )
  }
  
  return result
})

// 加载我的待办任务
const loadMyTasks = async () => {
  loading.value = true
  try {
    const response = await getMyPendingTasks()
    if (response.success) {
      tasks.value = response.data || []
      updateTaskStats()
    }
  } catch (error) {
    console.error('加载待办任务失败:', error)
    ElMessage.error('加载待办任务失败')
  } finally {
    loading.value = false
  }
}

// 更新任务统计
const updateTaskStats = () => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000)
  
  taskStats.pending = tasks.value.filter(task => task.status === 'PENDING').length
  taskStats.overdue = tasks.value.filter(task => isOverdue(task.dueTime)).length
  taskStats.today = tasks.value.filter(task => {
    if (!task.dueTime) return false
    const dueDate = new Date(task.dueTime)
    return dueDate >= today && dueDate < tomorrow
  }).length
  taskStats.completed = tasks.value.filter(task => task.status === 'COMPLETED').length
}

// 处理任务
const handleTask = (task) => {
  currentTask.value = task
  taskForm.action = ''
  taskForm.comment = ''
  showTaskDialog.value = true
}

// 查看详情
const viewDetail = (task) => {
  taskDetail.value = task
  showDetailDialog.value = true
}

// 查看历史
const viewHistory = async (task) => {
  try {
    const response = await getInstanceHistory(task.instanceId)
    if (response.success) {
      workflowHistory.value = response.data || []
      showHistoryDialog.value = true
    }
  } catch (error) {
    console.error('获取流程历史失败:', error)
    ElMessage.error('获取流程历史失败')
  }
}

// 提交审批决定
const submitTaskDecision = async () => {
  try {
    await taskFormRef.value.validate()
    
    processing.value = true
    
    const response = await processTask(currentTask.value.id, {
      userId: userStore.userInfo.id,
      action: taskForm.action,
      comment: taskForm.comment
    })
    
    if (response.success) {
      ElMessage.success('审批任务处理成功')
      showTaskDialog.value = false
      await loadMyTasks() // 刷新列表
    }
  } catch (error) {
    console.error('处理审批任务失败:', error)
    ElMessage.error('处理审批任务失败')
  } finally {
    processing.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  // 触发计算属性重新计算
}

// 工具方法
const isOverdue = (dueTime) => {
  if (!dueTime) return false
  return new Date(dueTime) < new Date()
}

const isUrgent = (dueTime) => {
  if (!dueTime) return false
  const now = new Date()
  const due = new Date(dueTime)
  const diffHours = (due - now) / (1000 * 60 * 60)
  return diffHours > 0 && diffHours <= 24
}

const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

const getBusinessTypeTag = (type) => {
  const tagMap = {
    'LEAVE': '',
    'EXPENSE': 'success',
    'PURCHASE': 'warning',
    'OTHER': 'info'
  }
  return tagMap[type] || 'info'
}

const getBusinessTypeLabel = (type) => {
  const labelMap = {
    'LEAVE': '请假申请',
    'EXPENSE': '费用报销',
    'PURCHASE': '采购申请',
    'OTHER': '其他'
  }
  return labelMap[type] || type
}

const getHistoryColor = (action) => {
  const colorMap = {
    'START': '#409EFF',
    'APPROVED': '#67C23A',
    'REJECTED': '#F56C6C',
    'WITHDRAW': '#E6A23C',
    'COMPLETE': '#67C23A'
  }
  return colorMap[action] || '#909399'
}

// 组件挂载
onMounted(() => {
  loadMyTasks()
})
</script>

<style scoped>
.my-tasks-container {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: calc(100vh - 140px);
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.page-description {
  color: #6b7280;
  margin: 0;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 12px;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 8px 0;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  color: white;
}

.pending-card .stat-icon {
  background: linear-gradient(135deg, #409EFF 0%, #66B1FF 100%);
}

.overdue-card .stat-icon {
  background: linear-gradient(135deg, #F56C6C 0%, #F78989 100%);
}

.today-card .stat-icon {
  background: linear-gradient(135deg, #E6A23C 0%, #EEBE77 100%);
}

.completed-card .stat-icon {
  background: linear-gradient(135deg, #67C23A 0%, #85CE61 100%);
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.task-list-card {
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.task-filters {
  margin-bottom: 24px;
  padding: 16px;
  background-color: #f8f9fa;
  border-radius: 8px;
}

.task-list {
  min-height: 400px;
}

.task-item {
  background: white;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.task-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.task-item.overdue {
  border-left: 4px solid #f56c6c;
}

.task-item.urgent {
  border-left: 4px solid #e6a23c;
}

.task-content {
  padding: 20px;
}

.task-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.task-title h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin: 0 0 8px 0;
}

.task-badges {
  display: flex;
  gap: 8px;
}

.task-meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.task-time, .task-due {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #6b7280;
}

.task-body {
  margin-bottom: 16px;
}

.task-description {
  color: #4b5563;
  line-height: 1.6;
  margin-bottom: 12px;
}

.task-info {
  display: flex;
  gap: 24px;
  font-size: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.info-label {
  color: #6b7280;
}

.info-value {
  color: #1f2937;
  font-weight: 500;
}

.task-actions {
  display: flex;
  gap: 12px;
}

.task-detail {
  max-height: 500px;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 8px;
}

.detail-content p {
  margin-bottom: 8px;
  line-height: 1.6;
}

.content-box {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 16px;
  border: 1px solid #e5e7eb;
  line-height: 1.6;
  color: #4b5563;
}

.task-detail-content {
  max-height: 600px;
  overflow-y: auto;
}

.history-content {
  max-height: 500px;
  overflow-y: auto;
}

.history-item {
  padding: 8px 0;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.history-action {
  font-weight: 600;
  color: #1f2937;
}

.history-operator {
  color: #6b7280;
  font-size: 14px;
}

.history-comment {
  color: #4b5563;
  line-height: 1.6;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .my-tasks-container {
    padding: 16px;
  }
  
  .stats-row {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  
  .task-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .task-info {
    flex-direction: column;
    gap: 8px;
  }
  
  .task-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
}
</style>