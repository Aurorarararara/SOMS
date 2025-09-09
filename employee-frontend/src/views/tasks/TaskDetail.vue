<template>
  <div class="task-detail-container">
    <div class="page-header">
      <el-button @click="$router.go(-1)">
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
      <h2 class="page-title">任务详情</h2>
    </div>

    <el-card>
      <h3>{{ task.title }}</h3>
      <p>{{ task.description }}</p>
      
      <div class="task-meta">
        <el-tag :type="getPriorityType(task.priority)">
          {{ getPriorityText(task.priority) }}
        </el-tag>
        <el-tag :type="getStatusType(task.status)">
          {{ getStatusText(task.status) }}
        </el-tag>
      </div>
      
      <div class="progress-section" v-if="task.status === 'in_progress'">
        <label>进度:</label>
        <el-progress :percentage="task.progress" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'

const route = useRoute()
const task = ref({})

const getPriorityType = (priority) => {
  const typeMap = { low: 'info', medium: 'warning', high: 'danger', urgent: 'danger' }
  return typeMap[priority] || 'info'
}

const getPriorityText = (priority) => {
  const textMap = { low: '低', medium: '中', high: '高', urgent: '紧急' }
  return textMap[priority] || '未知'
}

const getStatusType = (status) => {
  const typeMap = { pending: 'warning', in_progress: 'primary', completed: 'success', cancelled: 'info' }
  return typeMap[status] || 'info'
}

const getStatusText = (status) => {
  const textMap = { pending: '待开始', in_progress: '进行中', completed: '已完成', cancelled: '已取消' }
  return textMap[status] || '未知'
}

onMounted(() => {
  // 模拟任务数据
  task.value = {
    id: route.params.id,
    title: '完成项目需求分析',
    description: '对新项目进行详细的需求分析和功能规划',
    status: 'in_progress',
    priority: 'high',
    progress: 75
  }
})
</script>

<style scoped>
.task-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.page-title {
  margin: 0;
  color: #333;
  font-weight: 600;
}

.task-meta {
  display: flex;
  gap: 8px;
  margin: 16px 0;
}

.progress-section {
  margin-top: 16px;
}

.progress-section label {
  display: block;
  margin-bottom: 8px;
  font-weight: 600;
}
</style>