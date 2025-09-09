<template>
  <div class="task-management">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">
          <el-icon class="title-icon"><Operation /></el-icon>
          {{ $t('nav.tasks') }}
        </h1>
        <p class="page-subtitle">{{ $t('taskManagement.subtitle') }}</p>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateTaskDialog = true">
          <el-icon><Plus /></el-icon>
          {{ $t('taskManagement.createTask') }}
        </el-button>
        <el-button @click="exportTasks">
          <el-icon><Download /></el-icon>
          {{ $t('taskManagement.exportTasks') }}
        </el-button>
      </div>
    </div>

    <div class="content-container">
      <!-- 任务统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card">
          <div class="stat-icon pending">
            <el-icon><Clock /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.pending }}</div>
            <div class="stat-label">{{ $t('taskManagement.pendingTasks') }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon processing">
            <el-icon><Loading /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.processing }}</div>
            <div class="stat-label">{{ $t('taskManagement.processingTasks') }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon completed">
            <el-icon><Check /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.completed }}</div>
            <div class="stat-label">{{ $t('taskManagement.completedTasks') }}</div>
          </div>
        </div>
        <div class="stat-card">
          <div class="stat-icon overdue">
            <el-icon><Warning /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-number">{{ taskStats.overdue }}</div>
            <div class="stat-label">{{ $t('taskManagement.overdueTasks') }}</div>
          </div>
        </div>
      </div>

      <!-- 筛选和搜索 -->
      <div class="filter-bar">
        <div class="filter-left">
          <el-input
            v-model="searchQuery"
            :placeholder="$t('taskManagement.searchPlaceholder')"
            style="width: 300px"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-select v-model="filterStatus" :placeholder="$t('taskManagement.taskStatus')" style="width: 150px" clearable>
            <el-option :label="$t('taskManagement.pending')" value="pending" />
            <el-option :label="$t('taskManagement.processing')" value="processing" />
            <el-option :label="$t('taskManagement.completed')" value="completed" />
            <el-option :label="$t('taskManagement.overdue')" value="overdue" />
          </el-select>
          <el-select v-model="filterPriority" :placeholder="$t('taskManagement.priority')" style="width: 150px" clearable>
            <el-option :label="$t('taskManagement.urgent')" value="urgent" />
            <el-option :label="$t('taskManagement.high')" value="high" />
            <el-option :label="$t('taskManagement.medium')" value="medium" />
            <el-option :label="$t('taskManagement.low')" value="low" />
          </el-select>
        </div>
        <div class="filter-right">
          <el-button @click="resetFilters">{{ $t('taskManagement.resetFilters') }}</el-button>
        </div>
      </div>

      <!-- 任务列表 -->
      <div class="task-list">
        <el-table :data="filteredTasks" v-loading="loading" style="width: 100%">
          <el-table-column type="selection" width="55" />
          <el-table-column prop="title" label="任务标题" min-width="200">
            <template #default="scope">
              <div class="task-title-cell">
                <span class="task-title">{{ scope.row.title }}</span>
                <el-tag v-if="scope.row.isUrgent" type="danger" size="small">紧急</el-tag>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="priority" label="优先级" width="100">
            <template #default="scope">
              <el-tag :type="getPriorityType(scope.row.priority)" size="small">
                {{ getPriorityText(scope.row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="assignee" label="负责人" width="120">
            <template #default="scope">
              <div class="assignee-cell">
                <el-avatar :size="24" :src="scope.row.assigneeAvatar">
                  {{ scope.row.assignee?.charAt(0) }}
                </el-avatar>
                <span>{{ scope.row.assignee }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)" size="small">
                {{ getStatusText(scope.row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="dueDate" label="截止时间" width="120" />
          <el-table-column prop="progress" label="进度" width="150">
            <template #default="scope">
              <div class="progress-cell">
                <el-progress :percentage="scope.row.progress" :stroke-width="8" />
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="editTask(scope.row)">编辑</el-button>
              <el-button type="success" size="small" @click="openCommentDialog(scope.row)">批注</el-button>
              <el-dropdown>
                <el-button type="info" size="small">
                  更多<el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item @click="duplicateTask(scope.row)">复制任务</el-dropdown-item>
                    <el-dropdown-item @click="assignTask(scope.row)">重新分配</el-dropdown-item>
                    <el-dropdown-item divided @click="deleteTask(scope.row)">删除任务</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 创建/编辑任务对话框 -->
    <TaskFormDialog 
      v-model="showCreateTaskDialog" 
      :task="currentTask"
      @save="handleTaskSave"
    />

    <!-- 批注对话框 -->
    <CommentDialog 
      v-model="showCommentDialog" 
      :task="currentTask"
      @save="handleCommentSave"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Operation, Plus, Download, Clock, Loading, Check, Warning, 
  Search, ArrowDown 
} from '@element-plus/icons-vue'
import TaskFormDialog from './components/TaskFormDialog.vue'
import CommentDialog from './components/CommentDialog.vue'

const { t: $t } = useI18n()

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const filterPriority = ref('')
const showCreateTaskDialog = ref(false)
const showCommentDialog = ref(false)
const currentTask = ref(null)

// 任务统计数据
const taskStats = ref({
  pending: 12,
  processing: 8,
  completed: 45,
  overdue: 3
})

// 模拟任务数据
const tasks = ref([
  {
    id: 1,
    title: '系统性能优化',
    priority: 'high',
    assignee: '张三',
    assigneeAvatar: '',
    status: 'processing',
    dueDate: '2025-09-15',
    progress: 75,
    isUrgent: true,
    comments: []
  },
  {
    id: 2,
    title: '用户界面重构',
    priority: 'medium',
    assignee: '李四',
    assigneeAvatar: '',
    status: 'pending',
    dueDate: '2025-09-20',
    progress: 0,
    isUrgent: false,
    comments: []
  }
])

// 计算属性
const filteredTasks = computed(() => {
  let result = tasks.value

  if (searchQuery.value) {
    result = result.filter(task => 
      task.title.includes(searchQuery.value) || 
      task.assignee.includes(searchQuery.value)
    )
  }

  if (filterStatus.value) {
    result = result.filter(task => task.status === filterStatus.value)
  }

  if (filterPriority.value) {
    result = result.filter(task => task.priority === filterPriority.value)
  }

  return result
})

// 方法
const getPriorityType = (priority) => {
  const types = { urgent: 'danger', high: 'warning', medium: 'info', low: 'success' }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = { urgent: '紧急', high: '高', medium: '中', low: '低' }
  return texts[priority] || '中'
}

const getStatusType = (status) => {
  const types = { pending: 'info', processing: 'warning', completed: 'success', overdue: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = { pending: '待处理', processing: '进行中', completed: '已完成', overdue: '已逾期' }
  return texts[status] || '待处理'
}

const resetFilters = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  filterPriority.value = ''
}

const editTask = (task) => {
  currentTask.value = { ...task }
  showCreateTaskDialog.value = true
}

const openCommentDialog = (task) => {
  currentTask.value = task
  showCommentDialog.value = true
}

const handleTaskSave = (taskData) => {
  // 处理任务保存逻辑
  ElMessage.success('任务保存成功！')
}

const handleCommentSave = (commentData) => {
  // 处理评论保存逻辑
  ElMessage.success('评论添加成功！')
}

const duplicateTask = (task) => {
  // 复制任务逻辑
  ElMessage.success('任务复制成功！')
}

const assignTask = (task) => {
  // 重新分配任务逻辑
  ElMessage.success('任务分配成功！')
}

const deleteTask = (task) => {
  ElMessageBox.confirm('确定要删除这个任务吗？', '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    ElMessage.success('任务删除成功！')
  })
}

const exportTasks = () => {
  // 导出任务逻辑
  ElMessage.success('任务导出成功！')
}

onMounted(() => {
  // 初始化数据
})
</script>

<style scoped>
.task-management {
  padding: 24px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.header-content .page-title {
  display: flex;
  align-items: center;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.title-icon {
  margin-right: 12px;
  color: #1890ff;
}

.page-subtitle {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 24px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 20px;
  color: white;
}

.stat-icon.pending { background: linear-gradient(135deg, #1890ff, #40a9ff); }
.stat-icon.processing { background: linear-gradient(135deg, #faad14, #ffc53d); }
.stat-icon.completed { background: linear-gradient(135deg, #52c41a, #73d13d); }
.stat-icon.overdue { background: linear-gradient(135deg, #ff4d4f, #ff7875); }

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1a1a1a;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-top: 4px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.filter-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.content-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.task-list {
  padding: 20px;
}

.task-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.assignee-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-cell {
  padding: 0 8px;
}
</style>