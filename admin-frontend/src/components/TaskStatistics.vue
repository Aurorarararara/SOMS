<template>
  <div class="task-statistics">
    <!-- 全局统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-icon total">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ globalStats.total_tasks || 0 }}</div>
          <div class="stat-label">总任务数</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon pending">
          <el-icon><Clock /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ globalStats.pending_tasks || 0 }}</div>
          <div class="stat-label">待处理</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon processing">
          <el-icon><Loading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ globalStats.processing_tasks || 0 }}</div>
          <div class="stat-label">进行中</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon completed">
          <el-icon><Check /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ globalStats.completed_tasks || 0 }}</div>
          <div class="stat-label">已完成</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon overdue">
          <el-icon><Warning /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ globalStats.overdue_tasks || 0 }}</div>
          <div class="stat-label">已逾期</div>
        </div>
      </div>
      
      <div class="stat-card">
        <div class="stat-icon users">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-number">{{ globalStats.active_users || 0 }}</div>
          <div class="stat-label">活跃用户</div>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="charts-container">
      <!-- 部门任务分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>部门任务分布</h3>
        </div>
        <div class="chart-content">
          <div ref="departmentChartRef" class="chart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 任务完成趋势 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>任务完成趋势</h3>
          <el-select v-model="trendDays" @change="loadTrendData" style="width: 120px;">
            <el-option label="7天" :value="7" />
            <el-option label="30天" :value="30" />
            <el-option label="90天" :value="90" />
          </el-select>
        </div>
        <div class="chart-content">
          <div ref="trendChartRef" class="chart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 用户工作量排行 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>用户工作量排行</h3>
        </div>
        <div class="chart-content">
          <div ref="workloadRankingChartRef" class="chart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 任务状态分布 -->
      <div class="chart-card">
        <div class="chart-header">
          <h3>任务状态分布</h3>
        </div>
        <div class="chart-content">
          <div ref="statusChartRef" class="chart" style="height: 300px;"></div>
        </div>
      </div>

      <!-- 系统负载分析 -->
      <div class="chart-card full-width">
        <div class="chart-header">
          <h3>系统负载分析</h3>
          <div class="chart-controls">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="loadSystemLoadData"
              style="width: 240px;"
            />
          </div>
        </div>
        <div class="chart-content">
          <div ref="systemLoadChartRef" class="chart" style="height: 400px;"></div>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="data-tables">
      <!-- 部门绩效表 -->
      <div class="table-card">
        <div class="table-header">
          <h3>部门绩效排行</h3>
        </div>
        <el-table :data="departmentPerformance" style="width: 100%">
          <el-table-column prop="department_name" label="部门名称" />
          <el-table-column prop="total_tasks" label="总任务数" />
          <el-table-column prop="completed_tasks" label="完成任务数" />
          <el-table-column prop="completion_rate" label="完成率">
            <template #default="scope">
              <el-progress 
                :percentage="scope.row.completion_rate" 
                :stroke-width="8"
                :show-text="true"
              />
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 用户绩效表 -->
      <div class="table-card">
        <div class="table-header">
          <h3>用户绩效排行</h3>
        </div>
        <el-table :data="userPerformance" style="width: 100%">
          <el-table-column prop="user_name" label="用户姓名" />
          <el-table-column prop="total_tasks" label="总任务数" />
          <el-table-column prop="completed_tasks" label="完成任务数" />
          <el-table-column prop="total_hours" label="总工时">
            <template #default="scope">
              {{ scope.row.total_hours }}h
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Clock, Loading, Check, Warning, Document, User 
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as taskApi from '@/api/taskApi'

// 响应式数据
const loading = ref(false)
const trendDays = ref(30)
const dateRange = ref([])
const globalStats = ref({})
const departmentDistribution = ref([])
const trendData = ref([])
const userWorkloadRanking = ref([])
const statusStats = ref({})
const systemLoadData = ref([])
const departmentPerformance = ref([])
const userPerformance = ref([])

// 图表引用
const departmentChartRef = ref(null)
const trendChartRef = ref(null)
const workloadRankingChartRef = ref(null)
const statusChartRef = ref(null)
const systemLoadChartRef = ref(null)

// 图表实例
let departmentChart = null
let trendChart = null
let workloadRankingChart = null
let statusChart = null
let systemLoadChart = null

// 加载全局统计
const loadGlobalStats = async () => {
  try {
    const response = await taskApi.getGlobalTaskStats()
    if (response.code === 200) {
      globalStats.value = response.data
    }
  } catch (error) {
    console.error('加载全局统计失败:', error)
  }
}

// 加载部门分布数据
const loadDepartmentDistribution = async () => {
  try {
    const response = await taskApi.getDepartmentTaskDistribution()
    if (response.code === 200) {
      departmentDistribution.value = response.data
      departmentPerformance.value = response.data
      renderDepartmentChart()
    }
  } catch (error) {
    console.error('加载部门分布失败:', error)
  }
}

// 加载趋势数据
const loadTrendData = async () => {
  try {
    const response = await taskApi.getTaskTrendStats(trendDays.value)
    if (response.code === 200) {
      trendData.value = response.data
      renderTrendChart()
    }
  } catch (error) {
    console.error('加载趋势数据失败:', error)
  }
}

// 加载用户工作量排行
const loadUserWorkloadRanking = async () => {
  try {
    const response = await taskApi.getUserWorkloadRanking(10)
    if (response.code === 200) {
      userWorkloadRanking.value = response.data
      userPerformance.value = response.data
      renderWorkloadRankingChart()
    }
  } catch (error) {
    console.error('加载用户工作量排行失败:', error)
  }
}

// 加载状态统计
const loadStatusStats = async () => {
  try {
    const response = await taskApi.getTaskStatsByStatus()
    if (response.code === 200) {
      statusStats.value = response.data
      renderStatusChart()
    }
  } catch (error) {
    console.error('加载状态统计失败:', error)
  }
}

// 加载系统负载数据
const loadSystemLoadData = async () => {
  try {
    // TODO: 实现系统负载数据API
    renderSystemLoadChart()
  } catch (error) {
    console.error('加载系统负载数据失败:', error)
  }
}

// 渲染部门分布图表
const renderDepartmentChart = () => {
  if (!departmentChartRef.value || !departmentDistribution.value.length) return
  
  if (departmentChart) {
    departmentChart.dispose()
  }
  
  departmentChart = echarts.init(departmentChartRef.value)
  
  const data = departmentDistribution.value.map(item => ({
    value: item.total_tasks,
    name: item.department_name
  }))
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '部门任务分布',
        type: 'pie',
        radius: '50%',
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  departmentChart.setOption(option)
}

// 渲染趋势图表
const renderTrendChart = () => {
  if (!trendChartRef.value || !trendData.value.length) return
  
  if (trendChart) {
    trendChart.dispose()
  }
  
  trendChart = echarts.init(trendChartRef.value)
  
  const dates = trendData.value.map(item => item.date)
  const createdData = trendData.value.map(item => item.created_count)
  const completedData = trendData.value.map(item => item.completed_count)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['创建任务', '完成任务']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '创建任务',
        type: 'line',
        data: createdData,
        smooth: true,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '完成任务',
        type: 'line',
        data: completedData,
        smooth: true,
        itemStyle: {
          color: '#67C23A'
        }
      }
    ]
  }
  
  trendChart.setOption(option)
}

// 渲染工作量排行图表
const renderWorkloadRankingChart = () => {
  if (!workloadRankingChartRef.value || !userWorkloadRanking.value.length) return

  if (workloadRankingChart) {
    workloadRankingChart.dispose()
  }

  workloadRankingChart = echarts.init(workloadRankingChartRef.value)

  const users = userWorkloadRanking.value.map(item => item.user_name)
  const workloads = userWorkloadRanking.value.map(item => item.total_tasks)

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: users,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value'
    },
    series: [
      {
        name: '任务数量',
        type: 'bar',
        data: workloads,
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  }

  workloadRankingChart.setOption(option)
}

// 渲染状态分布图表
const renderStatusChart = () => {
  if (!statusChartRef.value) return

  if (statusChart) {
    statusChart.dispose()
  }

  statusChart = echarts.init(statusChartRef.value)

  const data = [
    { value: statusStats.value.pending || 0, name: '待处理' },
    { value: statusStats.value.processing || 0, name: '进行中' },
    { value: statusStats.value.completed || 0, name: '已完成' },
    { value: statusStats.value.overdue || 0, name: '已逾期' }
  ]

  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        data: data,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }

  statusChart.setOption(option)
}

// 渲染系统负载图表
const renderSystemLoadChart = () => {
  if (!systemLoadChartRef.value) return

  if (systemLoadChart) {
    systemLoadChart.dispose()
  }

  systemLoadChart = echarts.init(systemLoadChartRef.value)

  // 模拟系统负载数据
  const dates = []
  const taskCreated = []
  const taskCompleted = []
  const activeUsers = []

  for (let i = 29; i >= 0; i--) {
    const date = new Date()
    date.setDate(date.getDate() - i)
    dates.push(date.toLocaleDateString('zh-CN'))
    taskCreated.push(Math.floor(Math.random() * 20) + 5)
    taskCompleted.push(Math.floor(Math.random() * 15) + 3)
    activeUsers.push(Math.floor(Math.random() * 50) + 20)
  }

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['创建任务', '完成任务', '活跃用户']
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '任务数量',
        position: 'left'
      },
      {
        type: 'value',
        name: '用户数量',
        position: 'right'
      }
    ],
    series: [
      {
        name: '创建任务',
        type: 'bar',
        data: taskCreated,
        itemStyle: {
          color: '#409EFF'
        }
      },
      {
        name: '完成任务',
        type: 'bar',
        data: taskCompleted,
        itemStyle: {
          color: '#67C23A'
        }
      },
      {
        name: '活跃用户',
        type: 'line',
        yAxisIndex: 1,
        data: activeUsers,
        smooth: true,
        itemStyle: {
          color: '#E6A23C'
        }
      }
    ]
  }

  systemLoadChart.setOption(option)
}

// 初始化图表
const initCharts = async () => {
  await nextTick()
  renderDepartmentChart()
  renderTrendChart()
  renderWorkloadRankingChart()
  renderStatusChart()
  renderSystemLoadChart()
}

// 窗口大小变化时重新调整图表
const handleResize = () => {
  if (departmentChart) departmentChart.resize()
  if (trendChart) trendChart.resize()
  if (workloadRankingChart) workloadRankingChart.resize()
  if (statusChart) statusChart.resize()
  if (systemLoadChart) systemLoadChart.resize()
}

onMounted(async () => {
  loading.value = true
  try {
    await Promise.all([
      loadGlobalStats(),
      loadDepartmentDistribution(),
      loadTrendData(),
      loadUserWorkloadRanking(),
      loadStatusStats(),
      loadSystemLoadData()
    ])
    await initCharts()
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }

  window.addEventListener('resize', handleResize)
})

// 清理
onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (departmentChart) departmentChart.dispose()
  if (trendChart) trendChart.dispose()
  if (workloadRankingChart) workloadRankingChart.dispose()
  if (statusChart) statusChart.dispose()
  if (systemLoadChart) systemLoadChart.dispose()
})
</script>

<style scoped>
.task-statistics {
  padding: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
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

.stat-icon.total {
  background: linear-gradient(135deg, #6366f1, #4f46e5);
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

.stat-icon.users {
  background: linear-gradient(135deg, #8b5cf6, #7c3aed);
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

.charts-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.chart-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.chart-card.full-width {
  grid-column: 1 / -1;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.chart-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chart-controls {
  display: flex;
  gap: 12px;
  align-items: center;
}

.chart-content {
  position: relative;
}

.chart {
  width: 100%;
}

.data-tables {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(500px, 1fr));
  gap: 20px;
}

.table-card {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.table-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
}

.table-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

@media (max-width: 1200px) {
  .charts-container {
    grid-template-columns: 1fr;
  }

  .data-tables {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .stats-cards {
    grid-template-columns: 1fr;
  }

  .chart-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .chart-controls {
    width: 100%;
  }
}
</style>
