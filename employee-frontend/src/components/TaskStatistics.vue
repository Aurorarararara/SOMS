<template>
  <div class="task-statistics" v-loading="loading">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.pending || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon processing">
          <el-icon><Loading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.processing || 0 }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon completed">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.completed || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ taskStats.overdue || 0 }}</div>
          <div class="stat-label">已逾期</div>
        </div>
      </div>
    </div>

    <!-- 工作量统计 -->
    <div class="workload-section">
      <div class="section-header">
        <h3>工作量统计</h3>
      </div>
      <div class="workload-stats">
        <div class="workload-item">
          <span class="label">总任务数：</span>
          <span class="value">{{ workloadStats.total_tasks || 0 }}</span>
        </div>
        <div class="workload-item">
          <span class="label">预计工时：</span>
          <span class="value">{{ workloadStats.total_estimated_hours || 0 }}h</span>
        </div>
        <div class="workload-item">
          <span class="label">实际工时：</span>
          <span class="value">{{ workloadStats.total_actual_hours || 0 }}h</span>
        </div>
        <div class="workload-item">
          <span class="label">完成率：</span>
          <span class="value">{{ getCompletionRate() }}%</span>
        </div>
      </div>
    </div>

    <!-- 优先级分布 -->
    <div class="priority-section">
      <div class="section-header">
        <h3>任务优先级分布</h3>
      </div>
      <div class="priority-stats">
        <div class="priority-item" v-for="item in priorityStats" :key="item.priority">
          <div class="priority-label" :class="item.priority">{{ item.label }}</div>
          <div class="priority-count">{{ item.count }}</div>
        </div>
      </div>
    </div>

    <!-- 效率分析 -->
    <div class="efficiency-section">
      <div class="section-header">
        <h3>效率分析</h3>
      </div>
      <div class="efficiency-stats">
        <div class="efficiency-item">
          <span class="label">任务完成率：</span>
          <span class="value">{{ efficiencyStats.completionRate || 0 }}%</span>
        </div>
        <div class="efficiency-item">
          <span class="label">平均完成时间：</span>
          <span class="value">{{ efficiencyStats.avgCompletionTime || 0 }} 天</span>
        </div>
        <div class="efficiency-item">
          <span class="label">按时完成率：</span>
          <span class="value">{{ efficiencyStats.onTimeRate || 0 }}%</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Loading, Check, Warning } from '@element-plus/icons-vue'
import * as taskApi from '@/api/taskApi'

// 响应式数据
const loading = ref(false)
const taskStats = ref({
  pending: 0,
  processing: 0,
  completed: 0,
  overdue: 0
})

const workloadStats = ref({
  total_tasks: 0,
  total_estimated_hours: 0,
  total_actual_hours: 0,
  completed_tasks: 0
})

const priorityStats = ref([
  { priority: 'low', label: '低优先级', count: 0 },
  { priority: 'normal', label: '普通', count: 0 },
  { priority: 'high', label: '高优先级', count: 0 },
  { priority: 'urgent', label: '紧急', count: 0 }
])

const efficiencyStats = ref({
  completionRate: 0,
  avgCompletionTime: 0,
  onTimeRate: 0
})

// 计算完成率
const getCompletionRate = () => {
  const total = workloadStats.value.total_tasks || 0
  const completed = workloadStats.value.completed_tasks || 0
  return total > 0 ? Math.round((completed / total) * 100) : 0
}

// 加载任务统计
const loadTaskStats = async () => {
  try {
    const response = await taskApi.getTaskStats()
    if (response.data) {
      taskStats.value = response.data
    }
  } catch (error) {
    console.error('加载任务统计失败:', error)
  }
}

// 加载工作量统计
const loadWorkloadStats = async () => {
  try {
    const response = await taskApi.getWorkloadStats()
    if (response.data) {
      workloadStats.value = response.data
    }
  } catch (error) {
    console.error('加载工作量统计失败:', error)
  }
}

// 加载优先级统计
const loadPriorityStats = async () => {
  try {
    const response = await taskApi.getPriorityDistributionStats()
    if (response.data && Array.isArray(response.data)) {
      const priorityMap = {
        'low': '低优先级',
        'normal': '普通', 
        'high': '高优先级',
        'urgent': '紧急'
      }
      
      priorityStats.value = response.data.map(item => ({
        priority: item.priority,
        label: priorityMap[item.priority] || item.priority,
        count: item.count || 0
      }))
    }
  } catch (error) {
    console.error('加载优先级统计失败:', error)
  }
}

// 加载效率统计
const loadEfficiencyStats = async () => {
  try {
    const response = await taskApi.getTaskEfficiencyStats()
    if (response.data) {
      efficiencyStats.value = response.data
    }
  } catch (error) {
    console.error('加载效率统计失败:', error)
  }
}

// 初始化
onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadTaskStats(),
      loadWorkloadStats(),
      loadPriorityStats(),
      loadEfficiencyStats()
    ])
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.task-statistics {
  padding: 24px;
  background-color: #f5f5f5;
  min-height: 100vh;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
}

.stat-icon.pending {
  background-color: #fef3c7;
  color: #d97706;
}

.stat-icon.processing {
  background-color: #dbeafe;
  color: #2563eb;
}

.stat-icon.completed {
  background-color: #d1fae5;
  color: #059669;
}

.stat-icon.overdue {
  background-color: #fee2e2;
  color: #dc2626;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.workload-section, .priority-section, .efficiency-section {
  background: white;
  border-radius: 8px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-header h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.workload-stats, .efficiency-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.workload-item, .efficiency-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background-color: #f9fafb;
  border-radius: 6px;
}

.label {
  font-size: 14px;
  color: #6b7280;
}

.value {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.priority-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.priority-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  background-color: #f9fafb;
}

.priority-label {
  font-size: 14px;
  font-weight: 500;
}

.priority-label.low {
  color: #059669;
}

.priority-label.normal {
  color: #2563eb;
}

.priority-label.high {
  color: #d97706;
}

.priority-label.urgent {
  color: #dc2626;
}

.priority-count {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}
</style>
