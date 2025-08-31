<template>
  <div class="task-statistics-page">
    <div class="page-header">
      <h1>任务统计</h1>
      <p>查看您的任务完成情况和工作效率分析</p>
    </div>

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

    <!-- 图表区域 -->
    <div class="charts-container">
      <div class="chart-card">
        <div class="chart-header">
          <h3>任务趋势</h3>
        </div>
        <div class="chart-content">
          <div ref="trendChartRef" style="width: 100%; height: 300px;"></div>
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

const trendChartRef = ref(null)

// 加载任务统计
const loadTaskStats = async () => {
  try {
    const response = await taskApi.getTaskStats()
    if (response.data) {
      taskStats.value = response.data
    }
  } catch (error) {
    console.error('加载任务统计失败:', error)
    ElMessage.error('加载任务统计失败')
  }
}

// 初始化
onMounted(async () => {
  loading.value = true
  try {
    await loadTaskStats()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.task-statistics-page {
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

.charts-container {
  display: grid;
  gap: 24px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.chart-header {
  margin-bottom: 16px;
}

.chart-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.chart-content {
  min-height: 300px;
}
</style>
