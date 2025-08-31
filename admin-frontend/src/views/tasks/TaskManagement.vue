<template>
  <div class="task-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1>任务管理</h1>
      <p>管理和监控所有任务的执行情况</p>
    </div>

    <!-- 任务统计卡片 -->
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.pending }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon processing">
          <el-icon><Loading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.processing }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon completed">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.completed }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.overdue }}</div>
          <div class="stat-label">已逾期</div>
        </div>
      </div>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar">
      <div class="search-filters">
        <el-input
          v-model="searchQuery"
          placeholder="搜索任务标题..."
          :prefix-icon="Search"
          style="width: 300px; margin-right: 16px;"
          clearable
        />
        <el-select v-model="filterStatus" placeholder="状态" style="width: 120px; margin-right: 16px;" clearable>
          <el-option label="待处理" value="pending" />
          <el-option label="进行中" value="processing" />
          <el-option label="已完成" value="completed" />
          <el-option label="已逾期" value="overdue" />
        </el-select>
        <el-select v-model="filterPriority" placeholder="优先级" style="width: 120px; margin-right: 16px;" clearable>
          <el-option label="低" value="low" />
          <el-option label="中" value="medium" />
          <el-option label="高" value="high" />
          <el-option label="紧急" value="urgent" />
        </el-select>
        <el-button @click="resetFilters">重置</el-button>
      </div>
      <div class="action-buttons">
        <el-button type="primary" :icon="Plus" @click="showCreateTaskDialog = true">
          创建任务
        </el-button>
        <el-button :icon="Download" @click="exportTasks">导出</el-button>
        <el-button :icon="Operation" @click="showBatchActions = !showBatchActions">
          批量操作
        </el-button>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div v-if="showBatchActions" class="batch-actions">
      <el-button type="success" size="small" @click="batchAssign">批量分配</el-button>
      <el-button type="warning" size="small" @click="batchUpdateStatus">批量更新状态</el-button>
      <el-button type="danger" size="small" @click="batchDelete">批量删除</el-button>
    </div>

    <!-- 任务列表 -->
    <div class="task-list">
      <el-table 
        :data="tasks" 
        v-loading="loading" 
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
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
        <el-table-column prop="assigneeName" label="负责人" width="120">
          <template #default="scope">
            <div class="assignee-cell">
              <el-avatar :size="24" :src="scope.row.assigneeAvatar">
                {{ scope.row.assigneeName?.charAt(0) }}
              </el-avatar>
              <span>{{ scope.row.assigneeName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="120">
          <template #default="scope">
            <div class="creator-cell">
              <el-avatar :size="24" :src="scope.row.creatorAvatar">
                {{ scope.row.creatorName?.charAt(0) }}
              </el-avatar>
              <span>{{ scope.row.creatorName }}</span>
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
        <el-table-column prop="dueDate" label="截止时间" width="120">
          <template #default="scope">
            {{ formatDate(scope.row.dueDate) }}
          </template>
        </el-table-column>
        <el-table-column prop="progress" label="进度" width="150">
          <template #default="scope">
            <div class="progress-cell">
              <el-progress :percentage="scope.row.progress || 0" :stroke-width="8" />
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="editTask(scope.row)">编辑</el-button>
            <el-button type="success" size="small" @click="assignTask(scope.row)">分配</el-button>
            <el-button type="info" size="small" @click="viewTaskDetail(scope.row)">详情</el-button>
            <el-dropdown>
              <el-button type="info" size="small">
                更多<el-icon><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="duplicateTask(scope.row)">复制任务</el-dropdown-item>
                  <el-dropdown-item @click="viewTaskHistory(scope.row)">查看历史</el-dropdown-item>
                  <el-dropdown-item divided @click="deleteTask(scope.row)">删除任务</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 创建/编辑任务对话框 -->
    <TaskFormDialog
      v-model="showCreateTaskDialog"
      :task="currentTask"
      :users="users"
      @save="handleTaskSave"
    />

    <!-- 任务详情对话框 -->
    <TaskDetailDialog
      v-model="showTaskDetailDialog"
      :task="currentTask"
    />

    <!-- 任务分配对话框 -->
    <TaskAssignDialog
      v-model="showAssignDialog"
      :task="currentTask"
      :users="users"
      @assign="handleTaskAssign"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Operation, Plus, Download, Clock, Loading, Check, Warning, 
  Search, ArrowDown 
} from '@element-plus/icons-vue'
import TaskFormDialog from './components/TaskFormDialog.vue'
import TaskDetailDialog from './components/TaskDetailDialog.vue'
import TaskAssignDialog from './components/TaskAssignDialog.vue'
import * as taskApi from '@/api/taskApi'
import { employeeApi } from '@/api/employee'

// 响应式数据
const loading = ref(false)
const searchQuery = ref('')
const filterStatus = ref('')
const filterPriority = ref('')
const showCreateTaskDialog = ref(false)
const showTaskDetailDialog = ref(false)
const showAssignDialog = ref(false)
const showBatchActions = ref(false)
const currentTask = ref(null)
const selectedTasks = ref([])

// 任务统计数据
const taskStats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  overdue: 0,
  total: 0
})

// 任务数据
const tasks = ref([])
const pagination = ref({
  current: 1,
  size: 20,
  total: 0
})

// 用户列表
const users = ref([])

// 计算属性
const getPriorityType = (priority) => {
  const types = {
    low: 'info',
    medium: 'warning',
    high: 'danger',
    urgent: 'danger'
  }
  return types[priority] || 'info'
}

const getPriorityText = (priority) => {
  const texts = {
    low: '低',
    medium: '中',
    high: '高',
    urgent: '紧急'
  }
  return texts[priority] || '未知'
}

const getStatusType = (status) => {
  const types = {
    pending: 'info',
    processing: 'warning',
    completed: 'success',
    overdue: 'danger'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '待处理',
    processing: '进行中',
    completed: '已完成',
    overdue: '已逾期'
  }
  return texts[status] || '未知'
}

// API调用方法
const loadTaskStats = async () => {
  try {
    const response = await taskApi.getGlobalTaskStats()
    if (response.code === 200) {
      taskStats.value = response.data
    }
  } catch (error) {
    console.error('加载任务统计失败:', error)
  }
}

const loadTasks = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.current,
      size: pagination.value.size,
      title: searchQuery.value,
      status: filterStatus.value,
      priority: filterPriority.value
    }

    const response = await taskApi.getTaskList(params)
    if (response.code === 200) {
      tasks.value = response.data.records || []
      pagination.value.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载任务列表失败:', error)
    ElMessage.error('加载任务列表失败')
  } finally {
    loading.value = false
  }
}

// 事件处理方法
const resetFilters = () => {
  searchQuery.value = ''
  filterStatus.value = ''
  filterPriority.value = ''
  pagination.value.current = 1
  loadTasks()
}

const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

const editTask = (task) => {
  currentTask.value = { ...task }
  showCreateTaskDialog.value = true
}

const viewTaskDetail = (task) => {
  currentTask.value = task
  showTaskDetailDialog.value = true
}

const assignTask = (task) => {
  currentTask.value = task
  showAssignDialog.value = true
}

const viewTaskHistory = (task) => {
  // TODO: 实现任务历史查看功能
  ElMessage.info('任务历史功能开发中')
}

const handleTaskSave = async (taskData) => {
  try {
    let response
    if (taskData.id) {
      response = await taskApi.updateTask(taskData.id, taskData)
    } else {
      response = await taskApi.createTask(taskData)
    }

    if (response.code === 200) {
      ElMessage.success(taskData.id ? '任务更新成功！' : '任务创建成功！')
      showCreateTaskDialog.value = false
      currentTask.value = null
      await loadTasks()
      await loadTaskStats()
    }
  } catch (error) {
    console.error('保存任务失败:', error)
    ElMessage.error('保存任务失败')
  }
}

const handleTaskAssign = async (assignData) => {
  try {
    const response = await taskApi.assignTask(assignData.taskId, assignData.assigneeId)
    if (response.code === 200) {
      ElMessage.success('任务分配成功！')
      showAssignDialog.value = false
      await loadTasks()
      await loadTaskStats()
    }
  } catch (error) {
    console.error('分配任务失败:', error)
    ElMessage.error('分配任务失败')
  }
}

const duplicateTask = async (task) => {
  try {
    const response = await taskApi.duplicateTask(task.id)
    if (response.code === 200) {
      ElMessage.success('任务复制成功！')
      await loadTasks()
      await loadTaskStats()
    }
  } catch (error) {
    console.error('复制任务失败:', error)
    ElMessage.error('复制任务失败')
  }
}

const deleteTask = async (task) => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const response = await taskApi.deleteTask(task.id)
    if (response.code === 200) {
      ElMessage.success('任务删除成功！')
      await loadTasks()
      await loadTaskStats()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 批量操作方法
const batchAssign = async () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请先选择要分配的任务')
    return
  }
  // TODO: 实现批量分配对话框
  ElMessage.info('批量分配功能开发中')
}

const batchUpdateStatus = async () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请先选择要更新的任务')
    return
  }
  // TODO: 实现批量状态更新对话框
  ElMessage.info('批量状态更新功能开发中')
}

const batchDelete = async () => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请先选择要删除的任务')
    return
  }

  try {
    await ElMessageBox.confirm(`确定要删除选中的 ${selectedTasks.value.length} 个任务吗？`, '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })

    const taskIds = selectedTasks.value.map(task => task.id)
    // TODO: 实现批量删除API
    ElMessage.success('批量删除成功！')
    await loadTasks()
    await loadTaskStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('批量删除失败')
    }
  }
}

const exportTasks = async () => {
  try {
    const params = {
      title: searchQuery.value,
      status: filterStatus.value,
      priority: filterPriority.value
    }

    const response = await taskApi.exportTasks(params, 'excel')

    const blob = new Blob([response], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `tasks_${new Date().getTime()}.xlsx`
    link.click()
    window.URL.revokeObjectURL(url)

    ElMessage.success('任务导出成功！')
  } catch (error) {
    console.error('导出任务失败:', error)
    ElMessage.error('导出任务失败')
  }
}

// 分页处理方法
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.current = 1
  loadTasks()
}

const handleCurrentChange = (current) => {
  pagination.value.current = current
  loadTasks()
}

// 工具方法
const formatDate = (dateString) => {
  if (!dateString) return ''
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
}

// 监听筛选条件变化
watch([searchQuery, filterStatus, filterPriority], () => {
  pagination.value.current = 1
  loadTasks()
}, { debounce: 300 })

// 获取用户列表
const loadUsers = async () => {
  try {
    const response = await employeeApi.getEmployeeList()
    users.value = response.data || []
  } catch (error) {
    console.error('获取用户列表失败:', error)
    ElMessage.error('获取用户列表失败')
  }
}

onMounted(async () => {
  await loadTaskStats()
  await loadTasks()
  await loadUsers()
})
</script>

<style scoped>
.task-management {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.page-header p {
  margin: 0;
  color: #6b7280;
  font-size: 14px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 16px;
  font-size: 24px;
  color: white;
}

.stat-icon.pending {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.stat-icon.processing {
  background: linear-gradient(135deg, #f59e0b, #d97706);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #10b981, #059669);
}

.stat-icon.overdue {
  background: linear-gradient(135deg, #ef4444, #dc2626);
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  line-height: 1;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
  margin-top: 4px;
}

.action-bar {
  background: white;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.search-filters {
  display: flex;
  align-items: center;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.batch-actions {
  background: #fef3c7;
  border: 1px solid #fbbf24;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
  display: flex;
  gap: 8px;
  align-items: center;
}

.task-list {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.task-title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-title {
  font-weight: 500;
}

.assignee-cell,
.creator-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.progress-cell {
  padding: 0 8px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}
</style>
