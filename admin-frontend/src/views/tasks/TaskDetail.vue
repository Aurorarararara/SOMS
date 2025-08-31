<template>
  <div class="task-detail">
    <div class="page-header">
      <el-button @click="goBack" :icon="ArrowLeft">返回</el-button>
      <h1>任务详情</h1>
      <div class="header-actions">
        <el-button type="primary" @click="editTask" v-if="canEdit">编辑</el-button>
        <el-button type="success" @click="assignTask" v-if="canAssign">分配</el-button>
        <el-button type="danger" @click="deleteTask" v-if="canDelete">删除</el-button>
      </div>
    </div>

    <div class="detail-content" v-loading="loading">
      <div v-if="task" class="task-info">
        <el-row :gutter="20">
          <el-col :span="16">
            <el-card class="info-card">
              <template #header>
                <div class="card-header">
                  <span>基本信息</span>
                  <div class="task-status">
                    <el-tag :type="getStatusType(task.status)">
                      {{ getStatusText(task.status) }}
                    </el-tag>
                    <el-tag v-if="task.isUrgent" type="danger">紧急</el-tag>
                  </div>
                </div>
              </template>
              
              <div class="task-basic-info">
                <h2 class="task-title">{{ task.title }}</h2>
                <div class="task-description">{{ task.description }}</div>
                
                <div class="task-meta">
                  <div class="meta-item">
                    <span class="meta-label">优先级：</span>
                    <el-tag :type="getPriorityType(task.priority)">
                      {{ getPriorityText(task.priority) }}
                    </el-tag>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">创建人：</span>
                    <span>{{ task.creatorName }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">负责人：</span>
                    <span>{{ task.assigneeName || '未分配' }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">开始时间：</span>
                    <span>{{ formatDate(task.startDate) }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">截止时间：</span>
                    <span>{{ formatDate(task.dueDate) }}</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">预计工时：</span>
                    <span>{{ task.estimatedHours || 0 }}小时</span>
                  </div>
                  <div class="meta-item">
                    <span class="meta-label">实际工时：</span>
                    <span>{{ task.actualHours || 0 }}小时</span>
                  </div>
                </div>
                
                <div class="task-progress" v-if="task.progress !== null">
                  <span class="meta-label">完成进度：</span>
                  <el-progress :percentage="task.progress" :stroke-width="8" />
                </div>
                
                <div class="task-tags" v-if="task.tags && task.tags.length">
                  <span class="meta-label">标签：</span>
                  <el-tag v-for="tag in task.tags" :key="tag" size="small" style="margin-right: 8px;">
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </el-card>
            
            <!-- 任务评论 -->
            <el-card class="info-card">
              <template #header>
                <span>任务评论</span>
              </template>
              <div class="comments-section">
                <!-- TODO: 实现评论组件 -->
                <p>评论功能开发中...</p>
              </div>
            </el-card>
          </el-col>
          
          <el-col :span="8">
            <el-card class="info-card">
              <template #header>
                <span>操作历史</span>
              </template>
              <div class="task-history">
                <!-- TODO: 实现操作历史 -->
                <p>操作历史功能开发中...</p>
              </div>
            </el-card>
            
            <el-card class="info-card" style="margin-top: 20px;">
              <template #header>
                <span>快速操作</span>
              </template>
              <div class="quick-actions">
                <el-button type="primary" @click="updateProgress" style="width: 100%; margin-bottom: 10px;">
                  更新进度
                </el-button>
                <el-button type="success" @click="completeTask" style="width: 100%; margin-bottom: 10px;">
                  标记完成
                </el-button>
                <el-button type="warning" @click="duplicateTask" style="width: 100%;">
                  复制任务
                </el-button>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>
      
      <div v-else class="empty-state">
        <el-icon :size="64"><Document /></el-icon>
        <p>任务不存在或已被删除</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Document } from '@element-plus/icons-vue'
import * as taskApi from '@/api/taskApi'

const route = useRoute()
const router = useRouter()

// 响应式数据
const loading = ref(false)
const task = ref(null)

// 计算属性
const canEdit = computed(() => {
  // TODO: 根据用户权限判断
  return true
})

const canAssign = computed(() => {
  // TODO: 根据用户权限判断
  return true
})

const canDelete = computed(() => {
  // TODO: 根据用户权限判断
  return true
})

// 加载任务详情
const loadTaskDetail = async () => {
  loading.value = true
  try {
    const response = await taskApi.getTaskDetail(route.params.id)
    if (response.code === 200) {
      task.value = response.data
    }
  } catch (error) {
    console.error('加载任务详情失败:', error)
    ElMessage.error('加载任务详情失败')
  } finally {
    loading.value = false
  }
}

// 编辑任务
const editTask = () => {
  router.push(`/admin/tasks/${route.params.id}/edit`)
}

// 分配任务
const assignTask = () => {
  // TODO: 实现任务分配对话框
  ElMessage.info('任务分配功能开发中')
}

// 删除任务
const deleteTask = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这个任务吗？', '确认删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    })
    
    const response = await taskApi.deleteTask(route.params.id)
    if (response.code === 200) {
      ElMessage.success('任务删除成功')
      router.push('/admin/tasks')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败:', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 更新进度
const updateProgress = () => {
  // TODO: 实现进度更新对话框
  ElMessage.info('进度更新功能开发中')
}

// 完成任务
const completeTask = async () => {
  try {
    const response = await taskApi.completeTask(route.params.id)
    if (response.code === 200) {
      ElMessage.success('任务已标记为完成')
      await loadTaskDetail()
    }
  } catch (error) {
    console.error('完成任务失败:', error)
    ElMessage.error('完成任务失败')
  }
}

// 复制任务
const duplicateTask = async () => {
  try {
    const response = await taskApi.duplicateTask(route.params.id)
    if (response.code === 200) {
      ElMessage.success('任务复制成功')
      router.push(`/admin/tasks/${response.data.id}`)
    }
  } catch (error) {
    console.error('复制任务失败:', error)
    ElMessage.error('复制任务失败')
  }
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 工具方法
const formatDate = (dateString) => {
  if (!dateString) return '未设置'
  const date = new Date(dateString)
  return date.toLocaleDateString('zh-CN')
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

onMounted(() => {
  loadTaskDetail()
})
</script>

<style scoped>
.task-detail {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-status {
  display: flex;
  gap: 8px;
}

.task-title {
  margin: 0 0 16px 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.task-description {
  margin-bottom: 20px;
  line-height: 1.6;
  color: #606266;
}

.task-meta {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
}

.meta-label {
  font-weight: 500;
  color: #909399;
  margin-right: 8px;
  min-width: 80px;
}

.task-progress {
  margin-bottom: 20px;
}

.task-tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-state p {
  margin-top: 16px;
  font-size: 16px;
}
</style>
